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

import org.junit.*;

import static org.junit.Assert.*;

public final class ExpectationsForConstructorsTest
{
   public static class BaseCollaborator
   {
      protected int value;

      protected BaseCollaborator() { value = -1; }
      protected BaseCollaborator(int value) { this.value = value; }

      protected boolean add(Integer i) { return i != null; }
   }

   static class Collaborator extends BaseCollaborator
   {
      Collaborator() {}
      Collaborator(int value) { super(value); }
   }

   @SuppressWarnings({"UnusedDeclaration"})
   public abstract static class AbstractCollaborator extends BaseCollaborator
   {
      protected AbstractCollaborator(int value) { super(value); }
      protected AbstractCollaborator(boolean b, int value) { super(value); }

      protected abstract void doSomething();
   }

   @Test
   public void mockAllConstructors()
   {
      new Expectations()
      {
         final Collaborator unused = null;

         {
            new Collaborator();
            new Collaborator(123);
         }
      };

      assertEquals(0, new Collaborator().value);
      assertEquals(0, new Collaborator(123).value);
   }

   @Test
   public void mockOnlyOneConstructorSpecifyingUseOfNoArgsSuperConstructor()
   {
      new Expectations()
      {
         @Mocked("(int): ()")
         final Collaborator unused = null;

         {
            new Collaborator(123);
         }
      };

      assertEquals(-1, new Collaborator().value);
      assertEquals(-1, new Collaborator(123).value);
   }

   @Test
   public void mockOnlyNoArgsConstructorSpecifyingUseOfSuperConstructorWithArgs()
   {
      new Expectations()
      {
         @Mocked("(): (int)")
         final Collaborator unused = null;

         {
            new Collaborator();
         }
      };

      assertEquals(0, new Collaborator().value);
      assertEquals(123, new Collaborator(123).value);
   }

   @Test
   public void partiallyMockAbstractClass(final AbstractCollaborator mock)
   {
      new Expectations()
      {
         {
            mock.doSomething();
         }
      };

      mock.doSomething();
   }

   @Test
   public void mockSubclassSpecifyingConstructorArgsMethod()
   {
      new Expectations()
      {
         @Mocked(methods = "add", constructorArgsMethod = "getConstructorArguments")
         Collaborator mock;

         {
            mock.add(5); result = true;
         }

         @SuppressWarnings({"UnusedDeclaration"})
         private Object[] getConstructorArguments(int value)
         {
            return new Object[] {100};
         }
      };

      assertTrue(new Collaborator().add(5));
   }

   @Test(expected = IllegalArgumentException.class)
   public void mockSubclassSpecifyingNonExistentConstructorArgsMethod()
   {
      new Expectations()
      {
         @Mocked(constructorArgsMethod = "nonExistent")
         Collaborator mock;
      };
   }

   @Ignore @Test
   public void mockAbstractClassSpecifyingConstructorArgsMethod(
      @Mocked(methods = "doSomething", constructorArgsMethod = "getConstructorArguments")
      final AbstractCollaborator mock)
   {
      new Expectations()
      {
         {
            mock.doSomething();
         }
      };

      mock.doSomething();
   }

   @SuppressWarnings({"UnusedDeclaration"})
   private Object[] getConstructorArguments(boolean b, int value)
   {
      return new Object[] {true, 100};
   }
}
