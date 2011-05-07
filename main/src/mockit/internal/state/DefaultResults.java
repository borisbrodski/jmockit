/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.state;

import java.lang.reflect.*;
import java.util.*;

import mockit.external.asm.Type;
import mockit.internal.util.*;

public final class DefaultResults
{
   private Map<String, ResultExtractor> defaultResults;

   private static final class ResultExtractor
   {
      final Field inputField;
      final Object fieldOwner;
      volatile Object valueCache;

      ResultExtractor(Field inputField, Object fieldOwner)
      {
         this.inputField = inputField;
         this.fieldOwner = fieldOwner;
      }

      void extractException()
      {
         Object exception = getInputFieldValue();
         Utilities.throwCheckedException((Exception) exception);
      }

      Object getInputFieldValue()
      {
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

      defaultResults.put(resultTypeDesc, resultExtractor);
   }

   public Object get(String signature, String[] exceptions)
   {
      if (defaultResults == null) {
         return null;
      }

      ResultExtractor extractor;

      if (exceptions != null) {
         for (String exception : exceptions) {
            extractor = defaultResults.get(exception);

            if (extractor != null) {
               extractor.extractException();
            }
         }
      }

      String returnTypeDesc = DefaultValues.getReturnTypeDesc(signature);
      int typeParameter = returnTypeDesc.indexOf("<T");

      extractor = typeParameter < 0 ? defaultResults.get(returnTypeDesc) : findResultForGenericType(typeParameter);

      return extractor == null ? null : extractor.getInputFieldValue();
   }

   private ResultExtractor findResultForGenericType(int typeParamPos)
   {
      for (Map.Entry<String, ResultExtractor> keyAndValue : defaultResults.entrySet()) {
         String key = keyAndValue.getKey();

         if (key.length() > typeParamPos && key.charAt(typeParamPos) == '<' && key.indexOf(';', typeParamPos) > 0) {
            return keyAndValue.getValue();
         }
      }

      return null;
   }

   void clear()
   {
      defaultResults = null;
   }
}