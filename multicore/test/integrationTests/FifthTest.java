/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package integrationTests;

import org.junit.*;

public final class FifthTest
{
   @Test
   public void slowTest1() throws Exception
   {
      TestedClass.doSomething(true);
      Thread.sleep(667);
   }

   @Test
   public void slowTest2() throws Exception
   {
      Thread.sleep(333);
      TestedClass.doSomething(false);
   }
}
