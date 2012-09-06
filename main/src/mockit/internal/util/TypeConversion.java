/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.util;

import mockit.external.asm4.*;

import static mockit.external.asm4.Opcodes.*;

public final class TypeConversion
{
   private static final String[] PRIMITIVE_WRAPPER_TYPE = {
      null, "java/lang/Boolean", "java/lang/Character", "java/lang/Byte", "java/lang/Short", "java/lang/Integer",
      "java/lang/Float", "java/lang/Long", "java/lang/Double"
   };
   private static final String[] UNBOXING_METHOD = {
      null, "booleanValue", "charValue", "byteValue", "shortValue", "intValue", "floatValue", "longValue", "doubleValue"
   };

   public static void generateCastToObject(MethodVisitor mv, Type type)
   {
      int sort = type.getSort();

      if (sort < Type.ARRAY) {
         String wrapperType = PRIMITIVE_WRAPPER_TYPE[sort];
         mv.visitMethodInsn(INVOKESTATIC, wrapperType, "valueOf", "(" + type + ")L" + wrapperType + ';');
      }
   }

   public static void generateCastFromObject(MethodVisitor mv, Type toType)
   {
      int sort = toType.getSort();

      if (sort == Type.VOID) {
         mv.visitInsn(POP);
      }
      else if (sort == Type.ARRAY) {
         mv.visitTypeInsn(CHECKCAST, toType.getDescriptor());
      }
      else if (sort == Type.OBJECT) {
         mv.visitTypeInsn(CHECKCAST, toType.getInternalName());
      }
      else {
         String typeDesc = PRIMITIVE_WRAPPER_TYPE[sort];
         mv.visitTypeInsn(CHECKCAST, typeDesc);
         mv.visitMethodInsn(INVOKEVIRTUAL, typeDesc, UNBOXING_METHOD[sort], "()" + toType);
      }
   }
}
