/*
 * JMockit
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
package mockit.internal.util;

import java.lang.reflect.*;
import java.util.*;

import static java.util.Collections.*;

/**
 * Provides default values for each type, typically used for returning default values according to
 * method return types.
 */
public final class DefaultValues
{
   private DefaultValues() {}

   private static final Map<String, Object> TYPE_DESC_TO_VALUE_MAP = new HashMap<String, Object>()
   {
      {
         put("Z", false);
         put("C", '\0');
         put("B", (byte) 0);
         put("S", (short) 0);
         put("I", 0);
         put("F", 0.0F);
         put("J", 0L);
         put("D", 0.0);
         put("Ljava/util/Collection;", EMPTY_LIST);
         put("Ljava/util/List;", EMPTY_LIST);
         put("Ljava/util/Set;", EMPTY_SET);
         put("Ljava/util/SortedSet;", unmodifiableSortedSet(new TreeSet<Object>()));
         put("Ljava/util/Map;", EMPTY_MAP);
         put("Ljava/util/SortedMap;", unmodifiableSortedMap(new TreeMap<Object, Object>()));
      }
   };

   private static final Map<String, Object> ELEM_TYPE_TO_ONE_D_ARRAY = new HashMap<String, Object>()
   {
      {
         put("[Z", new boolean[0]);
         put("[C", new char[0]);
         put("[B", new byte[0]);
         put("[S", new short[0]);
         put("[I", new int[0]);
         put("[F", new float[0]);
         put("[J", new long[0]);
         put("[D", new double[0]);
         put("[Ljava/lang/Object;", new Object[0]);
         put("[Ljava/lang/String;", new String[0]);
      }
   };

   public static Object computeForReturnType(String methodNameAndDesc)
   {
      int rightParen = methodNameAndDesc.indexOf(')') + 1;
      String typeDesc = methodNameAndDesc.substring(rightParen);

      return computeForType(typeDesc);
   }

   public static Object computeForType(String typeDesc)
   {
      char typeDescChar = typeDesc.charAt(0);

      if (typeDescChar == 'V') {
         return null;
      }

      Object defaultValue = TYPE_DESC_TO_VALUE_MAP.get(typeDesc);

      if (defaultValue != null) {
         return defaultValue;
      }

      if (typeDescChar == 'L') {
         return null;
      }

      // It's an array.
      Object emptyArray = ELEM_TYPE_TO_ONE_D_ARRAY.get(typeDesc);

      if (emptyArray == null) {
         emptyArray = newEmptyArray(typeDesc);
      }

      return emptyArray;
   }

   private static Object newEmptyArray(String typeDesc)
   {
      mockit.external.asm.Type type = mockit.external.asm.Type.getType(typeDesc);
      Class<?> elementType = Utilities.getClassForType(type.getElementType());

      return Array.newInstance(elementType, new int[type.getDimensions()]);
   }

   public static Object computeForType(Class<?> type)
   {
      if (type.isArray()) {
         return Array.newInstance(type.getComponentType(), 0);
      }
      else if (type != void.class && type.isPrimitive()) {
         return defaultValueForPrimitiveType(type);
      }

      return null;
   }

   private static Object defaultValueForPrimitiveType(Class<?> type)
   {
      if (type == int.class) {
         return 0;
      }
      else if (type == boolean.class) {
         return false;
      }
      else if (type == long.class) {
         return 0L;
      }
      else if (type == double.class) {
         return 0.0;
      }
      else if (type == float.class) {
         return 0.0F;
      }
      else if (type == char.class) {
         return '\0';
      }
      else if (type == byte.class) {
         return (byte) 0;
      }
      else {
         return (short) 0;
      }
   }
}
