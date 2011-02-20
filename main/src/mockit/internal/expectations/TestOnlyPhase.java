/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations;

import java.util.*;

import mockit.external.hamcrest.*;

public abstract class TestOnlyPhase extends Phase
{
   protected int numberOfIterations;
   protected Object nextInstanceToMatch;
   protected boolean matchInstance;
   protected List<Matcher<?>> argMatchers;

   TestOnlyPhase(RecordAndReplayExecution recordAndReplay)
   {
      super(recordAndReplay);
   }

   public final void setNumberOfIterations(int numberOfIterations)
   {
      this.numberOfIterations = numberOfIterations;
   }

   public final void setNextInstanceToMatch(Object nextInstanceToMatch)
   {
      this.nextInstanceToMatch = nextInstanceToMatch;
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

   public final void moveArgMatcher(int originalMatcherIndex, int toIndex)
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

   public void setMaxInvocationCount(int maxInvocations)
   {
      int currentMinimum = getCurrentExpectation().constraints.minInvocations;

      if (numberOfIterations > 0) {
         currentMinimum /= numberOfIterations;
      }

      int minInvocations = maxInvocations < 0 ? currentMinimum : Math.min(currentMinimum, maxInvocations);

      handleInvocationCountConstraint(minInvocations, maxInvocations);
   }

   public abstract void handleInvocationCountConstraint(int minInvocations, int maxInvocations);

   public abstract void setCustomErrorMessage(CharSequence customMessage);

   public abstract void applyHandlerForEachInvocation(Object invocationHandler);
}
