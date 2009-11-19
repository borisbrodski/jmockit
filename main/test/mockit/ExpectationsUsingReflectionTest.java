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

@SuppressWarnings({"ClassWithTooManyMethods"})
public final class ExpectationsUsingReflectionTest
{
   interface BusinessInterface
   {
      void doOperation();
   }

   @SuppressWarnings({"UnusedDeclaration"})
   static class Collaborator
   {
      static String xyz;
      static Collection<?> items;
      static List<?> items2;

      private int value;
      private Integer value2;
      private String value3;

      Collaborator() {}

      Collaborator(int value) { this.value = value; }

      Collaborator(int value, Integer value2, String value3)
      {
         this.value = value; this.value2 = value2; this.value3 = value3;
      }

      private static String doInternal() { return "123"; }

      void setValue(int value) { this.value = value; }

      final void simpleOperation(int a, String b, Date c) {}

      void doBusinessOperation(BusinessInterface operation) { operation.doOperation(); }

      private final class Inner
      {
         private final String s;

         Inner() { s = null; }
         Inner(String s, boolean b) { this.s = s; }
      }
   }

   // Just to have a mock field so no empty expectation blocks exist.
   @Mocked Runnable unused;

   @Test
   public void expectInstanceMethodInvocation(final Collaborator mock)
   {
      new Expectations()
      {
         {
            invoke(mock, "setValue", 2);
         }
      };

      mock.setValue(2);
   }

   @Test
   public void expectStaticMethodInvocation()
   {
      new Expectations()
      {
         final Collaborator mock = null;

         {
            invoke(Collaborator.class, "doInternal"); returns("test");
         }
      };

      assertEquals("test", Collaborator.doInternal());
   }

   @Test
   public void expectMethodInvocationWithProxyArgument(
      final Collaborator mock, final BusinessInterface proxyArg)
   {
      new Expectations()
      {
         {
            invoke(mock, "doBusinessOperation", proxyArg);
         }
      };

      mock.doBusinessOperation(proxyArg);
   }

   @Test
   public void setInstanceFieldByName()
   {
      final Collaborator collaborator = new Collaborator();

      new Expectations()
      {
         {
            setField(collaborator, "value", 123);
         }
      };

      assertEquals(123, collaborator.value);
   }

   @Test
   public void setInstanceFieldBy()
   {
      final Collaborator collaborator = new Collaborator();
      collaborator.value3 = "";

      new Expectations()
      {
         {
            setField(collaborator, "test");
         }
      };

      assertEquals("test", collaborator.value3);
   }

   @Test(expected = IllegalArgumentException.class)
   public void setInstanceFieldByTypeWhenNoCompatibleFieldExists()
   {
      final Collaborator collaborator = new Collaborator();

      new Expectations()
      {
         {
            setField(collaborator, 'X');
         }
      };
   }

   @Test(expected = IllegalArgumentException.class)
   public void setInstanceFieldByTypeWhenMultipleCompatibleFieldsExist()
   {
      final Collaborator collaborator = new Collaborator();

      new Expectations()
      {
         {
            setField(collaborator, 56);
         }
      };
   }

   @Test
   public void setStaticFieldByName()
   {
      new Expectations()
      {
         {
            setField(Collaborator.class, "xyz", "test");
         }
      };

      assertEquals("test", Collaborator.xyz);
   }

   @Test
   public void setStaticFieldByType()
   {
      new Expectations()
      {
         {
            setField(Collaborator.class, "static");
         }
      };

      assertEquals("static", Collaborator.xyz);
   }

   @Test(expected = IllegalArgumentException.class)
   public void setStaticFieldByTypeWhenNoCompatibleFieldExists()
   {
      new Expectations()
      {
         {
            setField(Collaborator.class, 123);
         }
      };
   }

   @Test(expected = IllegalArgumentException.class)
   public void setStaticFieldByTypeWhenMultipleCompatibleFieldsExist()
   {
      new Expectations()
      {
         {
            setField(Collaborator.class, Collections.<Object>emptyList());
         }
      };
   }

