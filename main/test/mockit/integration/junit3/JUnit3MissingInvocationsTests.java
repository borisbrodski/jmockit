/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.integration.junit3;

import java.util.*;

import junit.framework.*;

import mockit.*;

// These tests are expected to fail, so they are kept inactive to avoid busting the full test run.
public final class JUnit3MissingInvocationsTests //extends TestCase
{
   static class Collaborator { void doSomething() {} }

   public void testExpectInvocationWhichDoesNotOccurInTestedCodeThatThrowsAnException_mockUp()
   {
      new MockUp<Collaborator>() {
         @Mock(minInvocations = 2)
         void doSomething() { throw new IllegalFormatCodePointException('x'); }
      };

      try { new Collaborator().doSomething(); } catch (IllegalFormatCodePointException ignore) {}
   }

   public void testExpectInvocationWhichDoesNotOccurInTestedCodeThatThrowsAnException_strict(final Collaborator mock)
   {
      new Expectations() {{
         mock.doSomething(); result = new IllegalFormatCodePointException('x');
         times = 2;
      }};

      try { mock.doSomething(); } catch (IllegalFormatCodePointException ignore) {}
   }

   public void testExpectInvocationWhichDoesNotOccurInTestedCodeThatThrowsAnException_nonStrict(final Collaborator mock)
   {
      new NonStrictExpectations() {{
         mock.doSomething(); result = new IllegalFormatCodePointException('x');
         minTimes = 2;
      }};

      try { mock.doSomething(); } catch (IllegalFormatCodePointException ignore) {}
   }
}
