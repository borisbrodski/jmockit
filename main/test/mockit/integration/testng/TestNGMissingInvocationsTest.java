/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.integration.testng;

import java.util.*;

import org.testng.annotations.*;

import mockit.*;

public final class TestNGMissingInvocationsTest
{
   static class Collaborator { void doSomething() {} }

   @Test(expectedExceptions = AssertionError.class)
   public void expectInvocationWhichDoesNotOccurInTestedCodeThatThrowsAnException_mockUp()
   {
      new MockUp<Collaborator>() {
         @Mock(minInvocations = 2)
         void doSomething() { throw new IllegalFormatCodePointException('x'); }
      };

      new Collaborator().doSomething();
   }

   @Test(expectedExceptions = AssertionError.class)
   public void expectInvocationWhichDoesNotOccurInTestedCodeThatThrowsAnException_strict(final Collaborator mock)
   {
      new Expectations() {{
         mock.doSomething(); result = new IllegalFormatCodePointException('x');
         times = 2;
      }};

      mock.doSomething();
   }

   @Test(expectedExceptions = AssertionError.class)
   public void expectInvocationWhichDoesNotOccurInTestedCodeThatThrowsAnException_nonStrict(final Collaborator mock)
   {
      new NonStrictExpectations() {{
         mock.doSomething(); result = new IllegalFormatCodePointException('x');
         minTimes = 2;
      }};

      mock.doSomething();
   }
}
