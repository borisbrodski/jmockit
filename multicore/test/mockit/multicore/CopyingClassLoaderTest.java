/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.multicore;

import org.junit.*;

import mockit.*;

public final class CopyingClassLoaderTest
{
   final CopyingClassLoader copyingCL = new CopyingClassLoader();

   @Test
   public void createCopyOfClassWithoutCausingDependenciesToBeLoaded() throws Exception
   {
      Class<?> original = A.class;

      Class<?> copy = copyingCL.getCopy(original);

      assert original != copy;
      assert original.getClassLoader() == ClassLoader.getSystemClassLoader();
      assert copy.getClassLoader() == copyingCL;
      assert !copyingCL.hasLoadedClass(B.class);
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

      Class<?> copy = copyingCL.getCopy(original);

      Object a = Deencapsulation.newInstance(copy);
      assert a.getClass() == copy;
      assert copyingCL.hasLoadedClass(B.class);

      Class<?> bClass = Deencapsulation.invoke(a, "getClassOfDependency");
      assert bClass != B.class;
      assert "B".equals(bClass.getSimpleName());
   }
}
