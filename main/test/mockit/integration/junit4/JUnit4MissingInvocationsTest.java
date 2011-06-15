/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.integration.junit4;

import java.util.*;

import org.junit.*;

import mockit.*;

public final class JUnit4MissingInvocationsTest
{
   static class Collaborator { void doSomething() {} }

   @Test(expected = AssertionError.class)
   public void expectInvocationWhichDoesNotOccurInTestedCodeThatThrowsAnException_mockUp()
   {
      new MockUp<Collaborator>() {
         @Mock(minInvocations = 2)
         void doSomething() { throw new IllegalFormatCodePointException('x'); }
      };

      new Collaborator().doSomething();
   }

   @Test(expected = AssertionError.class)
   public void expectInvocationWhichDoesNotOccurInTestedCodeThatThrowsAnException_strict(final Collaborator mock)
   {
      new Expectations() {{
         mock.doSomething(); result = new IllegalFormatCodePointException('x');
         times = 2;
      }};

      mock.doSomething();
   }

   @Test(expected = AssertionError.class)
   public void expectInvocationWhichDoesNotOccurInTestedCodeThatThrowsAnException_nonStrict(final Collaborator mock)
   {
      new NonStrictExpectations() {{
         mock.doSomething(); result = new IllegalFormatCodePointException('x');
         minTimes = 2;
      }};

      mock.doSomething();
   }
}
