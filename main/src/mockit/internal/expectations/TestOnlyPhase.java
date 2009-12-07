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
package mockit.internal.expectations;

import java.util.*;

import mockit.external.hamcrest.*;

public abstract class TestOnlyPhase extends Phase
{
   protected int numberOfIterations;
   protected List<Matcher<?>> argMatchers;

   TestOnlyPhase(RecordAndReplayExecution recordAndReplay)
   {
      super(recordAndReplay);
      numberOfIterations = 1;
   }

   public final void setNumberOfIterations(int numberOfIterations)
   {
      this.numberOfIterations = numberOfIterations;
   }

   public final void addArgMatcher(Matcher<?> matcher)
   {
      createArgMatchersListIfNeeded();
      argMatchers.add(matcher);
   }

   private void createArgMatchersListIfNeeded()
   {
      if (argMatchers == null) {
         argMatchers = new ArrayList<Matcher<?>>();
      }
   }

   final void addArgMatcher(int index, Matcher<?> matcher)
   {
      createArgMatchersListIfNeeded();

      while (index > argMatchers.size()) {
         argMatchers.add(null);
      }

      argMatchers.add(index, matcher);
   }

   final void moveArgMatcher(int originalMatcherIndex, int toIndex)
   {
      int i = 0;

      for (int matchersFound = 0; matchersFound <= originalMatcherIndex; i++) {
         if (argMatchers.get(i) != null) {
            matchersFound++;
         }
      }

      for (i--; i < toIndex; i++) {
         argMatchers.add(i, null);
      }
   }

   public abstract void handleInvocationCountConstraint(int minInvocations, int maxInvocations);
}
