/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations;

import java.lang.reflect.*;
import java.util.*;

import mockit.*;
import mockit.external.asm.Type;
import mockit.internal.expectations.invocation.*;
import mockit.internal.state.*;
import mockit.internal.util.*;

final class Expectation
{
   final RecordPhase recordPhase;
   final ExpectedInvocation invocation;
   final InvocationConstraints constraints;
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

   InvocationResults getResults()
   {
      createResultsHolderIfNotYetCreated();
      return results;
   }

   private void createResultsHolderIfNotYetCreated()
   {
      if (results == null) {
         results = new InvocationResults(invocation, constraints);
      }
   }

   Object produceResult(Object invokedObject, Object[] invocationArgs) throws Throwable
   {
      if (results == null) {
         return invocation.getDefaultValueForReturnType(null);
      }

      return results.produceResult(invokedObject, invocationArgs);
   }

   AssertionError verifyConstraints(int minInvocations, int maxInvocations)
   {
      return constraints.verify(invocation, minInvocations, maxInvocations);
   }

   void addReturnValueOrValues(Object value)
   {
      createResultsHolderIfNotYetCreated();

      boolean valueIsArray = value != null && value.getClass().isArray();
      boolean valueIsIterable = value instanceof Iterable<?>;

      if ((valueIsArray || valueIsIterable || value instanceof Iterator<?>) && hasReturnOfDifferentType(value)) {
         if (valueIsArray) {
            results.addReturnValues(value);
         }
         else if (valueIsIterable) {
            results.addReturnValues((Iterable<?>) value);
         }
         else {
            results.addDeferredReturnValues((Iterator<?>) value);
         }

         return;
      }

      addSingleReturnValue(value);
   }

   private void addSingleReturnValue(Object value)
   {
      validateReturnValues(value, (Object) null);
      substituteCascadedMockToBeReturnedIfNeeded(value);
      results.addReturnValue(value);
   }

   private boolean hasReturnOfDifferentType(Object valueToBeReturned)
   {
      Class<?> returnClass = getReturnType();
      return returnClass == null || !returnClass.isAssignableFrom(valueToBeReturned.getClass());
   }

   private Class<?> getReturnType()
   {
      Type invocationReturnType = Type.getReturnType(invocation.getMethodNameAndDescription());
      return Utilities.getClassForType(invocationReturnType);
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
         throw new IllegalArgumentException("Non-null return value specified for constructor or void method");
      }
   }

   private void substituteCascadedMockToBeReturnedIfNeeded(Object valueToBeReturned)
   {
      Object cascadedMock = invocation.getCascadedMock();

      if (valueToBeReturned != null && cascadedMock != null) {
         TestRun.getExecutingTest().discardCascadedMockWhenInjectable(cascadedMock);
         recordPhase.setNextInstanceToMatch(null);
      }
   }

   void addSequenceOfReturnValues(Object firstValue, Object[] remainingValues)
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

   void addResult(Object value)
   {
      createResultsHolderIfNotYetCreated();

      if (value instanceof Throwable) {
         results.addThrowable((Throwable) value);
         return;
      }

      boolean valueIsArray = value != null && value.getClass().isArray();
      boolean valueIsIterable = value instanceof Iterable<?>;

      if ((valueIsArray || valueIsIterable || value instanceof Iterator<?>) && hasReturnOfDifferentType(value)) {
         if (valueIsArray) {
            results.addResults(value);
         }
         else if (valueIsIterable) {
            results.addResults((Iterable<?>) value);
         }
         else {
            results.addDeferredResults((Iterator<?>) value);
         }

         return;
      }

      addSingleReturnValue(value);
   }

   void setCustomErrorMessage(CharSequence message)
   {
      invocation.customErrorMessage = message;
   }
}
