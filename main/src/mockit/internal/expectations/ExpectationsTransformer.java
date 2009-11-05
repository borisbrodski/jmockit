/*
 * JMockit Expectations
 * Copyright (c) 2006-2009 Rogério Liesenfeld
 * All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package mockit.internal.expectations;

import java.lang.instrument.*;
import static java.lang.reflect.Modifier.isFinal;
import java.security.*;
import java.util.*;

import mockit.internal.state.*;
import mockit.internal.*;
import mockit.internal.util.*;
import mockit.*;
import org.objectweb.asm2.*;
import org.objectweb.asm2.commons.*;
import static org.objectweb.asm2.Opcodes.*;

public final class ExpectationsTransformer implements ClassFileTransformer
{
   private static final class VisitInterruptedException extends RuntimeException {}
   private static final VisitInterruptedException INTERRUPT_VISIT = new VisitInterruptedException();

   private final SuperClassAnalyser superClassAnalyser = new SuperClassAnalyser();
   private final List<String> baseSubclasses;

   public ExpectationsTransformer(Instrumentation instrumentation)
   {
      baseSubclasses = new ArrayList<String>();
      baseSubclasses.add("mockit/Expectations");
      baseSubclasses.add("mockit/NonStrictExpectations");
      baseSubclasses.add("mockit/Verifications");
      baseSubclasses.add("mockit/FullVerifications");
      baseSubclasses.add("mockit/VerificationsInOrder");
      baseSubclasses.add("mockit/FullVerificationsInOrder");

      Class<?>[] alreadyLoaded = instrumentation.getInitiatedClasses(getClass().getClassLoader());
      findOtherBaseSubclasses(alreadyLoaded);
      modifyFinalSubclasses(alreadyLoaded);
   }

   private void findOtherBaseSubclasses(Class<?>[] alreadyLoaded)
   {
      for (Class<?> aClass : alreadyLoaded) {
         if (!isFinalClass(aClass) && isSubclassFromUserCode(aClass)) {
            String classInternalName = aClass.getName().replace('.', '/');
            baseSubclasses.add(classInternalName);
         }
      }
   }

   private boolean isFinalClass(Class<?> aClass)
   {
      return isFinal(aClass.getModifiers()) || Utilities.isAnonymousClass(aClass);
   }

   private boolean isSubclassFromUserCode(Class<?> aClass)
   {
      return
         aClass != Expectations.class && aClass != NonStrictExpectations.class &&
         Expectations.class.isAssignableFrom(aClass) ||
         aClass != Verifications.class && aClass != FullVerifications.class &&
         aClass != VerificationsInOrder.class && aClass != FullVerificationsInOrder.class &&
         Verifications.class.isAssignableFrom(aClass);
   }

   private void modifyFinalSubclasses(Class<?>[] alreadyLoaded)
   {
      for (Class<?> aClass : alreadyLoaded) {
         if (isFinalClass(aClass) && isSubclassFromUserCode(aClass)) {
            ClassReader cr = ClassFile.createClassFileReader(aClass.getName());
            EndOfBlockModifier modifier = new EndOfBlockModifier(cr);

            try {
               cr.accept(modifier, false);
            }
            catch (RuntimeException ignore) {
               continue;
            }

            byte[] modifiedClassfile = modifier.toByteArray();
            RedefinitionEngine.redefineMethods(new ClassDefinition(aClass, modifiedClassfile));
         }
      }
   }

   public byte[] transform(
      ClassLoader loader, String className, Class<?> classBeingRedefined,
      ProtectionDomain protectionDomain, byte[] classfileBuffer)
   {
      if (classBeingRedefined == null && TestRun.isRunningTestCode(protectionDomain)) {
         ClassReader cr = new ClassReader(classfileBuffer);
         EndOfBlockModifier modifier = new EndOfBlockModifier(cr);
         cr.accept(modifier, false);
         return modifier.toByteArray();
      }

      return null;
   }

   private final class EndOfBlockModifier extends ClassWriter
   {
      MethodVisitor mw;
      String classDesc;

      EndOfBlockModifier(ClassReader cr) { super(cr, true); }

      @Override
      public void visit(
         int version, int access, String name, String signature, String superName,
         String[] interfaces)
      {
         int p = name.indexOf('$');

         if (p > 0) {
            boolean superClassIsKnownInvocationsSubclass = baseSubclasses.contains(superName);

            if (isFinal(access) || Character.isDigit(name.charAt(p + 1))) {
               if (
                  superClassIsKnownInvocationsSubclass ||
                  superClassAnalyser.classExtendsInvocationsClass(superName)
               ) {
                  super.visit(version, access, name, signature, superName, interfaces);
                  classDesc = name;
                  return; // go on and modify the class
               }
            }
            else if (superClassIsKnownInvocationsSubclass) {
               baseSubclasses.add(name);
            }
         }

         throw INTERRUPT_VISIT;
      }


      @Override
      public MethodVisitor visitMethod(
         int access, String name, String desc, String signature, String[] exceptions)
      {
         mw = super.visitMethod(access, name, desc, signature, exceptions);

         if ("<init>".equals(name)) {
            return new ConstructorModifier((MethodWriter) mw, classDesc);
         }

         return mw;
      }
   }

   private static final class ConstructorModifier extends MethodAdapter
   {
      static final String CALLBACK_CLASS_DESC = "mockit/internal/expectations/ActiveInvocations";
      final int[] matcherStacks = new int[20];
      int matchers;
      final MethodWriter mw;
      final String fieldOwner;

      ConstructorModifier(MethodWriter mw, String fieldOwner)
      {
         super(mw);
         this.mw = mw;
         this.fieldOwner = fieldOwner;
      }

      @Override
      public void visitFieldInsn(int opcode, String owner, String name, String desc)
      {
         mw.visitFieldInsn(opcode, owner, name, desc);

         if (opcode == GETSTATIC && fieldOwner.equals(owner) && name.startsWith("any")) {
            mw.visitMethodInsn(INVOKESTATIC, CALLBACK_CLASS_DESC, "addArgMatcher", "()V");
            matcherStacks[matchers++] = mw.stackSize;
         }
      }

      @Override
      public void visitMethodInsn(int opcode, String owner, String name, String desc)
      {
         if (opcode == INVOKEVIRTUAL && owner.equals(fieldOwner) && name.startsWith("with")) {
            mw.visitMethodInsn(opcode, owner, name, desc);
            matcherStacks[matchers++] = mw.stackSize;
            return;
         }

         if (matchers > 0) {
            Type[] argTypes = Type.getArgumentTypes(desc);
            int stackAfter = mw.stackSize - sumOfSizes(argTypes);

            if (stackAfter < matcherStacks[0]) {
               generateCallsToMoveArgMatchers(argTypes, stackAfter);
               matchers = 0;
            }
         }

         mw.visitMethodInsn(opcode, owner, name, desc);
      }

      private int sumOfSizes(Type[] argTypes)
      {
         int sum = 0;

         for (Type argType : argTypes) {
            sum += argType.getSize();
         }

         return sum;
      }

      private void generateCallsToMoveArgMatchers(Type[] argTypes, int initialStack)
      {
         int stack = initialStack;
         int nextMatcher = 0;
         int matcherStack = matcherStacks[0];

         for (int i = 0; i < argTypes.length && nextMatcher < matchers; i++) {
            stack += argTypes[i].getSize();

            if (stack == matcherStack || stack == matcherStack + 1) {
               if (nextMatcher < i) {
                  generateCallToMoveArgMatcher(nextMatcher, i);
               }

               matcherStack = matcherStacks[++nextMatcher];
            }
         }
      }

      private void generateCallToMoveArgMatcher(int originalMatcherIndex, int toIndex)
      {
         mw.visitIntInsn(SIPUSH, originalMatcherIndex);
         mw.visitIntInsn(SIPUSH, toIndex);
         mw.visitMethodInsn(INVOKESTATIC, CALLBACK_CLASS_DESC, "moveArgMatcher", "(II)V");
      }

      @Override
      public void visitInsn(int opcode)
      {
         if (opcode == RETURN) {
            mw.visitMethodInsn(INVOKESTATIC, CALLBACK_CLASS_DESC, "endInvocations", "()V");
         }

         mw.visitInsn(opcode);
      }
   }

   private final class SuperClassAnalyser extends EmptyVisitor
   {
      private boolean classExtendsBaseSubclass;

      boolean classExtendsInvocationsClass(String classOfInterest)
      {
         String className = classOfInterest.replace('/', '.');
         ClassReader cr = ClassFile.createClassFileReader(className);

         try {
            cr.accept(this, true);
         }
         catch (VisitInterruptedException ignore) {
            // OK
         }

         return classExtendsBaseSubclass;
      }

      @Override
      public void visit(
         int version, int access, String name, String signature, String superName,
         String[] interfaces)
      {
         classExtendsBaseSubclass = baseSubclasses.contains(superName);

         if (!classExtendsBaseSubclass && !"java/lang/Object".equals(superName)) {
            classExtendsInvocationsClass(superName);
         }

         throw INTERRUPT_VISIT;
      }
   }
}
