/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.state;

import java.lang.reflect.*;
import java.util.*;

import mockit.*;
import mockit.external.asm4.Type;
import mockit.internal.util.*;

public final class DefaultResults
{
   private Map<String, ResultExtractor> defaultResults;
   private Map<String, ReturnType> genericReturnTypes;

   private static final class ReturnType
   {
      private final List<String> components;

      ReturnType(String typeDesc)
      {
         components = new ArrayList<String>();
         int p = 0;

         while (p < typeDesc.length()) {
            char typeCode = typeDesc.charAt(p);

            if (typeCode == 'L' || typeCode == 'T') {
               int q1 = typeDesc.indexOf('<', p);
               int q2 = typeDesc.indexOf(';', p);
               int q = q1 > 0 && q1 < q2 ? q1 : q2;
               components.add(typeDesc.substring(p, q));
               p = q;
            }

            p++;
         }
      }

      boolean satisfiesTypeVariables(ReturnType typeDesc)
      {
         int n = components.size();

         if (n != typeDesc.components.size()) {
            return false;
         }

         for (int i = 0; i < n; i++) {
            String c1 = components.get(i);
            String c2 = typeDesc.components.get(i);

            if (c1.charAt(0) != 'T' && !c1.equals(c2)) {
               return false;
            }
         }

         return true;
      }
   }

   private static final class ResultExtractor
   {
      final Field inputField;
      final Object fieldOwner;
      ResultExtractor next;
      volatile int invocationsRemaining;
      volatile Object valueCache;

      ResultExtractor(Field inputField, Object fieldOwner)
      {
         this.inputField = inputField;
         this.fieldOwner = fieldOwner;
         invocationsRemaining = inputField.getAnnotation(Input.class).invocations();
      }

      void chainNextOne(ResultExtractor next)
      {
         this.next = next;

         if (invocationsRemaining == Integer.MAX_VALUE) {
            invocationsRemaining = 1;
         }
      }

      void extractException()
      {
         Object exception = getInputFieldValue();
         Utilities.throwCheckedException((Exception) exception);
      }

      Object getInputFieldValue()
      {
         if (invocationsRemaining <= 0) {
            return next == null ? null : next.getInputFieldValue();
         }

         invocationsRemaining--;
         Object valueFromCache = valueCache;

         if (valueFromCache != null) {
            return valueFromCache;
         }

         Object fieldValue = Utilities.getFieldValue(inputField, fieldOwner);

         if (fieldValue == null) {
            fieldValue = Utilities.newInstanceUsingDefaultConstructor(inputField.getType());
         }

         valueCache = fieldValue;
         return fieldValue;
      }
   }

   public void add(Field inputField, Object fieldOwner)
   {
      Class<?> fieldType = inputField.getType();
      String resultTypeDesc;

      if (Exception.class.isAssignableFrom(fieldType)) {
         resultTypeDesc = Type.getInternalName(fieldType);
      }
      else {
         resultTypeDesc = getReturnTypeDescriptor(inputField, fieldType);
      }

      addExtractor(resultTypeDesc, new ResultExtractor(inputField, fieldOwner));
   }

   private String getReturnTypeDescriptor(Field inputField, Class<?> fieldType)
   {
      String returnTypeDesc = Utilities.invoke(Field.class, inputField, "getGenericSignature");

      if (returnTypeDesc == null) {
         returnTypeDesc = Type.getDescriptor(fieldType);
      }

      return returnTypeDesc;
   }

   private void addExtractor(String resultTypeDesc, ResultExtractor resultExtractor)
   {
      if (defaultResults == null) {
         defaultResults = new HashMap<String, ResultExtractor>();
      }

      ResultExtractor previousExtractor = defaultResults.get(resultTypeDesc);

      if (previousExtractor == null) {
         defaultResults.put(resultTypeDesc, resultExtractor);
      }
      else {
         previousExtractor.chainNextOne(resultExtractor);
      }
   }

   public Object get(String signature, String[] exceptions)
   {
      if (defaultResults == null) {
         return null;
      }

      extractAndThrowExceptionIfSpecified(exceptions);

      String returnTypeDesc = DefaultValues.getReturnTypeDesc(signature);
      ResultExtractor extractor;

      if (signature.charAt(0) == '<' && !signature.startsWith("<init>") || returnTypeDesc.charAt(0) == 'T') {
         extractor = findResultForGenericType(returnTypeDesc);
      }
      else {
         extractor = defaultResults.get(returnTypeDesc);
      }

      return extractor == null ? null : extractor.getInputFieldValue();
   }

   private void extractAndThrowExceptionIfSpecified(String[] exceptions)
   {
      if (exceptions != null) {
         for (String exception : exceptions) {
            ResultExtractor extractor = defaultResults.get(exception);

            if (extractor != null) {
               extractor.extractException();
            }
         }
      }
   }

   private ResultExtractor findResultForGenericType(String returnTypeDesc)
   {
      ReturnType genericType = findReturnType(returnTypeDesc);

      for (Map.Entry<String, ResultExtractor> keyAndValue : defaultResults.entrySet()) {
         String typeDesc = keyAndValue.getKey();

         if (typeDesc.length() > 1) {
            ReturnType returnType = findReturnType(typeDesc);

            if (genericType.satisfiesTypeVariables(returnType)) {
               return keyAndValue.getValue();
            }
         }
      }

      return null;
   }

   private ReturnType findReturnType(String returnTypeDesc)
   {
      if (genericReturnTypes == null) {
         genericReturnTypes = new HashMap<String, ReturnType>();
      }

      ReturnType genericType = genericReturnTypes.get(returnTypeDesc);

      if (genericType == null) {
         genericType = new ReturnType(returnTypeDesc);
         genericReturnTypes.put(returnTypeDesc, genericType);
      }

      return genericType;
   }

   void clear()
   {
      defaultResults = null;
      genericReturnTypes = null;
   }
}