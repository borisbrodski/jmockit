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
package mockit.internal.expectations.mocking;

import java.util.*;

import static java.lang.reflect.Modifier.*;

import org.objectweb.asm2.*;
import static org.objectweb.asm2.Opcodes.*;
import org.objectweb.asm2.Type;
import org.objectweb.asm2.commons.*;

import mockit.internal.*;
import mockit.internal.filtering.*;
import mockit.internal.startup.*;
import mockit.internal.util.*;

final class ExpectationsModifier extends BaseClassModifier
{
   private static final int METHOD_ACCESS_MASK = ACC_SYNTHETIC + ACC_ABSTRACT;
   private static final Type VOID_TYPE = Type.getType("Ljava/lang/Void;");

   private static final Map<String, String> DEFAULT_FILTERS = new HashMap<String, String>()
   {{
      put("java/lang/System", "arraycopy getSecurityManager");
   }};

   private final MockingConfiguration mockingCfg;
   private final boolean mockingCfgNullOrEmpty;
   private final MockConstructorInfo mockConstructorInfo;
   private String redefinedConstructorDesc;
   private String superClassName;
   private String className;
   private String baseClassNameForCapturedInstanceMethods;
   private boolean isProxy;
   private String defaultFilters;

   ExpectationsModifier(
      ClassLoader classLoader, ClassReader classReader, MockingConfiguration mockingConfiguration,
      MockConstructorInfo mockConstructorInfo)
   {
      super(classReader);
      mockingCfg = mockingConfiguration;
      mockingCfgNullOrEmpty = mockingConfiguration == null || mockingConfiguration.isEmpty();
      this.mockConstructorInfo = mockConstructorInfo;
      setUseMockingBridge(classLoader);
   }

   public void setClassNameForInstanceMethods(String internalClassName)
   {
      baseClassNameForCapturedInstanceMethods = internalClassName;
   }

