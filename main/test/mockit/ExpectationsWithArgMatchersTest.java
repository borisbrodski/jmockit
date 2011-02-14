/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import java.security.cert.*;
import java.util.*;

import org.hamcrest.*;
import org.hamcrest.core.*;
import org.junit.*;

public final class ExpectationsWithArgMatchersTest
{
   @SuppressWarnings({"UnusedDeclaration"})
   static class Collaborator
   {
      void setValue(int value) {}
      void setValue(double value) {}
      void setValue(float value) {}

      List<?> complexOperation(Object input1, Object... otherInputs)
      {
         return input1 == null ? Collections.emptyList() : Arrays.asList(otherInputs);
      }

      final void simpleOperation(int a, String b, Date c) {}

      void setValue(Certificate cert) {}
   }

   @Mocked Collaborator mock;

   @Test(expected = AssertionError.class)
   public void replayWithUnexpectedMethodArgument()
   {
      new Expectations()
      {
         {
            mock.simpleOperation(2, "test", null);
         }
      };

      mock.simpleOperation(2, "other", null);
   }

   @Test(expected = AssertionError.class)
   public void replayWithUnexpectedNullArgument()
   {
      new Expectations()
      {
         {
            mock.simpleOperation(2, "test", null);
         }
      };

      mock.simpleOperation(2, null, null);
   }

   @Test(expected = AssertionError.class)
   public void replayWithUnexpectedMethodArgumentUsingMatcher()
   {
      new Expectations()
      {
         {
            mock.setValue(withEqual(-1));
         }
      };

      mock.setValue(1);
   }

   @Test(expected = AssertionError.class)
   public void expectInvocationWithDifferentThanExpectedProxyArgument()
   {
      new Expectations()
      {
         Runnable mock2;

         {
            mock.complexOperation(mock2);
         }
      };

      mock.complexOperation(null);
   }

   @Test
   public void expectInvocationWithAnyArgument()
   {
      new Expectations()
      {
         {
            mock.setValue(anyInt);
         }
      };

      mock.setValue(3);
   }

   @Test
   public void expectInvocationWithEqualArgument()
   {
      new Expectations()
      {
         {
            mock.setValue(withEqual(3));
         }
      };

      mock.setValue(3);
   }

   @Test
   public void expectInvocationWithEqualDoubleArgument()
   {
      new Expectations()
      {
         {
            mock.setValue(withEqual(3.0, 0.01)); times = 3;
         }
      };

      mock.setValue(3.0);
      mock.setValue(3.01);
      mock.setValue(2.99);
   }

   @Test
   public void expectInvocationWithEqualFloatArgument()
   {
      new Expectations()
      {
         {
            mock.setValue(withEqual(3.0F, 0.01)); times = 3;
         }
      };

      mock.setValue(3.0F);
      mock.setValue(3.01F);
      mock.setValue(2.99F);
   }

   @Test(expected = AssertionError.class)
   public void expectInvocationWithEqualFloatArgumentButWithDifferentReplayValue()
   {
      new Expectations()
      {
         {
            mock.setValue(withEqual(3.0F, 0.01));
         }
      };

      mock.setValue(3.02F);
   }

   @Test
   public void expectInvocationWithNotEqualArgument()
   {
      new Expectations()
      {
         {
            mock.setValue(withNotEqual(3));
         }
      };

      mock.setValue(4);
   }

   @Test
   public void expectInvocationWithInstanceOfClassFromGivenObject()
   {
      new Expectations()
      {
         {
            mock.complexOperation("string");
            mock.complexOperation(withInstanceLike("string"));
         }
      };

      mock.complexOperation("string");
      mock.complexOperation("another string");
   }

   @Test
   public void expectInvocationWithInstanceOfGivenClass()
   {
      new Expectations()
      {
         {
            mock.complexOperation(withInstanceOf(Long.class));
         }
      };

      mock.complexOperation(5L);
   }

   @Test
   public void expectInvocationWithNullArgument()
   {
      new Expectations()
      {
         {
            mock.complexOperation(withNull());
         }
      };

      mock.complexOperation(null);
   }

