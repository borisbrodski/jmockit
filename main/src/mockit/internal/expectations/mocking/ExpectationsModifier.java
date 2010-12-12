/*
 * JMockit Expectations & Verifications
 * Copyright (c) 2006-2010 Rogério Liesenfeld
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
package mockit.internal.expectations.mocking;

import java.util.*;

import static java.lang.reflect.Modifier.*;

import static mockit.external.asm.Opcodes.*;

import mockit.external.asm.Type;

import mockit.external.asm.*;
import mockit.internal.*;
import mockit.internal.filtering.*;
import mockit.internal.startup.*;
import mockit.internal.util.*;

@SuppressWarnings({"ClassWithTooManyFields"})
final class ExpectationsModifier extends BaseClassModifier
{
   private static final int METHOD_ACCESS_MASK = ACC_SYNTHETIC + ACC_ABSTRACT;
   private static final Type VOID_TYPE = Type.getType("Ljava/lang/Void;");
   private static final Map<String, String> DEFAULT_FILTERS = new HashMap<String, String>()
   {{
      put("java/lang/Object", "<init> getClass hashCode");
      put("java/lang/System", "arraycopy getSecurityManager");
      put("java/util/AbstractCollection", "<init>");
      put("java/util/AbstractList", "<init> iterator");
      put("java/util/ArrayList", "get size RangeCheck");
      put("java/util/Hashtable", "get");
      put("java/lang/Throwable", "<init> fillInStackTrace");
      put("java/lang/Exception", "<init>");
   }};

   private final MockingConfiguration mockingCfg;
   private String superClassName;
   private String className;
   private String baseClassNameForCapturedInstanceMethods;
   private boolean stubOutClassInitialization;
   private boolean ignoreStaticMethods;
   private int executionMode;
   private boolean isProxy;
   private String defaultFilters;

   ExpectationsModifier(ClassLoader classLoader, ClassReader classReader, MockingConfiguration mockingConfiguration)
   {
      super(classReader);
      mockingCfg = mockingConfiguration;
      stubOutClassInitialization = true;
      setUseMockingBridge(classLoader);
   }

   public void setClassNameForInstanceMethods(String internalClassName)
   {
      baseClassNameForCapturedInstanceMethods = internalClassName;
   }

   public void setStubOutClassInitialization(boolean stubOutClassInitialization)
   {
      this.stubOutClassInitialization = stubOutClassInitialization;
   }

   public void setIgnoreStaticMethods(boolean ignoreStaticMethods)
   {
      this.ignoreStaticMethods = ignoreStaticMethods;
   }

   public void setExecutionMode(int executionMode)
   {
      this.executionMode = executionMode;
   }

   @Override
   public void visit(int version, int access, String name, String signature, String superName, String[] interfaces)
   {
      superClassName = superName;
      super.visit(version, access, name, signature, superName, interfaces);
      isProxy = "java/lang/reflect/Proxy".equals(superName);

      if (isProxy) {
         className = interfaces[0];
         defaultFilters = null;
      }
      else {
         className = name;
         defaultFilters = DEFAULT_FILTERS.get(name);
      }
   }

   @Override
   public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions)
   {
      if (
         (access & METHOD_ACCESS_MASK) != 0 ||
         isProxy && isConstructorOrSystemMethodNotToBeMocked(name, desc) ||
         isMethodOrConstructorNotToBeMocked(access, name)
      ) {
         // Copies original without modifications when it's synthetic or abstract, belongs to a
         // Proxy subclass, or is a static or private method in a captured implementation class.
         return super.visitMethod(access, name, desc, signature, exceptions);
      }

      boolean noFiltersToMatch = mockingCfg == null || mockingCfg.isEmpty();
      boolean matchesFilters = noFiltersToMatch || mockingCfg.matchesFilters(name, desc);

      if ("<clinit>".equals(name)) {
         return stubOutClassInitializationIfApplicable(access, name, desc, signature, exceptions, matchesFilters);
      }

      if (!matchesFilters || noFiltersToMatch && isMethodFromObject(name, desc)) {
         // Copies original without modifications if it doesn't pass the filters, or when it's an override of
         // equals, hashCode, toString or finalize (from java.lang.Object) not prohibited by any mock filter.
         return super.visitMethod(access, name, desc, signature, exceptions);
      }

      // Otherwise, replace original implementation with redirect to JMockit.
      validateModificationOfNativeMethod(access, name);
      startModifiedMethodVersion(access, name, desc, signature, exceptions);

      boolean visitingConstructor = "<init>".equals(name);

      if (visitingConstructor && superClassName != null) {
         generateCallToSuperConstructor();
      }

      String internalClassName = className;

      if (baseClassNameForCapturedInstanceMethods != null && !visitingConstructor) {
         internalClassName = baseClassNameForCapturedInstanceMethods;
      }

      if (useMockingBridge) {
         return generateCallToHandlerThroughMockingBridge(access, name, desc, internalClassName);
      }

      generateDirectCallToHandler(internalClassName, access, name, desc, executionMode);

      if (executionMode > 0) {
         generateDecisionBetweenReturningOrContinuingToRealImplementation(desc);
         return copyOriginalImplementationCode(access, desc, visitingConstructor);
      }

      generateReturnWithObjectAtTopOfTheStack(desc);
      mw.visitMaxs(1, 0);
      return null;
   }

   private boolean isConstructorOrSystemMethodNotToBeMocked(String name, String desc)
   {
      return
         "<init>".equals(name) || isMethodFromObject(name, desc) ||
         "annotationType".equals(name) && "()Ljava/lang/Class;".equals(desc);
   }

   private boolean isMethodOrConstructorNotToBeMocked(int access, String name)
   {
      return
         isConstructorToBeIgnored(name) ||
         isStaticMethodToBeIgnored(access) ||
         isMethodFromCapturedClassNotToBeMocked(access) ||
         defaultFilters != null && defaultFilters.contains(name);
   }

   private boolean isConstructorToBeIgnored(String name)
   {
      return executionMode == 2 && "<init>".equals(name);
   }

   private boolean isStaticMethodToBeIgnored(int access)
   {
      return ignoreStaticMethods && isStatic(access);
   }

   private boolean isMethodFromCapturedClassNotToBeMocked(int access)
   {
      return baseClassNameForCapturedInstanceMethods != null && (isStatic(access) || isPrivate(access));
   }

   private MethodVisitor stubOutClassInitializationIfApplicable(
      int access, String name, String desc, String signature, String[] exceptions, boolean matchesFilters)
   {
      mw = super.visitMethod(access, name, desc, signature, exceptions);

      if (matchesFilters && stubOutClassInitialization) {
         // Stub out any class initialization block (unless specified otherwise),
         // to avoid potential side effects in tests.
         generateEmptyImplementation();
         return null;
      }

      return mw;
   }

   private void validateModificationOfNativeMethod(int access, String name)
   {
      if (isNative(access) && !Startup.isJava6OrLater()) {
         throw new IllegalArgumentException(
            "Mocking of native methods not supported under JDK 1.5; please filter out method \"" +
            name + "\", or run under JDK 1.6+");
      }
   }

   private void generateCallToSuperConstructor()
   {
      mw.visitVarInsn(ALOAD, 0);

      String constructorDesc;

      if ("java/lang/Object".equals(superClassName)) {
         constructorDesc = "()V";
      }
      else {
         constructorDesc = SuperConstructorCollector.INSTANCE.findConstructor(superClassName);
         pushDefaultValuesForParameterTypes(constructorDesc);
      }

      mw.visitMethodInsn(INVOKESPECIAL, superClassName, "<init>", constructorDesc);
   }

   private MethodVisitor generateCallToHandlerThroughMockingBridge(
      int access, String name, String desc, String internalClassName)
   {
      generateCallToMockingBridge(MockingBridge.RECORD_OR_REPLAY, internalClassName, access, name, desc, executionMode);
      generateDecisionBetweenReturningOrContinuingToRealImplementation(desc);
      return copyOriginalImplementationCode(access, desc, false);
   }

   private void generateDecisionBetweenReturningOrContinuingToRealImplementation(String desc)
   {
      mw.visitInsn(DUP);
      mw.visitLdcInsn(VOID_TYPE);

      Label startOfRealImplementation = new Label();
      mw.visitJumpInsn(IF_ACMPEQ, startOfRealImplementation);

      generateReturnWithObjectAtTopOfTheStack(desc);

      mw.visitLabel(startOfRealImplementation);
      mw.visitInsn(POP);
   }

   private MethodVisitor copyOriginalImplementationCode(int access, String desc, boolean specialTreatmentForConstructor)
   {
      if (isNative(access)) {
         generateEmptyImplementation(desc);
         return null;
      }

      return specialTreatmentForConstructor ? new DynamicConstructorModifier() : new DynamicModifier();
   }

   private class DynamicModifier extends MethodAdapter
   {
      DynamicModifier() { super(mw); }

      @Override
      public final void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int idx)
      {
         // For some reason, the start position for "this" gets displaced by bytecode inserted at the beginning,
         // in a method modified by the EMMA tool. If not treated, this causes a ClassFormatError.
         if (start.position > end.position) {
            start.position = end.position;
         }

         super.visitLocalVariable(name, desc, signature, start, end, idx);
      }
   }

   private final class DynamicConstructorModifier extends DynamicModifier
   {
      @Override
      public void visitMethodInsn(int opcode, String owner, String name, String desc)
      {
         if (opcode == INVOKESPECIAL && (owner.equals(superClassName) || owner.equals(className))) {
            return;
         }

         mw.visitMethodInsn(opcode, owner, name, desc);
      }
   }
}