   @Override
   public void visit(
      int version, int access, String name, String signature, String superName, String[] interfaces)
   {
      superClassName = superName;

      if (mockingCfg != null) {
         mockingCfg.setSuperClassName(superName);
      }

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
   public MethodVisitor visitMethod(
      int access, String name, String desc, String signature, String[] exceptions)
   {
      boolean visitingConstructor = "<init>".equals(name);

      if (
         (access & METHOD_ACCESS_MASK) != 0 || "<clinit>".equals(name) ||
         isProxy && isConstructorOrSystemMethodNotToBeMocked(name, desc) ||
         !visitingConstructor && isMethodNotToBeMocked(access, name)
      ) {
         // Copies original without modifications when it's synthetic, abstract, a class
         // initialization block, belongs to a Proxy subclass, or is a static or private method in
         // a captured implementation class.
         return super.visitMethod(access, name, desc, signature, exceptions);
      }

      boolean matchesFilters = mockingCfg == null || mockingCfg.matchesFilters(name, desc);

      if (!matchesFilters || mockingCfgNullOrEmpty && isMethodFromObject(name, desc)) {
         // Copies original without modifications if it doesn't pass the filters, or when it's an
         // override of equals, hashCode, toString or finalize (from java.lang.Object) not
         // prohibited by any mock filter.
         return super.visitMethod(access, name, desc, signature, exceptions);
      }

      boolean visitingNativeMethod = isNative(access);

      if (visitingNativeMethod && !Startup.isJava6OrLater()) {
         throw new IllegalArgumentException(
            "Mocking of native methods not supported under JDK 1.5; please filter out method \"" +
            name + "\", or run under JDK 1.6+");
      }

      // Otherwise, replace original implementation.
      startModifiedMethodVersion(access, name, desc, signature, exceptions);

      if (superClassName != null && visitingConstructor) {
         redefinedConstructorDesc = desc;
         generateCallToDefaultOrConfiguredSuperConstructor();
      }

      String internalClassName = className;

      if (baseClassNameForCapturedInstanceMethods != null && !visitingConstructor) {
         internalClassName = baseClassNameForCapturedInstanceMethods;
      }

      if (useMockingBridge) {
         generateCallToMockingBridge(
            MockingBridge.RECORD_OR_REPLAY, internalClassName, access, name, desc, null);
         generateDecisionBetweenReturningOrContinuingToRealImplementation(desc);

         if (visitingNativeMethod) {
            generateEmptyImplementation(desc);
            return null;
         }
         else {
            return new MethodAdapter(mw);
         }
      }

      generateDirectCallToRecordOrReplay(internalClassName, access, name, desc);
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

   private boolean isMethodFromObject(String name, String desc)
   {
      return
         "equals".equals(name)   && "(Ljava/lang/Object;)Z".equals(desc) ||
         "hashCode".equals(name) && "()I".equals(desc) ||
         "toString".equals(name) && "()Ljava/lang/String;".equals(desc) ||
         "finalize".equals(name) && "()V".equals(desc);
   }

   private boolean isMethodNotToBeMocked(int access, String name)
   {
      return
         isMethodFromCapturedClassNotToBeMocked(access) ||
         defaultFilters != null && defaultFilters.contains(name);
   }

   private boolean isMethodFromCapturedClassNotToBeMocked(int access)
   {
      return
         baseClassNameForCapturedInstanceMethods != null && (isStatic(access) || isPrivate(access));
   }

   private void generateCallToDefaultOrConfiguredSuperConstructor()
   {
      mw.visitVarInsn(ALOAD, 0);

      String constructorDesc;

      if (mockConstructorInfo != null && mockConstructorInfo.isWithSuperConstructor()) {
         constructorDesc = generateCallToSuperConstructorUsingTestProvidedArguments();
      }
      else if (mockingCfg != null) {
         Type[] paramTypes = mockingCfg.getSuperConstructorParameterTypes();
         constructorDesc = generateSuperConstructorArguments(paramTypes);
      }
      else {
         constructorDesc = new SuperConstructorCollector(1).findConstructor(superClassName);
         pushDefaultValuesForParameterTypes(constructorDesc);
      }

      mw.visitMethodInsn(INVOKESPECIAL, superClassName, "<init>", constructorDesc);
   }

   private String generateCallToSuperConstructorUsingTestProvidedArguments()
   {
      Type[] paramTypes = mockConstructorInfo.getParameterTypesForSuperConstructor();
      Object[] args = mockConstructorInfo.getSuperConstructorArguments();
      String constructorDesc = Type.getMethodDescriptor(Type.VOID_TYPE, paramTypes);
      GeneratorAdapter generator = new GeneratorAdapter(mw, 0, "<init>", constructorDesc);
      int i = 0;

      for (Type paramType : paramTypes) {
         Object arg = args[i++];
         pushParameterValueForSuperConstructorCall(paramType, arg, generator);
      }

      return constructorDesc;
   }

   private void pushParameterValueForSuperConstructorCall(
      Type paramType, Object arg, GeneratorAdapter generator)
   {
      switch (paramType.getSort()) {
         case Type.BOOLEAN: generator.push((Boolean) arg); break;
         case Type.CHAR:    generator.push((Character) arg); break;
         case Type.BYTE:    generator.push((Byte) arg); break;
         case Type.SHORT:   generator.push((Short) arg); break;
         case Type.INT:     generator.push((Integer) arg); break;
         case Type.LONG:    generator.push((Long) arg); break;
         case Type.FLOAT:   generator.push((Float) arg); break;
         case Type.DOUBLE:  generator.push((Double) arg); break;
         default:
            pushObjectValueForSuperConstructorCall(paramType, arg, generator);
      }
   }

   private void pushObjectValueForSuperConstructorCall(
      Type paramType, Object value, GeneratorAdapter generator)
   {
      if (value == null || value instanceof String) {
         generator.push((String) value);
      }
      else if (value instanceof Class<?>) {
         generator.push(paramType);
      }
      else {
         // TODO: get Object value onto stack by calling an static method which returns
         // the argument value
         mw.visitInsn(ACONST_NULL);
      }
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

   String getRedefinedConstructorDesc()
   {
      return redefinedConstructorDesc;
   }
}
