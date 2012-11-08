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

final class ReturnTypeConversion
{
   private final Expectation expectation;
   private final Class<?> returnType;
   private final Object value;
   private final InvocationResults results;

   ReturnTypeConversion(Expectation expectation, Class<?> returnType, Object value)
   {
      this.expectation = expectation;
      this.returnType = returnType;
      this.value = value;
      results = expectation.getResults();
   }

   void addConvertedValueOrValues()
   {
      boolean valueIsArray = value != null && value.getClass().isArray();
      boolean valueIsIterable = value instanceof Iterable<?>;

      if (valueIsArray || valueIsIterable || value instanceof Iterator<?>) {
         if (returnType == void.class || hasReturnOfDifferentType()) {
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
      }

      expectation.substituteCascadedMockToBeReturnedIfNeeded(value);
      results.addReturnValue(value);
   }

   private boolean hasReturnOfDifferentType()
   {
      return
         !returnType.isArray() &&
         !Iterable.class.isAssignableFrom(returnType) && !Iterator.class.isAssignableFrom(returnType) &&
         !returnType.isAssignableFrom(value.getClass());
   }

   void addConvertedValue()
   {
      boolean valueIsArray = value.getClass().isArray();

      if (valueIsArray || value instanceof Iterable<?> || value instanceof Iterator<?>) {
         addMultiValuedResultBasedOnTheReturnType(valueIsArray);
      }
      else {
         addResultFromSingleValue();
      }
   }

   private void addMultiValuedResultBasedOnTheReturnType(boolean valueIsArray)
   {
      if (returnType == void.class) {
         addMultiValuedResult(valueIsArray);
      }
      else if (returnType == Object.class) {
         results.addReturnValueResult(value);
      }
      else if (valueIsArray && addCollectionOrMapWithElementsFromArray()) {
         // Nothing left to do.
      }
      else if (hasReturnOfDifferentType()) {
         addMultiValuedResult(valueIsArray);
      }
      else {
         results.addReturnValueResult(value);
      }
   }

   private void addMultiValuedResult(boolean valueIsArray)
   {
      if (valueIsArray) {
         results.addResults(value);
      }
      else if (value instanceof Iterable<?>) {
         results.addResults((Iterable<?>) value);
      }
      else {
         results.addDeferredResults((Iterator<?>) value);
      }
   }

   private boolean addCollectionOrMapWithElementsFromArray()
   {
      int n = Array.getLength(value);
      Object values = null;

      if (returnType.isAssignableFrom(ListIterator.class)) {
         List<Object> list = new ArrayList<Object>(n);
         addArrayElements(list, n);
         values = list.listIterator();
      }
      else if (returnType.isAssignableFrom(List.class)) {
         values = addArrayElements(new ArrayList<Object>(n), n);
      }
      else if (returnType.isAssignableFrom(Set.class)) {
         values = addArrayElements(new LinkedHashSet<Object>(n), n);
      }
      else if (returnType.isAssignableFrom(SortedSet.class)) {
         values = addArrayElements(new TreeSet<Object>(), n);
      }
      else if (returnType.isAssignableFrom(Map.class)) {
         values = addArrayElements(new LinkedHashMap<Object, Object>(n), n);
      }
      else if (returnType.isAssignableFrom(SortedMap.class)) {
         values = addArrayElements(new TreeMap<Object, Object>(), n);
      }

      if (values != null) {
         results.addReturnValue(values);
         return true;
      }

      return false;
   }

   private Object addArrayElements(Collection<Object> values, int elementCount)
   {
      for (int i = 0; i < elementCount; i++) {
         Object element = Array.get(value, i);
         values.add(element);
      }

      return values;
   }

   private Object addArrayElements(Map<Object, Object> values, int elementPairCount)
   {
      for (int i = 0; i < elementPairCount; i++) {
         Object keyAndValue = Array.get(value, i);

         if (keyAndValue == null || !keyAndValue.getClass().isArray()) {
            return null;
         }

         Object key = Array.get(keyAndValue, 0);
         Object element = Array.getLength(keyAndValue) > 1 ? Array.get(keyAndValue, 1) : null;
         values.put(key, element);
      }

      return values;
   }

   private void addResultFromSingleValue()
   {
      if (returnType == Object.class || returnType.isPrimitive() || returnType.isEnum()) {
         results.addReturnValueResult(value);
      }
      else if (returnType.isArray()) {
         Object array = Array.newInstance(returnType.getComponentType(), 1);
         Array.set(array, 0, value);
         results.addReturnValueResult(array);
      }
      else if (returnType.isAssignableFrom(ArrayList.class)) {
         addCollectionWithSingleElement(new ArrayList<Object>(1));
      }
      else if (returnType.isAssignableFrom(LinkedList.class)) {
         addCollectionWithSingleElement(new LinkedList<Object>());
      }
      else if (returnType.isAssignableFrom(HashSet.class)) {
         addCollectionWithSingleElement(new HashSet<Object>(1));
      }
      else if (returnType.isAssignableFrom(TreeSet.class)) {
         addCollectionWithSingleElement(new TreeSet<Object>());
      }
      else if (returnType.isAssignableFrom(ListIterator.class)) {
         List<Object> l = new ArrayList<Object>(1);
         l.add(value);
         results.addReturnValueResult(l.listIterator());
      }
      else if (value instanceof CharSequence) {
         addCharSequence((CharSequence) value);
      }
      else {
         expectation.substituteCascadedMockToBeReturnedIfNeeded(value);
         results.addReturnValueResult(value);
      }
   }

   private void addCollectionWithSingleElement(Collection<Object> container)
   {
      container.add(value);
      results.addReturnValueResult(container);
   }

   private void addCharSequence(CharSequence value)
   {
      Object convertedValue = value;

      if (returnType.isAssignableFrom(ByteArrayInputStream.class)) {
         convertedValue = new ByteArrayInputStream(value.toString().getBytes());
      }
      else if (returnType.isAssignableFrom(StringReader.class)) {
         convertedValue = new StringReader(value.toString());
      }
      else if (!(value instanceof StringBuilder) && returnType.isAssignableFrom(StringBuilder.class)) {
         convertedValue = new StringBuilder(value);
      }
      else if (!(value instanceof CharBuffer) && returnType.isAssignableFrom(CharBuffer.class)) {
         convertedValue = CharBuffer.wrap(value);
      }

      results.addReturnValueResult(convertedValue);
   }
}
