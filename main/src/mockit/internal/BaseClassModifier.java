/*
 * JMockit
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
package mockit.internal;

import java.lang.reflect.*;

import static mockit.external.asm.Opcodes.*;

import mockit.external.asm.*;
import mockit.external.asm.Type;
import mockit.internal.state.*;

@SuppressWarnings({"ClassWithTooManyMethods"})
public class BaseClassModifier extends ClassWriter
{
   private static final int ACCESS_MASK = 0xFFFF - ACC_ABSTRACT - ACC_NATIVE;
   private static final String[] PRIMITIVE_WRAPPER_TYPE = {
      null, "java/lang/Boolean", "java/lang/Character", "java/lang/Byte", "java/lang/Short", "java/lang/Integer",
      "java/lang/Float", "java/lang/Long", "java/lang/Double"
   };
   private static final String[] UNBOXING_METHOD = {
      null, "booleanValue", "charValue", "byteValue", "shortValue", "intValue", "floatValue", "longValue", "doubleValue"
   };
   private static final Type[] NO_ARGS = new Type[0];

   protected MethodVisitor mw;
   protected boolean useMockingBridge;
   private String modifiedClassName;

   protected BaseClassModifier(ClassReader classReader)
   {
      super(classReader, true);
   }

   protected final void setUseMockingBridge(ClassLoader classLoader)
   {
      useMockingBridge = classLoader != ClassLoader.getSystemClassLoader();
   }

   @Override
   public void visit(
      int version, int access, String name, String signature, String superName, String[] interfaces)
   {
      super.visit(version, access, name, signature, superName, interfaces);
      modifiedClassName = name;
   }

   /**
    * Just creates a new MethodWriter which will write out the method bytecode when visited.
    * <p/>
    * Removes any "abstract" or "native" modifiers for the modified version.
    */
   protected final void startModifiedMethodVersion(
      int access, String name, String desc, String signature, String[] exceptions)
   {
      //noinspection UnnecessarySuperQualifier
      mw = super.visitMethod(access & ACCESS_MASK, name, desc, signature, exceptions);

      if (Modifier.isNative(access)) {
         TestRun.mockFixture().addRedefinedClassWithNativeMethods(modifiedClassName);
      }
   }

   private boolean generateCodeToPassThisOrNullIfStaticMethod(int access)
   {
      boolean isStatic = Modifier.isStatic(access);
      generateCodeToPassThisOrNullIfStaticMethod(isStatic);
      return isStatic;
   }

   protected final void generateCodeToPassThisOrNullIfStaticMethod(boolean isStatic)
   {
      if (isStatic) {
         mw.visitInsn(ACONST_NULL);
      }
      else {
         mw.visitVarInsn(ALOAD, 0);
      }
   }

   protected final void generateCodeToPassMethodArgumentsAsVarargs(boolean isStatic, Type[] argTypes)
   {
      generateCodeToCreateArrayOfObject(argTypes.length);
      generateCodeToPassMethodArgumentsAsVarargs(argTypes, 0, isStatic ? 0 : 1);
   }

   private void generateCodeToCreateArrayOfObject(int arrayLength)
   {
      mw.visitIntInsn(BIPUSH, arrayLength);
      mw.visitTypeInsn(ANEWARRAY, "java/lang/Object");
   }

   private void generateCodeToPassMethodArgumentsAsVarargs(
      Type[] argTypes, int initialArrayIndex, int initialParameterIndex)
   {
      int i = initialArrayIndex;
      int j = initialParameterIndex;

      for (Type argType : argTypes) {
         mw.visitInsn(DUP);
         mw.visitIntInsn(BIPUSH, i++);
         mw.visitVarInsn(argType.getOpcode(ILOAD), j);

         int sort = argType.getSort();

         if (sort < Type.ARRAY) {
            String wrapperType = PRIMITIVE_WRAPPER_TYPE[sort];
            mw.visitMethodInsn(INVOKESTATIC, wrapperType, "valueOf", "(" + argType + ")L" + wrapperType + ';');
         }

         mw.visitInsn(AASTORE);

         j += argType.getSize();
      }
   }

   protected final void generateReturnWithObjectAtTopOfTheStack(String methodDesc)
   {
      Type returnType = Type.getReturnType(methodDesc);
      int sort = returnType.getSort();

      if (sort == Type.VOID) {
         mw.visitInsn(POP);
      }
      else if (sort == Type.ARRAY) {
         mw.visitTypeInsn(CHECKCAST, returnType.getDescriptor());
      }
      else if (sort == Type.OBJECT) {
         mw.visitTypeInsn(CHECKCAST, returnType.getInternalName());
      }
      else {
         String returnDesc = PRIMITIVE_WRAPPER_TYPE[sort];
         mw.visitTypeInsn(CHECKCAST, returnDesc);
         mw.visitMethodInsn(INVOKEVIRTUAL, returnDesc, UNBOXING_METHOD[sort], "()" + returnType);
      }

      mw.visitInsn(returnType.getOpcode(IRETURN));
   }

   protected final void generateDirectCallToHandler(
      String className, int access, String name, String desc, boolean withRealImpl)
   {
      // First argument: the mock instance, if any.
      boolean isStatic = generateCodeToPassThisOrNullIfStaticMethod(access);

      // Second argument: method access flags.
      mw.visitLdcInsn(access);

      // Third argument: class name.
      mw.visitLdcInsn(className);

      // Fourth argument: method signature.
      mw.visitLdcInsn(name + desc);

      // Fifth argument: whether real implementation is available or not
      mw.visitInsn(ICONST_0 + (withRealImpl ? 1 : 0));
      
      // Sixth argument: call arguments.
      Type[] argTypes = Type.getArgumentTypes(desc);
      generateCodeToPassMethodArgumentsAsVarargs(isStatic, argTypes);

      mw.visitMethodInsn(
         INVOKESTATIC, "mockit/internal/expectations/RecordAndReplayExecution", "recordOrReplay",
         "(Ljava/lang/Object;ILjava/lang/String;Ljava/lang/String;Z[Ljava/lang/Object;)Ljava/lang/Object;");
   }

   protected final void generateCallToMockingBridge(
      int targetId, String mockClassName, int mockAccess, String mockName, String mockDesc, Object extra)
   {
      generateCodeToInstantiateMockingBridge();

      // First and second "invoke" arguments:
      boolean isStatic = generateCodeToPassThisOrNullIfStaticMethod(mockAccess);
      mw.visitInsn(ACONST_NULL);

      // Create array for call arguments (third "invoke" argument):
      Type[] argTypes = mockDesc == null ? NO_ARGS : Type.getArgumentTypes(mockDesc);
      generateCodeToCreateArrayOfObject(5 + (extra == null ? 0 : 1) + argTypes.length);

      int i = 0;
      generateCodeToFillArrayElement(i++, targetId);
      generateCodeToFillArrayElement(i++, mockAccess);
      generateCodeToFillArrayElement(i++, mockClassName);
      generateCodeToFillArrayElement(i++, mockName);
      generateCodeToFillArrayElement(i++, mockDesc);

      if (extra != null) {
         generateCodeToFillArrayElement(i++, extra);
      }

      generateCodeToPassMethodArgumentsAsVarargs(argTypes, i, isStatic ? 0 : 1);
      generateCallToMockingBridge();
   }

   protected final void generateCodeToInstantiateMockingBridge()
   {
      mw.visitLdcInsn("mockit.internal.MockingBridge");
      mw.visitInsn(ICONST_1);
      mw.visitMethodInsn(INVOKESTATIC, "java/lang/ClassLoader", "getSystemClassLoader", "()Ljava/lang/ClassLoader;");
      mw.visitMethodInsn(
         INVOKESTATIC, "java/lang/Class", "forName", "(Ljava/lang/String;ZLjava/lang/ClassLoader;)Ljava/lang/Class;");
      mw.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "newInstance", "()Ljava/lang/Object;");
   }

   private void generateCodeToFillArrayElement(int arrayIndex, Object value)
   {
      mw.visitInsn(DUP);
      mw.visitInsn(ICONST_0 + arrayIndex);

      if (value == null) {
         mw.visitInsn(ACONST_NULL);
      }
      else if (value instanceof Integer) {
         mw.visitIntInsn(SIPUSH, (Integer) value);
         mw.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
      }
      else {
         mw.visitLdcInsn(value);
      }

      mw.visitInsn(AASTORE);
   }

   protected final void generateCallToMockingBridge()
   {
      mw.visitMethodInsn(
         INVOKEINTERFACE, "java/lang/reflect/InvocationHandler", "invoke",
         "(Ljava/lang/Object;Ljava/lang/reflect/Method;[Ljava/lang/Object;)Ljava/lang/Object;");
   }

   protected final String generateSuperConstructorArguments(Type[] paramTypes)
   {
      if (paramTypes == null || paramTypes.length == 0) {
         return "()V";
      }

      pushDefaultValuesForParameterTypes(paramTypes);

      return Type.getMethodDescriptor(Type.VOID_TYPE, paramTypes);
   }

   private void pushDefaultValuesForParameterTypes(Type[] paramTypes)
   {
      for (Type paramType : paramTypes) {
         pushDefaultValueForType(paramType);
      }
   }

   protected final void pushDefaultValuesForParameterTypes(String methodOrConstructorDesc)
   {
      if (!"()V".equals(methodOrConstructorDesc)) {
         pushDefaultValuesForParameterTypes(Type.getArgumentTypes(methodOrConstructorDesc));
      }
   }

   protected final void pushDefaultValueForType(Type type)
   {
      switch (type.getSort()) {
         case Type.VOID: break;
         case Type.BOOLEAN:
         case Type.CHAR:
         case Type.BYTE:
         case Type.SHORT:
         case Type.INT:    mw.visitInsn(ICONST_0); break;
         case Type.LONG:   mw.visitInsn(LCONST_0); break;
         case Type.FLOAT:  mw.visitInsn(FCONST_0); break;
         case Type.DOUBLE: mw.visitInsn(DCONST_0); break;
         case Type.ARRAY:  generateCreationOfEmptyArray(type); break;
         default:          mw.visitInsn(ACONST_NULL);
      }
   }

   private void generateCreationOfEmptyArray(Type arrayType)
   {
      int dimensions = arrayType.getDimensions();

      for (int dimension = 0; dimension < dimensions; dimension++) {
         mw.visitInsn(ICONST_0);
      }

      if (dimensions > 1) {
         mw.visitMultiANewArrayInsn(arrayType.getDescriptor(), dimensions);
         return;
      }

      Type elementType = arrayType.getElementType();
      int elementSort = elementType.getSort();

      if (elementSort == Type.OBJECT) {
         mw.visitTypeInsn(ANEWARRAY, elementType.getInternalName());
      }
      else {
         int typ = getArrayElementTypeCode(elementSort);
         mw.visitIntInsn(NEWARRAY, typ);
      }
   }

   private int getArrayElementTypeCode(int elementSort)
   {
      switch (elementSort) {
          case Type.BOOLEAN: return T_BOOLEAN;
          case Type.CHAR:    return T_CHAR;
          case Type.BYTE:    return T_BYTE;
          case Type.SHORT:   return T_SHORT;
          case Type.INT:     return T_INT;
          case Type.FLOAT:   return T_FLOAT;
          case Type.LONG:    return T_LONG;
          default:           return T_DOUBLE;
      }
   }

   protected final void generateEmptyImplementation(String desc)
   {
      Type returnType = Type.getReturnType(desc);
      pushDefaultValueForType(returnType);
      mw.visitInsn(returnType.getOpcode(IRETURN));
      mw.visitMaxs(1, 0);
   }

   protected final void generateEmptyImplementation()
   {
      mw.visitInsn(RETURN);
      mw.visitMaxs(1, 0);
   }

   protected final boolean isMethodFromObject(String name, String desc)
   {
      return
         "equals".equals(name)   && "(Ljava/lang/Object;)Z".equals(desc) ||
         "hashCode".equals(name) && "()I".equals(desc) ||
         "toString".equals(name) && "()Ljava/lang/String;".equals(desc) ||
         "finalize".equals(name) && "()V".equals(desc);
   }
}
