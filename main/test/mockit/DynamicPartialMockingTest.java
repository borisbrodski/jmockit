/*
 * JMockit Expectations
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
            new Collaborator().getValue(); returns(123);
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
            mock.getValue(); returns(123);
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
            collaborator.getValue(); returns(123);
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
            collaborator.simpleOperation(1, "", null); returns(false);
            Collaborator.doSomething(true, "test");
         }
      };

      // Mocked:
      assertFalse(collaborator.simpleOperation(1, "", null));
      Collaborator.doSomething(false, null);

      // Not mocked:
      assertEquals(2, collaborator.getValue());
      assertEquals(45, new Collaborator(45).value);
      assertEquals(-1, new Collaborator().value);
   }

   @Test(expected = IllegalStateException.class)
   public void dynamicallyMockASubCollaboratorInstance()
   {
      final SubCollaborator collaborator = new SubCollaborator();

      new NonStrictExpectations(collaborator)
      {
         {
            collaborator.getValue(); returns(5);
            new SubCollaborator().format(); returns("test");
         }
      };

      // Mocked:
      assertEquals(5, collaborator.getValue());
      assertEquals("test", collaborator.format());

      // Not mocked:
      assertTrue(collaborator.simpleOperation(0, null, null));
      Collaborator.doSomething(true, null); // will throw the IllegalStateException

      // Mocked sub-constructor/not mocked base constructor:
      assertEquals(-1, new SubCollaborator().value);
   }

   @Test
   public void dynamicallyMockOnlyTheSubclass()
   {
      final SubCollaborator collaborator = new SubCollaborator();

      new NonStrictExpectations(SubCollaborator.class)
      {
         {
            collaborator.getValue();
            collaborator.format(); returns("test");
         }
      };

      // Mocked:
      assertEquals("test", collaborator.format());

      // Not mocked:
      assertEquals(-1, collaborator.getValue());
      assertTrue(collaborator.simpleOperation(0, null, null));

      // Mocked sub-constructor/not mocked base constructor:
      assertEquals(-1, new SubCollaborator().value);
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
            collaborator.getValue(); returns(5);
            dependency.doSomething(); returns(true);
         }
      };

      // Mocked:
      assertEquals(5, collaborator.getValue());
      assertTrue(dependency.doSomething());

      // Not mocked:
      assertTrue(collaborator.simpleOperation(0, null, null));
      assertNull(dependency.doSomethingElse(3));
   }

   @Test
   public void dynamicallyMockJREClass()
   {
      final List<String> list = new LinkedList<String>();

      new NonStrictExpectations(list)
      {
         {
            list.get(1); returns("an item");
            list.size(); returns(2);
         }
      };

      // Use mocked methods:
      assertEquals(2, list.size());
      assertEquals("an item", list.get(1));

      // Use unmocked methods:
      assertTrue(list.add("another"));
      assertEquals("another", list.remove(0));
   }
}
