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

   static class Collaborator
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

   final class SubCollaborator1 extends Collaborator {}

   final class SubCollaborator2 extends Collaborator
   {
      int doSomething()
      {
         new SubCollaborator1().doSomething(true);
         return doSomething(true);
      }
   }

   @Test
   public void mockInheritedMethodInCapturedInstanceOfOneSubclassButNotInAnother()
   {
      new Expectations()
      {
         @Injectable @Mocked(capture = 1)
         SubCollaborator1 capturedInstance;

         {
            capturedInstance.doSomething(true);
         }
      };

      assertEquals(1, new SubCollaborator2().doSomething());
   }

   @Test
   public void mockSeparatelyTheNextTwoCreatedInstances()
   {
      new NonStrictExpectations()
      {
         @Injectable @Capturing(maxInstances = 1) Collaborator mock1;
         @Injectable @Capturing(maxInstances = 1) Collaborator mock2;

         {
            mock1.doSomething(true); result = 10;
            mock2.doSomething(false); result = 20;
         }
      };

      Collaborator captured1 = new Collaborator();
      assertEquals(0, captured1.value);
      assertEquals(0, captured1.doSomething(false));
      assertEquals(10, captured1.doSomething(true));

      Collaborator captured2 = new Collaborator(123);
      assertEquals(0, captured2.value);
      assertEquals(20, captured2.doSomething(false));
      assertEquals(0, captured2.doSomething(true));

      Collaborator notMocked = new Collaborator();
      assertEquals(101, notMocked.value);
      assertEquals(-1, notMocked.doSomething(false));
      assertEquals(1, notMocked.doSomething(true));
   }
   
   @Test
   public void mockSeparatelyTwoGroupsOfInternallyCreatedInstancesUsingMockParameters(
      @Injectable @Mocked(capture = 2) final Collaborator mock1,
      @Injectable @Mocked(capture = 3) final Collaborator mock2)
   {
      new NonStrictExpectations()
      {
         {
            mock1.doSomething(false); result = -45;
            mock2.doSomething(true); result = 123;
         }
      };

      // First two instances created and captured in code under test (mock1):
      assertEquals(0, new Collaborator(4) {}.doSomething(true));
      assertEquals(-45, new Collaborator().doSomething(false));

      // Next three instances created and captured in code under test (mock2):
      assertEquals(123, new Collaborator() {}.doSomething(true));
      assertEquals(123, new Collaborator(12).doSomething(true));
      assertEquals(0, new Collaborator(-5).doSomething(false));
      
      // Further instances not captured:
      assertEquals(1, new Collaborator().doSomething(true));
      assertEquals(-1, new Collaborator(2) {}.doSomething(false));

      new Verifications()
      {
         {
            mock1.doSomething(anyBoolean); times = 2;
         }
      };
   }

   @Test
   public void mockSeparatelyTwoGroupsOfInternallyCreatedInstancesUsingLocalMockFields()
   {
      new Expectations()
      {
         @Injectable @NonStrict @Mocked(capture = 2) Collaborator mock1;
         @Injectable @NonStrict @Capturing Collaborator mock2;

         {
            mock2.doSomething(anyBoolean); result = 123;
            mock1.doSomething(false); result = -45;
         }
      };

      // First two instances created and captured in code under test (mock1):
      assertEquals(0, new Collaborator(4).doSomething(true));
      assertEquals(-45, new Collaborator() {}.doSomething(false));

      // All other instances created and captured in code under test (mock2):
      assertEquals(123, new Collaborator().doSomething(true));
      assertEquals(123, new Collaborator(-5) {}.doSomething(false));
   }

   static class AnotherCollaborator
   {
      final int value;
      AnotherCollaborator() { value = 101; }
      AnotherCollaborator(int value) { this.value = value; }
      int doSomething(boolean b) { return b ? 1 : -1; }
   }

   static final class SubclassOfAnotherCollaborator extends AnotherCollaborator
   {
      SubclassOfAnotherCollaborator() {}
      SubclassOfAnotherCollaborator(int value) { throw new IllegalArgumentException("Bad value: " + value); }

      @Override
      int doSomething(boolean b) { return -super.doSomething(b); }
   }

   @Injectable @Mocked(capture = 2) AnotherCollaborator anotherMock1;
   @Injectable @Mocked(capture = 1) AnotherCollaborator anotherMock2;

   @Test
   public void mockSeparatelyTwoGroupsOfInternallyCreatedInstancesUsingMockFields()
   {
      new NonStrictExpectations()
      {
         {
            anotherMock1.doSomething(true); result = -45;
            anotherMock2.doSomething(true); result = 123;
            anotherMock2.doSomething(false); result = 246;
         }
      };

      // First two instances created and captured in code under test (anotherMock1):
      assertEquals(-45, new AnotherCollaborator(4) {}.doSomething(true));
      assertEquals(0, new SubclassOfAnotherCollaborator().doSomething(false));

      // Next instance created and captured in code under test (anotherMock2):
      AnotherCollaborator instance3 = new AnotherCollaborator();
      assertEquals(123, instance3.doSomething(true));
      assertEquals(246, instance3.doSomething(false));

      // Further instances not captured:
      assertEquals(1, new AnotherCollaborator().doSomething(true));
      assertEquals(-1, new AnotherCollaborator(2) {}.doSomething(false));
      assertEquals(1, new SubclassOfAnotherCollaborator().doSomething(false));

      try {
         new SubclassOfAnotherCollaborator(4560);
         fail();
      }
      catch (IllegalArgumentException ignore) {}

      new FullVerifications()
      {
         {
            anotherMock1.doSomething(anyBoolean); times = 2;
            anotherMock2.doSomething(true);
            anotherMock2.doSomething(false);
         }
      };
   }
}
