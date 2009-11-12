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
import java.io.*;

import static org.junit.Assert.*;
import org.junit.*;

interface Dependency
{
   String doSomething(boolean b);
}

public final class ExpectationsUsingMockedTest<MultiMock extends Dependency & Runnable>
{
   @Mocked private MultiMock multiMock;

   static class Collaborator
   {
      private int value;

      Collaborator() {}
      Collaborator(int value) { this.value = value; }

      void provideSomeService() {}

      int getValue() { return value; }

      @SuppressWarnings({"UnusedDeclaration"})
      final void simpleOperation(int a, String b, Date c) {}
   }

   static final class DependencyImpl implements Dependency
   {
      public String doSomething(boolean b) { return ""; }
   }

   @Mocked("do.*") private DependencyImpl mockDependency;

   public abstract static class AbstractBase
   {
      protected abstract boolean add(Integer i);
   }

   @NonStrict private AbstractBase base;

   @Test
   public void annotatedField()
   {
      new Expectations()
      {
         @Mocked
         private Collaborator mock;

         {
            new Collaborator().getValue();
         }
      };

      new Collaborator().getValue();
   }

   @Test
   public void annotatedMockFieldWithFilters()
   {
      new Expectations()
      {
         @Mocked({"(int)", "doInternal()", "[gs]etValue", "complexOperation(Object)"})
         Collaborator mock;

         {
            mock.getValue();
         }
      };

      // Calls the real method, not a mock.
      Collaborator collaborator = new Collaborator();
      collaborator.provideSomeService();

      // Calls the mock method.
      collaborator.getValue();
   }

   @Test
   public void annotatedMockFieldWithInverseFilters()
   {
      new Expectations()
      {
         @Mocked(
            inverse = true,
            methods = {"(int)", "simpleOperation(int, String, java.util.Date)", "setValue(long)"})
         Collaborator mock;

         {
            mock.provideSomeService();
         }
      };

      Collaborator collaborator = new Collaborator(2);
      collaborator.simpleOperation(1, "", null); // calls real method
      collaborator.provideSomeService(); // calls the mock
   }

   @Test(expected = IllegalArgumentException.class)
   public void annotatedFieldWithInvalidFilter()
   {
      new Expectations()
      {
         @Mocked("setValue(int")
         Collaborator mock;
      };
   }

   @Test
   public void annotatedParameter(@Mocked final List<Integer> mock)
   {
      new Expectations()
      {
         {
            mock.get(1);
         }
      };

      assertNull(mock.get(1));
   }

   @Test
   public void annotatedFieldAndParameter(@NonStrict final Dependency dependency1)
   {
      new Expectations()
      {
         @NonStrict private Dependency dependency2;

         {
            dependency1.doSomething(true); returns("1");
            dependency2.doSomething(false); returns("2");
         }
      };

      assertEquals("1", dependency1.doSomething(true));
      assertEquals("2", dependency1.doSomething(false));
   }

   @Test
   public void mockFieldWithTwoInterfaces()
   {
      new NonStrictExpectations()
      {
         {
            multiMock.doSomething(false); returns("test");
         }
      };

      multiMock.run();
      assertEquals("test", multiMock.doSomething(false));

      new Verifications()
      {
         {
            multiMock.run();
         }
      };
   }

   @Test
   public <M extends Dependency & Serializable> void mockParameterWithTwoInterfaces(final M mock)
   {
      new Expectations()
      {
         {
            mock.doSomething(true); returns("");
         }
      };

      assertEquals("", mock.doSomething(true));
   }

   @Test
   public void mockFinalFieldOfInterfaceTypeWithSpecifiedRealClassName()
   {
      new NonStrictExpectations()
      {
         @Mocked(realClassName = "mockit.ExpectationsUsingMockedTest$DependencyImpl")
         final Dependency mock = new DependencyImpl();

         {
            mock.doSomething(false);
         }
      };
   }

   @Test(expected = IllegalArgumentException.class)
   public void mockFinalFieldOfInterfaceTypeWithoutRealClassName()
   {
      new NonStrictExpectations()
      {
         final Dependency mock = null;
      };
   }

   @Test
   public void mockFieldForAbstractClass()
   {
      new Expectations()
      {
         {
            base.add(1); returns(true);
         }
      };

      assertFalse(base.add(0));
      assertTrue(base.add(1));
      assertFalse(base.add(2));
   }

   @Test
   public void partialMockingOfConcreteClassThatExcludesConstructors()
   {
      new Expectations()
      {
         {
            mockDependency.doSomething(withAny(true)); repeatsAtLeast(2);
         }
      };

      mockDependency.doSomething(true);
      mockDependency.doSomething(false);
      mockDependency.doSomething(true);
   }
}
