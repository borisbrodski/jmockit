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

import mockit.*;
import mockit.external.asm.*;
import mockit.internal.expectations.invocation.*;
import mockit.internal.util.*;

public final class Expectation
{
   final RecordPhase recordPhase;
   final ExpectedInvocation expectedInvocation;
   final InvocationConstraints constraints;
   private InvocationResults results;

   Expectation(RecordPhase recordPhase, ExpectedInvocation expectedInvocation, boolean nonStrict)
   {
      this.recordPhase = recordPhase;
      this.expectedInvocation = expectedInvocation;
      constraints = new InvocationConstraints(nonStrict);
   }

   Expectation(Expectation other)
   {
      recordPhase = other.recordPhase;
      expectedInvocation = other.expectedInvocation;
      constraints = new InvocationConstraints(other.constraints);
      results = other.results;
   }

   public InvocationResults getResults()
   {
      if (results == null) {
         results = new InvocationResults(expectedInvocation, constraints);
      }

      return results;
   }

   Object produceResult(Object[] invocationArgs) throws Throwable
   {
      if (results == null) {
         return expectedInvocation.getDefaultValueForReturnType(null);
      }

      return results.produceResult(invocationArgs);
   }

   AssertionError verifyConstraints()
   {
      return constraints.verify(expectedInvocation);
   }

   public void addReturnValueOrValues(Object value)
   {
      validateReturnValues(value, (Object) null);

      InvocationResults invocationResults = getResults();

      if (value instanceof Iterator<?> && !hasReturnValueOfType(value.getClass())) {
         invocationResults.addDeferredReturnValues((Iterator<?>) value);
      }
      else if (value instanceof Collection<?> && !hasReturnValueOfType(value.getClass())) {
         Collection<?> values = (Collection<?>) value;
         invocationResults.addReturnValues(values.toArray(new Object[values.size()]));
      }
      else {
         invocationResults.addReturnValue(value);
      }
   }

   public void validateReturnValues(Object firstValue, Object... remainingValues)
   {
      if (hasVoidReturnType()) {
         validateReturnValueForConstructorOrVoidMethod(firstValue);

         if (remainingValues != null) {
            for (Object anotherValue : remainingValues) {
               validateReturnValueForConstructorOrVoidMethod(anotherValue);
            }
         }
      }
   }

   private boolean hasVoidReturnType()
   {
      return expectedInvocation.getMethodNameAndDescription().endsWith(")V");
   }

   private void validateReturnValueForConstructorOrVoidMethod(Object value)
   {
      if (value != null && !(value instanceof Delegate)) {
         throw new IllegalArgumentException(
            "Non-null return value specified for constructor or void method");
      }
   }

   private boolean hasReturnValueOfType(Class<?> typeToBeReturned)
   {
      Type invocationReturnType =
         Type.getReturnType(expectedInvocation.getMethodNameAndDescription());

      Class<?> invocationReturnClass = Utilities.getClassForType(invocationReturnType);

      return invocationReturnClass.isAssignableFrom(typeToBeReturned);
   }

   public void addSequenceOfReturnValues(Object firstValue, Object[] remainingValues)
   {
      validateReturnValues(firstValue, remainingValues);

      InvocationResults invocationResults = getResults();
      invocationResults.addReturnValue(firstValue);

      if (remainingValues != null) {
         invocationResults.addReturnValues(remainingValues);
      }
   }

   public void setCustomErrorMessage(CharSequence message)
   {
      expectedInvocation.customErrorMessage = message;
   }
}
