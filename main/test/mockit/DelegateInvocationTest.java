/*
 * JMockit Expectations
 * Copyright (c) 2009 JMockit Developers
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

import java.util.Collection;

import org.junit.Test;

import static org.hamcrest.core.IsEqual.*;

import static org.junit.Assert.*;

public class DelegateInvocationTest {
   static class Collaborator {
      Collaborator() {
      }

      Collaborator(int i) {
      }

      int getValue() {
         return -1;
      }

      String doSomething(boolean b, int[] i, String s) {
         return s + b + i[0];
      }

      static boolean staticMethod() {
         return true;
      }

      static boolean staticMethod(int i) {
         return i > 0;
      }

      native long nativeMethod(boolean b);

      final char finalMethod() {
         return 's';
      }

      private float privateMethod() {
         return 1.2F;
      }

      void addElements(Collection<String> elements) {
         elements.add("one element");
      }
   }

   @Test
   public void testDelegateForStaticMethodWithContext() {
      new NonStrictExpectations() {
         final Collaborator unused = null;

         {
            Collaborator.staticMethod(-1);
            returns(new Delegate() {
               boolean staticMethod(Invocation context, int i) {
                  return context.getInvocationCount() + i > 0;
               }
            });
         }
      };

      assertFalse(Collaborator.staticMethod(-1));
      assertTrue(Collaborator.staticMethod(-1));
   }

   static class ConstructorDelegate implements Delegate {
      int capturedArgument;

      void $init(Invocation context, int i) {
         capturedArgument = i + context.getInvocationCount();
      }
   }

   @Test
   public void testDelegateForConstructorWithContext() {
      final ConstructorDelegate delegate = new ConstructorDelegate();

      new Expectations() {
         Collaborator mock;

         {
            new Collaborator(withAny(0));
            returns(delegate);
         }
      };

      new Collaborator(4);

      assertEquals(5, delegate.capturedArgument);
   }

   @Test
   public void testDelegateReceivingNullArguments() {
      new NonStrictExpectations() {
         Collaborator collaborator;

         {
            collaborator.doSomething(true, null, null);
            returns(new Delegate() {
               void doSomething(Invocation invocation, boolean b, int[] i,
                     String s) {
               }
            });
         }
      };

      assertNull(new Collaborator().doSomething(true, null, null));
   }

   @Test
   public void testDelegateForStaticMethodMultiWithContext() {
      new NonStrictExpectations() {
         final Collaborator unused = null;

         {
            Collaborator.staticMethod(-1);
            returns(new Delegate() {
               boolean staticMethod(Invocation context, int i) {
                  return context.getInvocationCount() + i > 0;
               }

               boolean otherMethod(float f) {
                  return f > 0;
               }
            });
         }
      };

      assertFalse(Collaborator.staticMethod(-1));
      assertTrue(Collaborator.staticMethod(-1));
   }

   static class CollaboratorNew {
      CollaboratorNew() {
      }

      static int staticMethod1(Object o, Exception e) {
         return -1;
      }
   }

   @Test
   public void testDelegateForStaticMethodWithObject() {
      new NonStrictExpectations() {
         final CollaboratorNew unused = null;

         {
            CollaboratorNew.staticMethod1(any, null);
            returns(new Delegate() {
               int staticMethod1(Object o, Exception e) {
                  return 1;
               }
            });
         }
      };

      assertThat(CollaboratorNew.staticMethod1(new Object(), new Exception()),
            equalTo(1));
      assertThat(CollaboratorNew.staticMethod1(new Object(), new Exception()),
            equalTo(1));
   }

   @Test
   public void testDelegateForStaticMethodWithObjectAndContext() {
      new NonStrictExpectations() {
         final CollaboratorNew unused = null;

         {
            CollaboratorNew.staticMethod1(any, null);
            repeatsAtMost(1);
            returns(new Delegate() {
               int staticMethod1(Invocation invocation, Object o, Exception e) {
                  invocation.setMinInvocations(2);
                  invocation.setMaxInvocations(2);
                  return invocation.getInvocationCount();
               }
            });
         }
      };

      assertThat(CollaboratorNew.staticMethod1(null, null), equalTo(1));
      assertThat(CollaboratorNew.staticMethod1(null, null), equalTo(2));

   }

   @Test(expected = IllegalArgumentException.class)
   public void testDelegateForStaticMethodSignatureMismatch() {
      new NonStrictExpectations() {
         final CollaboratorNew unused = null;

         {
            CollaboratorNew.staticMethod1(any, null);
            repeatsAtMost(1);
            returns(new Delegate() {
               int staticMethod1(Invocation invocation, Object o) {
                  return 1;
               }

               int someOtherMethod(float f) {
                  return 2;
               }
            });
         }
      };

      assertThat(CollaboratorNew.staticMethod1(null, null), equalTo(1));
   }

   public void testDelegateForStaticMethodDifferentName() {
      new NonStrictExpectations() {
         final CollaboratorNew unused = null;

         {
            CollaboratorNew.staticMethod1(any, null);
            repeatsAtMost(1);
            returns(new Delegate() {
               int differentName(Invocation invocation, Object o, Exception e) {
                  return 3;
               }
            });
         }
      };

      assertThat(CollaboratorNew.staticMethod1(null, null), equalTo(3));
   }

   public void testDelegateForStaticMethodDifferentNameTwoCalls() {
      new Expectations() {
         final CollaboratorNew unused = null;

         {
            CollaboratorNew.staticMethod1(any, null);
            repeatsAtMost(1);
            returns(new Delegate() {
               int differentName1(Invocation invocation, Object o, Exception e) {
                  return 3;
               }
            });
            CollaboratorNew.staticMethod1(any, null);
            repeatsAtMost(1);
            returns(new Delegate() {
               int differentName2(Invocation invocation, Object o, Exception e) {
                  return 4;
               }
            });
         }
      };

      assertThat(CollaboratorNew.staticMethod1(null, null), equalTo(3));
      assertThat(CollaboratorNew.staticMethod1(null, null), equalTo(4));
   }
}
