package integrationTests;

public final class BooleanExpressions
{
   public boolean eval1(boolean x, boolean y, int z)
   {
      return x && (y || z > 0);
   }

   public boolean eval2(boolean x, boolean y, int z)
   {
      return x && (y || z > 0);
   }

   public boolean eval3(boolean x, boolean y, boolean z)
   {
      return x && (y || z);
   }

   public boolean eval4(boolean x, boolean y, boolean z)
   {
      return x && (y || z);
   }

   static boolean isSameTypeIgnoringAutoBoxing(Class<?> firstType, Class<?> secondType)
   {
      return
         firstType == secondType ||
         firstType.isPrimitive() && isWrapperOfPrimitiveType(firstType, secondType) ||
         secondType.isPrimitive() && isWrapperOfPrimitiveType(secondType, firstType);
   }

   static boolean isWrapperOfPrimitiveType(Class<?> primitiveType, Class<?> otherType)
   {
      return
         primitiveType == int.class && otherType == Integer.class ||
         primitiveType == long.class && otherType == Long.class ||
         primitiveType == double.class && otherType == Double.class ||
//         primitiveType == float.class && otherType == Float.class ||
         primitiveType == boolean.class && otherType == Boolean.class;
   }
}
