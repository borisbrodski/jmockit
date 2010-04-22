/*
 * JMockit Core
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
package mockit.internal.core;

import static mockit.external.asm.Opcodes.*;

import mockit.external.asm.*;
import mockit.internal.*;
import mockit.internal.annotations.*;
import mockit.internal.startup.*;
import mockit.internal.state.*;
import mockit.internal.util.*;

/**
 * Responsible for generating all necessary bytecode in the redefined (real) class. Such code will
 * redirect calls made on "real" methods to equivalent calls on the corresponding "mock" methods.
 * The original code won't be executed by the running JVM until the class redefinition is undone.
 * <p/>
 * Methods in the real class which have no corresponding mock method are unaffected.
 * <p/>
 * Any fields (static or not) in the real class remain untouched.
 */
public class RealClassModifier extends BaseClassModifier
{
   private final String itFieldDesc;
   private final AnnotatedMockMethods mockMethods;
   private final int mockInstanceIndex;
   private final boolean forStartupMock;

   // Helper fields:
   private String realSuperClassName;
   protected String mockName;
   private boolean mockIsStatic;
   protected int varIndex;
   private String methodOrConstructorDesc;
   private int initialVar;

   /**
    * Initializes the modifier for a given real/mock class pair.
    * <p/>
    * If a mock instance is provided, it will receive calls for any instance methods defined in the
    * mock class. If not, a new instance will be created for such calls. In the first case, the mock
    * instance will need to be recovered by the modified bytecode inside the real method. To enable
    * this, the given mock instance is added to the end of a global list made available through
    * {@link TestRun#getMock(int)}.
    *
    * @param cr the class file reader for the real class
    * @param mock an instance of the mock class or null to create one
    * @param mockMethods contains the set of mock methods collected from the mock class; each
    * mock method is identified by a pair composed of "name" and "desc", where "name" is the method
    * name or "<init>" for a constructor, and "desc" is the JVM's internal description of the
    * parameters; once the real class modification is complete this set will be empty, unless no
    * corresponding real method was found for any of its methods identifiers
    *
    * @throws IllegalArgumentException if no mock instance is given but the mock class is an inner
    * class, which cannot be instantiated since the enclosing instance is not known
    */
   public RealClassModifier(
      ClassReader cr, String realClassDesc, Object mock, AnnotatedMockMethods mockMethods,
      boolean forStartupMock)
   {
      super(cr);
      itFieldDesc = realClassDesc;
      this.mockMethods = mockMethods;
      this.forStartupMock = forStartupMock;

      if (mock != null) {
         mockInstanceIndex = TestRun.getMockClasses().getMocks(forStartupMock).addMock(mock);
      }
      else if (!mockMethods.isInnerMockClass()) {
         mockInstanceIndex = -1;
      }
      else {
         throw new IllegalArgumentException(
            "An inner mock class cannot be instantiated without its enclosing instance; " +
            "you must either pass a mock instance, or make the class static");
      }
   }

   @SuppressWarnings({"DesignForExtension"})
   @Override
   public void visit(
      int version, int access, String name, String signature, String superName, String[] interfaces)
   {
      super.visit(version, access, name, signature, superName, interfaces);
      realSuperClassName = superName;
   }

   /**
    * If the specified method has a mock definition, then generates bytecode to redirect calls made
    * to it to the mock method. If it has no mock, does nothing.
    *
    * @param access not relevant
    * @param name together with desc, used to identity the method in given set of mock methods
    * @param signature not relevant
    * @param exceptions not relevant
    *
    * @return null if the method was redefined, otherwise a MethodWriter that writes out the visited
    * method code without changes
    */
   @Override
   public final MethodVisitor visitMethod(
      int access, String name, String desc, String signature, String[] exceptions)
   {
      if ((access & ACC_SYNTHETIC) != 0) {
         return super.visitMethod(access, name, desc, signature, exceptions);
      }

      boolean hasMock = hasMock(name, desc) || hasMockWithAlternativeName(name, desc);

      if (!hasMock) {
         return
            shouldCopyOriginalMethodBytecode(access, name, desc, signature, exceptions) ?
               super.visitMethod(access, name, desc, signature, exceptions) : null;
      }

      if ((access & ACC_NATIVE) != 0 && !Startup.isJava6OrLater()) {
         throw new IllegalArgumentException(
            "Mocking of native methods not supported under JDK 1.5: \"" + name + '\"');
      }

      startModifiedMethodVersion(access, name, desc, signature, exceptions);

      MethodVisitor alternativeWriter = getAlternativeMethodWriter(access, desc);
      
      if (alternativeWriter == null) {
         // Uses the MethodWriter just created to produce bytecode that calls the mock method.
         generateCallsForMockExecution(access, desc);
         generateMethodReturn(desc);
         mw.visitMaxs(1, 0); // dummy values, real ones are calculated by ASM

         // All desired bytecode is written at this point, so returns null in order to avoid
         // appending the original method bytecode, which would happen if mv was returned.
         return null;
      }

      return alternativeWriter;
   }