   @Test
   public void getInstanceFieldByName(final Collaborator mock)
   {
      mock.value = 123;

      new Expectations()
      {
         {
            assertEquals(123, getField(mock, "value"));
         }
      };
   }

   @Test
   public void getInstanceFieldByType(final Collaborator mock)
   {
      mock.value3 = "test";

      new Expectations()
      {
         {
            assertEquals("test", getField(mock, String.class));
         }
      };
   }


   @Test(expected = IllegalArgumentException.class)
   public void getInstanceFieldByTypeWhenNoCompatibleFieldExists(final Collaborator mock)
   {
      new Expectations()
      {
         {
            getField(mock, char.class);
         }
      };
   }

   @Test(expected = IllegalArgumentException.class)
   public void getInstanceFieldByTypeWhenMultipleCompatibleFieldsExist(final Collaborator mock)
   {
      new Expectations()
      {
         {
            getField(mock, int.class);
         }
      };
   }

   @Test
   public void getStaticFieldByName()
   {
      Collaborator.xyz = "test";

      new Expectations()
      {
         {
            assertEquals("test", getField(Collaborator.class, "xyz"));
         }
      };
   }

   @Test
   public void getStaticFieldByType()
   {
      Collaborator.xyz = "static";

      new Expectations()
      {
         {
            assertEquals("static", getField(Collaborator.class, String.class));
         }
      };
   }

   @Test(expected = IllegalArgumentException.class)
   public void getStaticFieldByTypeWhenNoCompatibleFieldExists()
   {
      new Expectations()
      {
         {
            getField(Collaborator.class, Integer.class);
         }
      };
   }

   @Test(expected = IllegalArgumentException.class)
   public void getStaticFieldByTypeWhenMultipleCompatibleFieldsExist()
   {
      new Expectations()
      {
         {
            getField(Collaborator.class, Collection.class);
         }
      };
   }

   @Test
   public void createNewInstanceWithExplicitParameterTypes()
   {
      new Expectations()
      {
         private final String className = Collaborator.class.getName();

         {
            Class<?>[] paramTypes = {int.class};
            Collaborator c1 = newInstance(className, paramTypes, 1);
            assertEquals(1, c1.value);

            Class<?>[] paramTypes2 = {int.class, Integer.class, String.class};
            Collaborator c2 = newInstance(className, paramTypes2, 1, 2, "3");
            assertEquals(1, c2.value);
            assertSame(2, c2.value2);
            assertEquals("3", c2.value3);

            Collaborator c3 = newInstance(className, paramTypes2, 0, null, null);
            assertEquals(0, c3.value);
            assertNull(c3.value2);
            assertNull(c3.value3);
         }
      };
   }

   @Test
   public void createNewInstanceWithNonNullArguments()
   {
      new Expectations()
      {
         private final String className = Collaborator.class.getName();

         {
            Collaborator c1 = newInstance(className, 1);
            assertEquals(1, c1.value);

            Collaborator c2 = newInstance(className, 1, 2, "3");
            assertEquals(1, c2.value);
            assertSame(2, c2.value2);
            assertEquals("3", c2.value3);

            Collaborator c3 = newInstance(className, 0, Integer.class, String.class);
            assertEquals(0, c3.value);
            assertNull(c3.value2);
            assertNull(c3.value3);
         }
      };
   }

   @Test(expected = IllegalArgumentException.class)
   public void createNewInstanceWithNonNullArgumentsButActuallyPassingNulls()
   {
      new Expectations()
      {
         {
            newInstance(Collaborator.class.getName(), 0, null, null);
         }
      };
   }

   @Test
   public void createNewInnerInstance()
   {
      final Collaborator collaborator = new Collaborator();

      new Expectations()
      {
         private final String className = Collaborator.Inner.class.getSimpleName();

         {
            Collaborator.Inner c1 = newInnerInstance(className, collaborator);
            assertNull(c1.s);

            Collaborator.Inner c2 = newInnerInstance(className, collaborator, "test", true);
            assertEquals("test", c2.s);
         }
      };
   }
}
