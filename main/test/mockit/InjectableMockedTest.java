/*
 * JMockit Expectations & Verifications
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
package mockit;

import org.junit.*;
import static org.junit.Assert.*;

public final class InjectableMockedTest
{
   static final class ClassWithStaticInitializer1
   {
      static boolean classInitializationExecuted = true;
      static int doSomething() { return 1; }
   }

   @Test
   public void mockClassWithStaticInitializerAsInjectable(
      @Injectable @Mocked(stubOutClassInitialization = false) ClassWithStaticInitializer1 mock)
   {
      assertEquals(1, ClassWithStaticInitializer1.doSomething());
      assertTrue(ClassWithStaticInitializer1.classInitializationExecuted);
   }

   static final class ClassWithStaticInitializer2
   {
      static boolean classInitializationExecuted = true;
      static int doSomething() { return 2; }
   }

   @Test
   public void mockClassWithStaticInitializerAsInjectableButSpecifyStubbingOutOfStaticInitializer(
      @Injectable @Mocked(stubOutClassInitialization = true) ClassWithStaticInitializer2 mock)
   {
      assertEquals(2, ClassWithStaticInitializer2.doSomething());
      assertFalse(ClassWithStaticInitializer2.classInitializationExecuted);
   }

   @Test
   public void mockStaticMethodInInjectableMockedClass(
      @Injectable @Mocked("doSomething") ClassWithStaticInitializer1 mock)
   {
      assertEquals(0, ClassWithStaticInitializer1.doSomething());
   }

   static final class Collaborator
   {
      final int value;
      Collaborator() { value = 101; }
      Collaborator(int value) { this.value = value; }
      int doSomething(boolean b) { return b ? 1 : -1; }
   }

   @Test
   public void mockConstructorInInjectableMockedClass(@Injectable @Mocked({"(int)", "doSomething"}) Collaborator mock)
   {
      Collaborator collaborator = new Collaborator(123);
      assertEquals(0, collaborator.value);
      assertEquals(1, collaborator.doSomething(true));
      assertEquals(0, mock.doSomething(true));
   }

   @Test
   public void mockNextCreatedInstance(@Injectable @Mocked(capture = 1) final Collaborator mock)
   {
      new NonStrictExpectations()
      {
         {
            mock.doSomething(true); result = 2;
         }
      };

      Collaborator captured = new Collaborator();
      assertEquals(0, captured.value);
      assertEquals(0, captured.doSomething(false));
      assertEquals(2, captured.doSomething(true));

      new Verifications()
      {
         {
            mock.doSomething(anyBoolean); times = 2;
         }
      };

      Collaborator notMocked = new Collaborator();
      assertEquals(101, notMocked.value);
      assertEquals(-1, notMocked.doSomething(false));
      assertEquals(1, notMocked.doSomething(true));
   }

   @Test
   public void mockSeparatelyTheNextTwoCreatedInstances()
   {
      new NonStrictExpectations()
      {
         @Injectable @Capturing(maxInstances = 1) Collaborator mock1;
         @Injectable @Capturing(maxInstances = 1) Collaborator mock2;

         {
            mock1.doSomething(true); result = 1;
            mock2.doSomething(false); result = 2;
         }
      };

      Collaborator captured1 = new Collaborator();
      assertEquals(0, captured1.value);
      assertEquals(0, captured1.doSomething(false));
      assertEquals(1, captured1.doSomething(true));

      Collaborator captured2 = new Collaborator(123);
      assertEquals(0, captured2.value);
      assertEquals(2, captured2.doSomething(false));
      assertEquals(0, captured2.doSomething(true));

      Collaborator notMocked = new Collaborator();
      assertEquals(101, notMocked.value);
      assertEquals(-1, notMocked.doSomething(false));
      assertEquals(1, notMocked.doSomething(true));
   }
}
