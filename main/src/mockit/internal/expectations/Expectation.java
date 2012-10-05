/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations;

import java.io.*;
import java.lang.reflect.*;
import java.nio.*;
import java.util.*;

import mockit.internal.expectations.invocation.*;
import mockit.internal.expectations.invocation.InvocationHandler;
import mockit.internal.state.*;
import mockit.internal.util.*;

final class Expectation
{
   final RecordPhase recordPhase;
   final ExpectedInvocation invocation;
   final InvocationConstraints constraints;
   private InvocationHandler handler;
   private InvocationResults results;
   boolean executedRealImplementation;

   Expectation(RecordPhase recordPhase, ExpectedInvocation invocation, boolean nonStrict)
   {
      this.recordPhase = recordPhase;
      this.invocation = invocation;
      constraints = new InvocationConstraints(nonStrict);
   }

   void setHandler(Object handler) { this.handler = new InvocationHandler(handler); }

   private InvocationResults getResults()
   {
      if (results == null) {
         results = new InvocationResults(invocation, constraints);
      }

      return results;
   }

   Object produceResult(Object invokedObject, Object[] invocationArgs) throws Throwable
   {
      if (handler != null) {
         handler.produceResult(invokedObject, invocation, constraints, invocationArgs);
      }

      if (results == null) {
         return invocation.getDefaultValueForReturnType(null);
      }

      return results.produceResult(invokedObject, invocationArgs);
   }

   void addReturnValueOrValues(Object value)
   {
      boolean valueIsArray = value != null && value.getClass().isArray();
      boolean valueIsIterable = value instanceof Iterable<?>;

      if (valueIsArray || valueIsIterable || value instanceof Iterator<?>) {
         Class<?> rt = getReturnType();

         if (rt == null || hasReturnOfDifferentType(rt, value)) {
            if (valueIsArray) {
               getResults().addReturnValues(value);
            }
            else if (valueIsIterable) {
               getResults().addReturnValues((Iterable<?>) value);
            }
            else {
               getResults().addDeferredReturnValues((Iterator<?>) value);
            }

            return;
         }
      }

      addSingleReturnValue(value);
   }

   private void addSingleReturnValue(Object value)
   {
      substituteCascadedMockToBeReturnedIfNeeded(value);
      getResults().addReturnValue(value);
   }

   private boolean hasReturnOfDifferentType(Class<?> returnClass, Object valuesToBeReturned)
   {
      return
         !returnClass.isArray() &&
         !Iterable.class.isAssignableFrom(returnClass) && !Iterator.class.isAssignableFrom(returnClass) &&
         !returnClass.isAssignableFrom(valuesToBeReturned.getClass());
   }

   private Class<?> getReturnType()
   {
      return Utilities.getReturnType(invocation.getMethodNameAndDescription());
   }

   private void substituteCascadedMockToBeReturnedIfNeeded(Object valueToBeReturned)
   {
      if (valueToBeReturned != null) {
         Object cascadedMock = invocation.getCascadedMock();

         if (cascadedMock != null) {
            TestRun.getExecutingTest().discardCascadedMockWhenInjectable(cascadedMock);
            recordPhase.setNextInstanceToMatch(null);
         }
      }
   }

   void addSequenceOfReturnValues(Object firstValue, Object[] remainingValues)
   {
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
      boolean added = false;

      if (rt != null) {
         if (rt.isArray()) {
            added = addValuesInArrayIfApplicable(first, remaining, rt);
         }
         else if (Iterator.class.isAssignableFrom(rt)) {
            added = addValuesInIteratorIfApplicable(first, remaining);
         }
         else if (Iterable.class.isAssignableFrom(rt)) {
            added = addValuesInIterableIfApplicable(first, remaining, rt);
         }
      }

      return added;
   }

   private boolean addValuesInArrayIfApplicable(Object first, Object[] remaining, Class<?> returnType)
   {
      if (first == null || !first.getClass().isArray()) {
         addArrayAsReturnValue(returnType.getComponentType(), first, remaining);
         return true;
      }

      return false;
   }

   private void addArrayAsReturnValue(Class<?> elementType, Object first, Object[] remaining)
   {
      int n = 1 + remaining.length;
      Object values = Array.newInstance(elementType, n);
      setArrayElement(elementType, values, 0, first);

      for (int i = 1; i < n; i++) {
         setArrayElement(elementType, values, i, remaining[i - 1]);
      }

      results.addReturnValue(values);
   }

   @SuppressWarnings("AssignmentToMethodParameter")
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

   private boolean addValuesInIteratorIfApplicable(Object first, Object[] remaining)
   {
      if (first == null || !Iterator.class.isAssignableFrom(first.getClass())) {
         List<Object> values = new ArrayList<Object>(1 + remaining.length);
         addAllValues(values, first, remaining);
         results.addReturnValue(values.iterator());
         return true;
      }

      return false;
   }

   private void addAllValues(Collection<Object> values, Object first, Object[] remaining)
   {
      values.add(first);
      Collections.addAll(values, remaining);
   }

   private boolean addValuesInIterableIfApplicable(Object first, Object[] remaining, Class<?> returnType)
   {
      if (first == null || !Iterable.class.isAssignableFrom(first.getClass())) {
         if (returnType.isAssignableFrom(List.class)) {
            List<Object> values = new ArrayList<Object>(1 + remaining.length);
            addReturnValues(values, first, remaining);
            return true;
         }
         else if (returnType.isAssignableFrom(Set.class)) {
            Set<Object> values = new LinkedHashSet<Object>(1 + remaining.length);
            addReturnValues(values, first, remaining);
            return true;
         }
         else if (returnType.isAssignableFrom(SortedSet.class)) {
            addReturnValues(new TreeSet<Object>(), first, remaining);
            return true;
         }
      }

      return false;
   }