   @SuppressWarnings({"UnusedDeclaration", "DesignForExtension"})
   protected boolean shouldCopyOriginalMethodBytecode(
      int access, String name, String desc, String signature, String[] exceptions)
   {
      return true;
   }

   @SuppressWarnings({"UnusedDeclaration", "DesignForExtension"})
   protected MethodVisitor getAlternativeMethodWriter(int access, String desc)
   {
      return null;
   }

   private boolean hasMock(String name, String desc)
   {
      mockName = name;
      boolean hasMock = mockMethods.containsMethod(name, desc);

      if (hasMock) {
         mockIsStatic = mockMethods.containsStaticMethod(name, desc);
      }

      return hasMock;
   }

   private boolean hasMockWithAlternativeName(String name, String desc)
   {
      if ("<init>".equals(name)) {
         return hasMock("$init", desc);
      }
      else if ("<clinit>".equals(name)) {
         return hasMock("$clinit", desc);
      }

      return false;
   }

   protected final void generateCallsForMockExecution(int access, String desc)
   {
      if ("<init>".equals(mockName) || "$init".equals(mockName)) {
         generateCallToSuper();
      }

      generateCallToMock(access, desc);
   }

   protected final void generateCallToSuper()
   {
      mw.visitVarInsn(ALOAD, 0);
      
      String constructorDesc = new SuperConstructorCollector(1).findConstructor(realSuperClassName);
      pushDefaultValuesForParameterTypes(constructorDesc);

      mw.visitMethodInsn(INVOKESPECIAL, realSuperClassName, "<init>", constructorDesc);
   }

   @SuppressWarnings({"DesignForExtension"})
   protected void generateCallToMock(int access, String desc)
   {
      methodOrConstructorDesc = desc;

      if ("<init>".equals(mockName)) {
         // Note: trying to call a constructor on a getMock(i) instance doesn't work (for reasons
         // not clear), and it's also not possible to set the "it" field before calling the mock
         // constructor; so, we do the only thing that can be done for mock constructors, which is
         // to instantiate a new mock object and then call the mock constructor on it.
         generateInstantiationAndConstructorCall(access);
      }
      else if (mockIsStatic) {
         generateStaticMethodCall(access);
      }
      else {
         generateInstanceMethodCall(access);
      }
   }

   private void generateInstantiationAndConstructorCall(int access)
   {
      if (useMockingBridge) {
         generateCallToMockingBridge(
            MockingBridge.CALL_CONSTRUCTOR_MOCK, getMockClassInternalName(), access, mockName,
            methodOrConstructorDesc, null);
         return;
      }

      generateMockObjectInstantiation();

      // Generate a call to the mock constructor, with the real constructor's arguments.
      initialVar = 1;
      generateMethodOrConstructorArguments();
      mw.visitMethodInsn(
         INVOKESPECIAL, getMockClassInternalName(), "<init>", methodOrConstructorDesc);

      mw.visitInsn(POP);
   }

   protected final String getMockClassInternalName()
   {
      return mockMethods.getMockClassInternalName();
   }

   private void generateMockObjectInstantiation()
   {
      mw.visitTypeInsn(NEW, getMockClassInternalName());
      mw.visitInsn(DUP);
   }

