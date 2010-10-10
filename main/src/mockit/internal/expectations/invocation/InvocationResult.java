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

import java.util.*;

abstract class InvocationResult
{
   InvocationResult next;

   abstract Object produceResult(
      Object invokedObject, ExpectedInvocation invocation, InvocationConstraints constraints, Object[] args)
      throws Throwable;

   static final class ReturnValueResult extends InvocationResult
   {
      private final Object returnValue;

      ReturnValueResult(Object returnValue) { this.returnValue = returnValue; }

      @Override
      Object produceResult(
         Object invokedObject, ExpectedInvocation invocation, InvocationConstraints constraints, Object[] args)
      {
         return returnValue;
      }
   }

   static final class ThrowableResult extends InvocationResult
   {
      private final Throwable throwable;

      ThrowableResult(Throwable throwable) { this.throwable = throwable; }

      @Override
      Object produceResult(
         Object invokedObject, ExpectedInvocation invocation, InvocationConstraints constraints, Object[] args)
         throws Throwable
      {
         throwable.fillInStackTrace();
         throw throwable;
      }
   }

   static final class DeferredReturnValues extends InvocationResult
   {
      private final Iterator<?> values;

      DeferredReturnValues(Iterator<?> values) { this.values = values; }

      @Override
      Object produceResult(
         Object invokedObject, ExpectedInvocation invocation, InvocationConstraints constraints, Object[] args)
         throws Throwable
      {
         return values.hasNext() ? values.next() : null;
      }
   }

   static final class DeferredResults extends InvocationResult
   {
      private final Iterator<?> values;

      DeferredResults(Iterator<?> values) { this.values = values; }

      @Override
      Object produceResult(
         Object invokedObject, ExpectedInvocation invocation, InvocationConstraints constraints, Object[] args)
         throws Throwable
      {
         Object nextValue = values.hasNext() ? values.next() : null;

         if (nextValue instanceof Throwable) {
            Throwable t = (Throwable) nextValue;
            t.fillInStackTrace();
            throw t;
         }

         return nextValue;
      }
   }
}