   private void addReturnValues(Collection<Object> values, Object first, Object[] remaining)
   {
      addAllValues(values, first, remaining);
      results.addReturnValue(values);
   }

   void addResult(Object value)
   {
      if (invocation.isConstructor() && value != null && value.getClass().isInstance(invocation.instance)) {
         invocation.replacementInstance = value;
         return;
      }

      if (value instanceof Throwable) {
         getResults().addThrowable((Throwable) value);
         return;
      }

      if (value instanceof CharSequence) {
         addCharSequence((CharSequence) value);
         return;
      }

      boolean valueIsArray = value != null && value.getClass().isArray();

      if (valueIsArray || value instanceof Iterable<?> || value instanceof Iterator<?>) {
         Class<?> rt = getReturnType();

         if (rt == null || rt == Object.class) {
            addMultiValuedResult(value, valueIsArray);
            return;
         }
         else if (valueIsArray && addCollectionOrMapWithElementsFromArray(value, rt)) {
            return;
         }
         else if (hasReturnOfDifferentType(rt, value)) {
            addMultiValuedResult(value, valueIsArray);
            return;
         }
      }

      addSingleReturnValue(value);
   }

   private void addCharSequence(CharSequence value)
   {
      String methodDesc = invocation.getMethodNameAndDescription();
      Object convertedValue = value;

      if (methodDesc.endsWith("Ljava/io/InputStream;") || methodDesc.endsWith("Ljava/io/ByteArrayInputStream;")) {
         convertedValue = new ByteArrayInputStream(value.toString().getBytes());
      }
      else if (methodDesc.endsWith("Ljava/io/Reader;") || methodDesc.endsWith("Ljava/io/StringReader;")) {
         convertedValue = new StringReader(value.toString());
      }
      else if (!(value instanceof StringBuilder) && methodDesc.endsWith("Ljava/lang/StringBuilder;")) {
         convertedValue = new StringBuilder(value);
      }
      else if (!(value instanceof CharBuffer) && methodDesc.endsWith("Ljava/nio/CharBuffer;")) {
         convertedValue = CharBuffer.wrap(value);
      }

      addSingleReturnValue(convertedValue);
   }

   private void addMultiValuedResult(Object value, boolean valueIsArray)
   {
      if (valueIsArray) {
         getResults().addResults(value);
      }
      else if (value instanceof Iterable<?>) {
         getResults().addResults((Iterable<?>) value);
      }
      else {
         getResults().addDeferredResults((Iterator<?>) value);
      }
   }

   private boolean addCollectionOrMapWithElementsFromArray(Object array, Class<?> returnType)
   {
      int n = Array.getLength(array);
      Object values = null;

      if (returnType.isAssignableFrom(ListIterator.class)) {
         List<Object> list = new ArrayList<Object>(n);
         addArrayElements(list, array, n);
         values = list.listIterator();
      }
      else if (returnType.isAssignableFrom(List.class)) {
         values = addArrayElements(new ArrayList<Object>(n), array, n);
      }
      else if (returnType.isAssignableFrom(Set.class)) {
         values = addArrayElements(new LinkedHashSet<Object>(n), array, n);
      }
      else if (returnType.isAssignableFrom(SortedSet.class)) {
         values = addArrayElements(new TreeSet<Object>(), array, n);
      }
      else if (returnType.isAssignableFrom(Map.class)) {
         values = addArrayElements(new LinkedHashMap<Object, Object>(n), array, n);
      }
      else if (returnType.isAssignableFrom(SortedMap.class)) {
         values = addArrayElements(new TreeMap<Object, Object>(), array, n);
      }

      if (values != null) {
         getResults().addReturnValue(values);
         return true;
      }

      return false;
   }

   private Object addArrayElements(Collection<Object> values, Object array, int elementCount)
   {
      for (int i = 0; i < elementCount; i++) {
         Object value = Array.get(array, i);
         values.add(value);
      }

      return values;
   }

   private Object addArrayElements(Map<Object, Object> values, Object array, int elementPairCount)
   {
      for (int i = 0; i < elementPairCount; i++) {
         Object keyAndValue = Array.get(array, i);

         if (keyAndValue == null || !keyAndValue.getClass().isArray()) {
            return null;
         }

         Object key = Array.get(keyAndValue, 0);
         Object value = Array.getLength(keyAndValue) > 1 ? Array.get(keyAndValue, 1) : null;
         values.put(key, value);
      }

      return values;
   }

   void setCustomErrorMessage(CharSequence message) { invocation.customErrorMessage = message; }

   Error verifyConstraints(
      ExpectedInvocation replayInvocation, Object[] replayArgs, int minInvocations, int maxInvocations)
   {
      Error error = constraints.verifyLowerLimit(invocation, minInvocations);

      if (error != null) {
         return error;
      }

      return constraints.verifyUpperLimit(replayInvocation, replayArgs, maxInvocations);
   }

   Object executeRealImplementation(Object replacementInstance, Object[] args) throws Throwable
   {
      return getResults().executeRealImplementation(replacementInstance, args);
   }
}
