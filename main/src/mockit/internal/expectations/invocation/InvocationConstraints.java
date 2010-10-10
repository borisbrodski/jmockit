/*
 * JMockit Expectations
 * Copyright (c) 2006-2010 Rogério Liesenfeld
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
package mockit.internal.expectations.invocation;

public final class InvocationConstraints
{
   public int minInvocations;
   int maxInvocations;
   public int invocationCount;

   public InvocationConstraints(boolean nonStrictInvocation)
   {
      setDefaultLimits(nonStrictInvocation);
   }

   public InvocationConstraints(InvocationConstraints other)
   {
      setLimits(other.minInvocations, other.maxInvocations);
      invocationCount = other.invocationCount;
   }

   public void setDefaultLimits(boolean nonStrictInvocation)
   {
      setLimits(nonStrictInvocation ? 0 : 1, nonStrictInvocation ? -1 : 1);
   }

   public void setLimits(int minInvocations, int maxInvocations)
   {
      this.minInvocations = minInvocations;
      this.maxInvocations = maxInvocations;
   }

   void adjustMaxInvocations(int expectedInvocationCount)
   {
      if (maxInvocations > 0 && maxInvocations < expectedInvocationCount) {
         maxInvocations = expectedInvocationCount;
      }
   }

   void setUnlimitedMaxInvocations()
   {
      maxInvocations = -1;
   }

   public boolean incrementInvocationCount()
   {
      invocationCount++;
      return invocationCount == maxInvocations;
   }

   public void addInvocationCount(InvocationConstraints other)
   {
      invocationCount += other.invocationCount;
   }

   public boolean isInvocationCountLessThanMinimumExpected()
   {
      return invocationCount < minInvocations;
   }

   public boolean isInvocationCountMoreThanMaximumExpected()
   {
      return maxInvocations >= 0 && invocationCount > maxInvocations;
   }

   public boolean isInvocationCountInExpectedRange()
   {
      return minInvocations <= invocationCount && (invocationCount <= maxInvocations || maxInvocations < 0);
   }

   public AssertionError verify(ExpectedInvocation invocation)
   {
      AssertionError error = verifyLowerLimit(invocation);

      return error != null ? error : verifyUpperLimit(invocation);
   }

   private AssertionError verifyLowerLimit(ExpectedInvocation invocation)
   {
      return invocationCount < minInvocations ? errorForMissingExpectations(invocation) : null;
   }

   public AssertionError errorForMissingExpectations(ExpectedInvocation invocation)
   {
      return invocation.errorForMissingInvocations(minInvocations - invocationCount) ;
   }

   private AssertionError verifyUpperLimit(ExpectedInvocation invocation)
   {
      if (maxInvocations >= 0) {
         int n = invocationCount - maxInvocations;

         if (n > 0) {
            return invocation.errorForUnexpectedInvocations(n);
         }
      }

      return null;
   }
}
