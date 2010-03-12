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
package mockit.internal.expectations;

import java.lang.reflect.*;
import java.util.*;

import mockit.*;
import mockit.external.asm.Type;
import mockit.internal.expectations.invocation.*;
import mockit.internal.util.*;

public final class Expectation
{
   final RecordPhase recordPhase;
   public final ExpectedInvocation invocation;
   public final InvocationConstraints constraints;
   private InvocationResults results;

   Expectation(RecordPhase recordPhase, ExpectedInvocation invocation, boolean nonStrict)
   {
      this.recordPhase = recordPhase;
      this.invocation = invocation;
      constraints = new InvocationConstraints(nonStrict);
   }

   Expectation(Expectation other)
   {
      recordPhase = other.recordPhase;
      invocation = other.invocation;
      constraints = new InvocationConstraints(other.constraints);
      results = other.results;
   }

   public InvocationResults getResults()
   {
      if (results == null) {
         results = new InvocationResults(invocation, constraints);
      }

      return results;
   }

   Object produceResult(Object invokedObject, Object[] invocationArgs) throws Throwable
   {
      if (results == null) {
         return invocation.getDefaultValueForReturnType(null);
      }

      return results.produceResult(invokedObject, invocationArgs);
   }

   AssertionError verifyConstraints()
   {
      return constraints.verify(invocation);
   }

   public void addReturnValueOrValues(Object value)
   {
      validateReturnValues(value, (Object) null);

      if (value instanceof Iterator<?> && !hasReturnValueOfType(value.getClass())) {
         getResults().addDeferredReturnValues((Iterator<?>) value);
      }
      else if (value instanceof Collection<?> && !hasReturnValueOfType(value.getClass())) {
         Collection<?> values = (Collection<?>) value;
         getResults().addReturnValues(values.toArray(new Object[values.size()]));
      }
      else if (invocation.overrideDefaultCascadedMockIfAny(value)) {
         recordPhase.setNextInstanceToMatch(null);
      }
      else {
         getResults().addReturnValue(value);
      }
   }

   private void validateReturnValues(Object firstValue, Object... remainingValues)
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
      return invocation.getMethodNameAndDescription().endsWith(")V");
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
      Class<?> invocationReturnClass = getReturnType();
      return invocationReturnClass.isAssignableFrom(typeToBeReturned);
   }

   private Class<?> getReturnType()
   {
      Type invocationReturnType = Type.getReturnType(invocation.getMethodNameAndDescription());
      return Utilities.getClassForType(invocationReturnType);
   }

   public void addSequenceOfReturnValues(Object firstValue, Object[] remainingValues)
   {
      validateReturnValues(firstValue, remainingValues);

      InvocationResults sequence = getResults();

      if (remainingValues == null) {
         sequence.addReturnValue(firstValue);
      }
      else if (!addReturnValueForSequenceOfValues(firstValue, remainingValues)) {
         sequence.addReturnValue(firstValue);
         sequence.addReturnValues(remainingValues);
      }
   }

   private boolean addReturnValueForSequenceOfValues(Object first, Object[] remaining)
   {
      Class<?> rt = getReturnType();

      if (rt != null) {
         int n = 1 + remaining.length;

         if (rt.isArray()) {
            if (first == null || !first.getClass().isArray()) {
               addArrayAsReturnValue(rt.getComponentType(), n, first, remaining);
               return true;
            }
         }
         else if (Iterator.class.isAssignableFrom(rt)) {
            addIteratorAsReturnValue(first, remaining, n);
            return true;
         }
         else if (Iterable.class.isAssignableFrom(rt)) {
            if (rt.isAssignableFrom(List.class)) {
               addReturnValues(new ArrayList<Object>(n), first, remaining);
               return true;
            }
            else if (rt.isAssignableFrom(Set.class)) {
               addReturnValues(new LinkedHashSet<Object>(n), first, remaining);
               return true;
            }
            else if (rt.isAssignableFrom(SortedSet.class)) {
               addReturnValues(new TreeSet<Object>(), first, remaining);
               return true;
            }
         }
      }

      return false;
   }

   private void addArrayAsReturnValue(Class<?> elementType, int n, Object first, Object[] remaining)
   {
      Object values = Array.newInstance(elementType, n);
      setArrayElement(elementType, values, 0, first);

      for (int i = 1; i < n; i++) {
         setArrayElement(elementType, values, i, remaining[i - 1]);
      }

      results.addReturnValue(values);
   }

   @SuppressWarnings({"AssignmentToMethodParameter"})
   private void setArrayElement(Class<?> elementType, Object array, int index, Object value)
   {
      if (elementType == byte.class || elementType == Byte.class) {
         value = ((Number) value).byteValue();
      }
      else if (elementType == short.class || elementType == Short.class) {
         value = ((Number) value).shortValue();
      }

      Array.set(array, index, value);
   }

   private void addIteratorAsReturnValue(Object first, Object[] remaining, int n)
   {
      List<Object> values = new ArrayList<Object>(n);
      addAllValues(values, first, remaining);
      results.addReturnValue(values.iterator());
   }

   private void addAllValues(Collection<Object> values, Object first, Object[] remaining)
   {
      values.add(first);
      Collections.addAll(values, remaining);
   }

   private void addReturnValues(Collection<Object> values, Object first, Object[] remaining)
   {
      addAllValues(values, first, remaining);
      results.addReturnValue(values);
   }

   public void setCustomErrorMessage(CharSequence message)
   {
      invocation.customErrorMessage = message;
   }
}
