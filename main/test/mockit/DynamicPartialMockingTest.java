/*
 * JMockit Expectations
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

import java.util.*;

import org.junit.*;

import static org.junit.Assert.*;

public final class DynamicPartialMockingTest
{
   static class Collaborator
   {
      protected final int value;

      Collaborator() { value = -1; }
      Collaborator(int value) { this.value = value; }

      int getValue() { return value; }

      @SuppressWarnings({"UnusedDeclaration"})
      final boolean simpleOperation(int a, String b, Date c) { return true; }

      @SuppressWarnings({"UnusedDeclaration"})
      static void doSomething(boolean b, String s) { throw new IllegalStateException(); }
   }

   static final class SubCollaborator extends Collaborator
   {
      SubCollaborator() { this(1); }
      SubCollaborator(int value) { super(value); }

      String format() { return String.valueOf(value); }
   }

   interface Dependency
   {
      boolean doSomething();
      List<?> doSomethingElse(int n);
   }

   @Test
   public void dynamicallyMockAClass()
   {
      new Expectations(Collaborator.class)
      {
         {
            new Collaborator().getValue(); result = 123;
         }
      };

      // Mocked:
      Collaborator collaborator = new Collaborator();
      assertEquals(123, collaborator.getValue());

      // Not mocked:
      assertTrue(collaborator.simpleOperation(1, "b", null));
      assertEquals(45, new Collaborator(45).value);
   }

   @Test
   public void dynamicallyMockAMockedClass(@Mocked final Collaborator mock)
   {
      assertEquals(0, mock.value);

      new Expectations(mock)
      {
         {
            mock.getValue(); result = 123;
         }
      };

      // Mocked:
      assertEquals(123, mock.getValue());

      // Not mocked:
      Collaborator collaborator = new Collaborator();
      assertEquals(-1, collaborator.value);
      assertTrue(collaborator.simpleOperation(1, "b", null));
      assertEquals(45, new Collaborator(45).value);
   }

   @Test
   public void dynamicallyMockAnInstance()
   {
      final Collaborator collaborator = new Collaborator();

      new Expectations(collaborator)
      {
         {
            collaborator.getValue(); result = 123;
         }
      };

      // Mocked:
      assertEquals(123, collaborator.getValue());

      // Not mocked:
      assertTrue(collaborator.simpleOperation(1, "b", null));
      assertEquals(45, new Collaborator(45).value);
      assertEquals(-1, new Collaborator().value);
   }

   @Test
   public void dynamicallyMockAnInstanceWithNonStrictExpectations()
   {
      final Collaborator collaborator = new Collaborator(2);

      new NonStrictExpectations(collaborator)
      {
         {
            collaborator.simpleOperation(1, "", null); result = false;
            Collaborator.doSomething(true, "test");
         }
      };

      // Mocked:
      assertFalse(collaborator.simpleOperation(1, "", null));
      Collaborator.doSomething(true, "test");

      // Not mocked:
      assertEquals(2, collaborator.getValue());
      assertEquals(45, new Collaborator(45).value);
      assertEquals(-1, new Collaborator().value);

      try {
         Collaborator.doSomething(false, null);
         fail();
      }
      catch (IllegalStateException ignore) {}

      new Verifications()
      {
         {
            Collaborator.doSomething(anyBoolean, "test");
            collaborator.getValue(); times = 1;
            new Collaborator(45);
         }
      };
   }

   @Test(expected = IllegalStateException.class)
   public void dynamicallyMockASubCollaboratorInstance()
   {
      final SubCollaborator collaborator = new SubCollaborator();

      new NonStrictExpectations(collaborator)
      {
         {
            collaborator.getValue(); result = 5;
            new SubCollaborator().format(); result = "test";
         }
      };

      // Mocked:
      assertEquals(5, collaborator.getValue());
      assertEquals("test", collaborator.format());

      // Not mocked:
      assertTrue(collaborator.simpleOperation(0, null, null));
      Collaborator.doSomething(true, null); // will throw the IllegalStateException
   }

   @Test
   public void dynamicallyMockOnlyTheSubclass()
   {
      final SubCollaborator collaborator = new SubCollaborator();

      new NonStrictExpectations(SubCollaborator.class)
      {
         {
            collaborator.getValue();
            collaborator.format(); result = "test";
         }
      };

      // Mocked:
      assertEquals("test", collaborator.format());

      // Not mocked:
      assertEquals(1, collaborator.getValue());
      assertTrue(collaborator.simpleOperation(0, null, null));

      // Mocked sub-constructor/not mocked base constructor:
      assertEquals(-1, new SubCollaborator().value);

      new VerificationsInOrder()
      {
         {
            collaborator.format();
            new SubCollaborator();
         }
      };
   }

   @Test
   public void dynamicallyMockAnAnonymousClassInstanceThroughTheImplementedInterface()
   {
      final Collaborator collaborator = new Collaborator();

      final Dependency dependency = new Dependency()
      {
         public boolean doSomething() { return false; }
         public List<?> doSomethingElse(int n) { return null; }
      };
      
      new NonStrictExpectations(collaborator, dependency)
      {
         {
            collaborator.getValue(); result = 5;
            dependency.doSomething(); result = true;
         }
      };

      // Mocked:
      assertEquals(5, collaborator.getValue());
      assertTrue(dependency.doSomething());

      // Not mocked:
      assertTrue(collaborator.simpleOperation(0, null, null));
      assertNull(dependency.doSomethingElse(3));

      new FullVerifications()
      {
         {
            dependency.doSomething();
            collaborator.getValue();
            dependency.doSomethingElse(anyInt);
            collaborator.simpleOperation(0, null, null);
         }
      };
   }

   @Test
   public void dynamicallyMockJREClass()
   {
      final List<String> list = new LinkedList<String>();

      new NonStrictExpectations(list)
      {
         {
            list.get(1); result = "an item";
            list.size(); result = 2;
         }
      };

      // Use mocked methods:
      assertEquals(2, list.size());
      assertEquals("an item", list.get(1));

      // Use unmocked methods:
      assertTrue(list.add("another"));
      assertEquals("another", list.remove(0));
   }

   @Test
   public void attemptToUseDynamicMockingForInvalidTypes()
   {
      assertInvalidTypeForDynamicMocking(Runnable.class);
      assertInvalidTypeForDynamicMocking(Test.class);
      assertInvalidTypeForDynamicMocking(int[].class);
      assertInvalidTypeForDynamicMocking(new String[1]);
      assertInvalidTypeForDynamicMocking(char.class);
      assertInvalidTypeForDynamicMocking(123);
      assertInvalidTypeForDynamicMocking(Boolean.class);
      assertInvalidTypeForDynamicMocking(true);
      assertInvalidTypeForDynamicMocking(2.5);
   }

   private void assertInvalidTypeForDynamicMocking(Object classOrObject)
   {
      try {
         new Expectations(classOrObject) {};
         fail();
      }
      catch (IllegalArgumentException ignore) {}
   }

   @Test
   public void dynamicPartialMockingWithExactArgumentMatching()
   {
      final Collaborator collaborator = new Collaborator();

      new NonStrictExpectations(collaborator)
      {{
         collaborator.simpleOperation(1, "s", null); result = false;
      }};

      assertFalse(collaborator.simpleOperation(1, "s", null));
      assertTrue(collaborator.simpleOperation(2, "s", null));
      assertTrue(collaborator.simpleOperation(1, "S", null));
      assertTrue(collaborator.simpleOperation(1, "s", new Date()));
      assertTrue(collaborator.simpleOperation(1, null, new Date()));
      assertFalse(collaborator.simpleOperation(1, "s", null));

      new FullVerifications()
      {
         {
            collaborator.simpleOperation(anyInt, null, null);
         }
      };
   }

   @Test
   public void dynamicPartialMockingWithFlexibleArgumentMatching(final Collaborator mock)
   {
      new NonStrictExpectations(mock)
      {{
         mock.simpleOperation(anyInt, withPrefix("s"), null); result = false;
      }};

      Collaborator collaborator = new Collaborator();
      assertFalse(collaborator.simpleOperation(1, "sSs", null));
      assertTrue(collaborator.simpleOperation(2, " s", null));
      assertTrue(collaborator.simpleOperation(1, "S", null));
      assertFalse(collaborator.simpleOperation(-1, "s", new Date()));
      assertTrue(collaborator.simpleOperation(1, null, null));
      assertFalse(collaborator.simpleOperation(0, "string", null));
   }

   @Test
   public void dynamicPartialMockingWithOnInstanceMatching()
   {
      final Collaborator mock = new Collaborator();

      new NonStrictExpectations(mock)
      {{
         onInstance(mock).getValue(); result = 3;
      }};

      assertEquals(3, mock.getValue());
      assertEquals(4, new Collaborator(4).getValue());

      new FullVerificationsInOrder()
      {
         {
            onInstance(mock).getValue();
            mock.getValue();
         }
      };
   }
}
