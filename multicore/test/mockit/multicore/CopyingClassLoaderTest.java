/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.multicore;

import static org.junit.Assert.*;
import org.junit.*;

import mockit.*;

public final class CopyingClassLoaderTest
{
   final CopyingClassLoader copyingCL = new CopyingClassLoader();

   @Test
   public void createCopyOfClassWithoutCausingDependenciesToBeLoaded() throws Exception
   {
      Class<?> original = A.class;

      Class<?> copy = copyingCL.getCopy(original.getName());

      assertNotSame(original, copy);
      assertSame(ClassLoader.getSystemClassLoader(), original.getClassLoader());
      assertSame(copyingCL, copy.getClassLoader());
      assertFalse(copyingCL.hasLoadedClass(B.class));
      assertNotNull(copy.getPackage());
   }

   @SuppressWarnings({"UnusedDeclaration"})
   public static class A
   {
      final B b = new B();

      public static boolean doSomething(int i) { return i > 0; }
      Class<?> getClassOfDependency() { return b.getClass(); }
   }

   public static class B
   {
      @SuppressWarnings({"UnusedDeclaration"})
      public boolean doSomething(int i) { return i > 0; }
   }

   @Test
   public void createCopyOfClassAndCauseDependencyToBeLoaded() throws Exception
   {
      Class<?> original = A.class;

      Class<?> copy = copyingCL.getCopy(original.getName());

      Object a = Deencapsulation.newInstance(copy);
      assertSame(copy, a.getClass());
      assertTrue(copyingCL.hasLoadedClass(B.class));

      Class<?> bClass = Deencapsulation.invoke(a, "getClassOfDependency");
      assertSame(B.class, bClass); // would be different if B was defined outside a "mockit" package
      assertEquals("B", bClass.getSimpleName());
   }
}
