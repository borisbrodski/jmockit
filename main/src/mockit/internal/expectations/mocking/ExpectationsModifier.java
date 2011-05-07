/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
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
      put("java/lang/System", "arraycopy getProperties getSecurityManager");
      put("java/util/Hashtable", "get");
      put("java/lang/Throwable", "<init> fillInStackTrace");
      put("java/lang/Exception", "<init>");
   }};

   private final MockingConfiguration mockingCfg;
   private String superClassName;
   private String className;
   private String baseClassNameForCapturedInstanceMethods;
   private boolean stubOutClassInitialization;
   private boolean ignoreConstructors;
   private int executionMode;
   private boolean isProxy;
   private String defaultFilters;

   ExpectationsModifier(ClassLoader classLoader, ClassReader classReader, MockedType typeMetadata)
   {
      super(classReader);
      
      if (typeMetadata == null) {
         mockingCfg = null;
      }
      else {
         mockingCfg = typeMetadata.mockingCfg;
         stubOutClassInitialization = typeMetadata.isClassInitializationToBeStubbedOut();
      }

      setUseMockingBridge(classLoader);
   }

   public void setClassNameForCapturedInstanceMethods(String internalClassName)
   {
      baseClassNameForCapturedInstanceMethods = internalClassName;
   }

   public void useDynamicMocking(boolean methodsOnly)
   {
      ignoreConstructors = methodsOnly;
      executionMode = 1;
   }

   public void useDynamicMockingForInstanceMethods(MockedType typeMetadata)
   {
      ignoreConstructors = typeMetadata == null || typeMetadata.getMaxInstancesToCapture() <= 0;
      executionMode = 2;
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
      boolean syntheticOrAbstractMethod = (access & METHOD_ACCESS_MASK) != 0;

      if (syntheticOrAbstractMethod || isProxy && isConstructorOrSystemMethodNotToBeMocked(name, desc)) {
         return unmodifiedBytecode(access, name, desc, signature, exceptions);
      }

      boolean noFiltersToMatch = mockingCfg == null;
      boolean matchesFilters = noFiltersToMatch || mockingCfg.matchesFilters(name, desc);

      if ("<clinit>".equals(name)) {
         return stubOutClassInitializationIfApplicable(access, noFiltersToMatch, matchesFilters);
      }
      else if (stubOutFinalizeMethod(access, name, desc)) {
         return null;
      }

      if (
         !matchesFilters ||
         isMethodFromCapturedClassNotToBeMocked(access) ||
         noFiltersToMatch && isMethodOrConstructorNotToBeMocked(access, name)
      ) {
         return unmodifiedBytecode(access, name, desc, signature, exceptions);
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

      int actualExecutionMode = determineAppropriateExecutionMode(access, visitingConstructor);

      if (useMockingBridge) {
         return
            generateCallToHandlerThroughMockingBridge(
               access, name, desc, signature, exceptions, internalClassName, actualExecutionMode);
      }

      generateDirectCallToHandler(internalClassName, access, name, desc, signature, exceptions, actualExecutionMode);

      if (actualExecutionMode > 0) {
         generateDecisionBetweenReturningOrContinuingToRealImplementation(desc);
         return copyOriginalImplementationCode(access, desc, visitingConstructor);
      }

      generateReturnWithObjectAtTopOfTheStack(desc);
      mw.visitMaxs(1, 0);
      return null;
   }

   private MethodVisitor unmodifiedBytecode(int access, String name, String desc, String signature, String[] exceptions)
   {
      return super.visitMethod(access, name, desc, signature, exceptions);
   }

   private boolean isConstructorOrSystemMethodNotToBeMocked(String name, String desc)
   {
      return
         "<init>".equals(name) || isMethodFromObject(name, desc) ||
         "annotationType".equals(name) && "()Ljava/lang/Class;".equals(desc);
   }

   private MethodVisitor stubOutClassInitializationIfApplicable(int access, boolean noFilters, boolean matchesFilters)
   {
      mw = super.visitMethod(access, "<clinit>", "()V", null, null);

      if (!noFilters && matchesFilters || noFilters && stubOutClassInitialization) {
         generateEmptyImplementation();
         return null;
      }

      return mw;
   }

   private boolean stubOutFinalizeMethod(int access, String name, String desc)
   {
      if ("finalize".equals(name) && "()V".equals(desc)) {
         mw = super.visitMethod(access, name, desc, null, null);
         generateEmptyImplementation();
         return true;
      }
      
      return false;
   }
   
   private boolean isMethodFromCapturedClassNotToBeMocked(int access)
   {
      return baseClassNameForCapturedInstanceMethods != null && (isStatic(access) || isPrivate(access));
   }

   private boolean isMethodOrConstructorNotToBeMocked(int access, String name)
   {
      return
         isConstructorToBeIgnored(name) ||
         isStaticMethodToBeIgnored(access) ||
         isNativeMethodForDynamicMocking(access) ||
         defaultFilters != null && defaultFilters.contains(name);
   }

   private boolean isConstructorToBeIgnored(String name)
   {
      return ignoreConstructors && "<init>".equals(name);
   }

   private boolean isStaticMethodToBeIgnored(int access)
   {
      return executionMode == 2 && isStatic(access);
   }

   private boolean isNativeMethodForDynamicMocking(int access)
   {
      return executionMode > 0 && isNative(access);
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

   private int determineAppropriateExecutionMode(int access, boolean visitingConstructor)
   {
      if (executionMode == 2) {
         if (visitingConstructor) {
            return ignoreConstructors ? 0 : 1;
         }
         else if (isStatic(access)) {
            return 0;
         }
      }

      return executionMode;
   }

   private MethodVisitor generateCallToHandlerThroughMockingBridge(
      int access, String name, String desc, String genericSignature, String[] exceptions, String internalClassName,
      int executionMode)
   {
      generateCallToMockingBridge(
         MockingBridge.RECORD_OR_REPLAY, internalClassName, access, name, desc, genericSignature, exceptions,
         executionMode);
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
         if (end.position > 0 && start.position > end.position) {
            start.position = end.position;
         }

         // Ignores any local variable with required information missing, to avoid a VerifyError/ClassFormatError.
         if (start.position > 0 && end.position > 0) {
            super.visitLocalVariable(name, desc, signature, start, end, idx);
         }
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