   @Test
   public void expectInvocationWithNotNullArgument()
   {
      new Expectations()
      {
         {
            mock.complexOperation(withNotNull());
         }
      };

      mock.complexOperation(true);
   }

   @Test
   public void expectInvocationWithSameInstance()
   {
      new Expectations()
      {
         {
            mock.complexOperation(withSameInstance(45L));
         }
      };

      mock.complexOperation(45L);
   }

   @Test(expected = AssertionError.class)
   public void expectInvocationWithSameMockInstanceButReplayWithNull()
   {
      new NonStrictExpectations()
      {
         // This class defines an abstract "toString" override, which initially was erroneously 
         // mocked, causing a non-strict expectation to be created during replay:
         Certificate cert;

         {
            mock.setValue(withSameInstance(cert)); times = 1;
         }
      };

      mock.setValue(null);
   }

   @Test(expected = AssertionError.class)
   public void expectNonStrictInvocationWithMatcherWhichInvokesMockedMethod()
   {
      new NonStrictExpectations()
      {
         {
            mock.setValue(with(0, new Object()
            {
               boolean validateAsPositive(int value)
               {
                  // Invoking mocked method caused ConcurrentModificationException (bug fixed):
                  mock.simpleOperation(1, "b", null);
                  return value > 0;
               }
            }));
            minTimes = 1;
         }
      };

      mock.setValue(-3);
   }

   @Test
   public void expectInvocationWithSubstring()
   {
      new Expectations()
      {
         {
            mock.complexOperation(withSubstring("sub"));
         }
      };

      mock.complexOperation("abcsub123");
   }

   @Test
   public void expectInvocationWithPrefix()
   {
      new Expectations()
      {
         {
            mock.complexOperation(withPrefix("abc"));
         }
      };

      mock.complexOperation("abcsub123");
   }

   @Test
   public void expectInvocationWithSuffix()
   {
      new Expectations()
      {
         {
            mock.complexOperation(withSuffix("123"));
         }
      };

      mock.complexOperation("abcsub123");
   }

   @Test
   public void expectInvocationWithMatchForRegex()
   {
      new Expectations()
      {
         {
            mock.complexOperation(withMatch("[a-z]+[0-9]*"));
            mock.complexOperation(withMatch("(?i)[a-z]+sub[0-9]*"));
         }
      };

      mock.complexOperation("abcsub123");
      mock.complexOperation("abcSuB123");
   }

   @Test(expected = AssertionError.class)
   public void expectInvocationWithMatchForRegexButWithNonMatchingArgument()
   {
      new Expectations()
      {
         {
            mock.complexOperation(withMatch("test"));
         }
      };

      mock.complexOperation("otherValue");
   }

   @Test
   public void expectInvocationWithUserProvidedMatcher()
   {
      new Expectations()
      {
         {
            mock.setValue(with(1, new IsEqual<Integer>(3)));
         }
      };

      mock.setValue(3);
   }

   @Test
   public void expectInvocationWithUserImplementedMatcherUsingHamcrestAPI()
   {
      new Expectations()
      {
         {
            mock.complexOperation(with(new BaseMatcher<Integer>()
            {
               public boolean matches(Object item)
               {
                  Integer value = (Integer) item;
                  return value >= 10 && value <= 100;
               }

               public void describeTo(Description description)
               {
                  description.appendText("between 10 and 100");
               }
            }));
         }
      };

      mock.complexOperation(28);
   }

   @Test
   public void expectInvocationsWithUserImplementedReflectionBasedMatchers()
   {
      new Expectations()
      {
         {
            mock.setValue(with(0, new Object()
            {
               boolean matches(int value)
               {
                  return value >= 10 && value <= 100;
               }
            }));

            mock.setValue(with(0.0, new Object()
            {
               void validate(double value)
               {
                  assert value >= 20.0 && value <= 80.0 : "value outside of 20-80 range";
               }
            }));
         }
      };

      mock.setValue(28);
      mock.setValue(20.0);
   }

   @Test
   public void expectInvocationWithMatcherContainingAnotherMatcher()
   {
      new Expectations()
      {
         {
            mock.setValue((Integer) with(IsEqual.equalTo(3)));
         }
      };

      mock.setValue(3);
   }
}