   private void generateStaticMethodCall(int access)
   {
      String mockClassName = getMockClassInternalName();

      if (useMockingBridge) {
         generateCallToMockingBridge(
            MockingBridge.CALL_STATIC_MOCK, mockClassName, access, mockName,
            methodOrConstructorDesc, null);
      }
      else {
         initialVar = initialLocalVariableIndexForRealMethod(access);
         String desc = generateInvocationArgumentIfNeeded();
         generateMethodOrConstructorArguments();
         mw.visitMethodInsn(INVOKESTATIC, mockClassName, mockName, desc);
      }
   }

   protected final int initialLocalVariableIndexForRealMethod(int access)
   {
      return (access & ACC_STATIC) == 0 ? 1 : 0;
   }

   private void generateInstanceMethodCall(int access)
   {
      String mockClassName = getMockClassInternalName();

      if (useMockingBridge) {
         generateCallToMockingBridge(
            MockingBridge.CALL_INSTANCE_MOCK, mockClassName, access, mockName,
            methodOrConstructorDesc, mockInstanceIndex);
         return;
      }

      if (mockInstanceIndex < 0) {
         // No mock instance available yet.
         obtainMockInstanceForInvocation(access, mockClassName);
      }
      else {
         // A mock instance is available, so retrieve it from the global list.
         generateGetMockCallWithMockInstanceIndex();
      }

      if ((access & ACC_STATIC) == 0 && mockMethods.isWithItField()) {
         generateItFieldSetting();
      }

      generateMockInstanceMethodInvocationWithRealMethodArgs(access);
   }

   @SuppressWarnings({"DesignForExtension"})
   protected void obtainMockInstanceForInvocation(int access, String mockClassName)
   {
      generateMockObjectInstantiation();
      mw.visitMethodInsn(INVOKESPECIAL, mockClassName, "<init>", "()V");
   }

   private void generateGetMockCallWithMockInstanceIndex()
   {
      mw.visitIntInsn(SIPUSH, mockInstanceIndex);
      String methodName = forStartupMock ? "getStartupMock" : "getMock";
      mw.visitMethodInsn(
         INVOKESTATIC, "mockit/internal/state/TestRun", methodName, "(I)Ljava/lang/Object;");
      mw.visitTypeInsn(CHECKCAST, getMockClassInternalName());
   }

   private void generateItFieldSetting()
   {
      Type[] argTypes = Type.getArgumentTypes(methodOrConstructorDesc);
      int var = 1;

      for (Type argType : argTypes) {
         var += argType.getSize();
      }

      mw.visitVarInsn(ASTORE, var); // stores the mock instance into local variable
      mw.visitVarInsn(ALOAD, var);  // loads the mock instance onto the operand stack
      mw.visitVarInsn(ALOAD, 0); // loads "this" onto the operand stack
      mw.visitFieldInsn(PUTFIELD, getMockClassInternalName(), "it", itFieldDesc);
      mw.visitVarInsn(ALOAD, var);  // again loads the mock instance onto the stack
   }

   private void generateMockInstanceMethodInvocationWithRealMethodArgs(int access)
   {
      initialVar = initialLocalVariableIndexForRealMethod(access);
      String desc = generateInvocationArgumentIfNeeded();
      generateMethodOrConstructorArguments();
      mw.visitMethodInsn(INVOKEVIRTUAL, getMockClassInternalName(), mockName, desc);
   }

   private String generateInvocationArgumentIfNeeded()
   {
      if (mockMethods.isWithInvocationParameter()) {
         mw.visitInsn(ACONST_NULL);
//         initialVar++;
         return "(Lmockit/Invocation;" + methodOrConstructorDesc.substring(1);
      }

      return methodOrConstructorDesc;
   }

   private void generateMethodOrConstructorArguments()
   {
      Type[] argTypes = Type.getArgumentTypes(methodOrConstructorDesc);
      varIndex = initialVar;

      for (Type argType : argTypes) {
         int opcode = argType.getOpcode(ILOAD);
         mw.visitVarInsn(opcode, varIndex);
         varIndex += argType.getSize();
      }
   }

   protected final void generateMethodReturn(String desc)
   {
      if (useMockingBridge) {
         generateReturnWithObjectAtTopOfTheStack(desc);
      }
      else {
         Type returnType = Type.getReturnType(desc);
         mw.visitInsn(returnType.getOpcode(IRETURN));
      }
   }
}
