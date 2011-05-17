/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations;

import java.util.*;

import mockit.external.hamcrest.*;

final class VerifiedExpectation
{
   final Expectation expectation;
   final Object[] arguments;
   final List<Matcher<?>> argMatchers;
   final int replayIndex;

   VerifiedExpectation(Expectation expectation, Object[] arguments, List<Matcher<?>> argMatchers, int replayIndex)
   {
      this.expectation = expectation;
      this.arguments = arguments;
      this.argMatchers = argMatchers;
      this.replayIndex = replayIndex;
   }
}
