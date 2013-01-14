/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.util;

import java.lang.reflect.*;
import java.util.*;
import java.util.Map.*;

public final class GenericTypeReflection
{
   private final Map<String, String> typeParametersToTypeArguments;

   public GenericTypeReflection(Class<?> realClass, Type mockedType)
   {
      typeParametersToTypeArguments = new HashMap<String, String>(4);

      if (mockedType instanceof ParameterizedType) {
         addMappingsFromTypeParametersToTypeArguments(realClass, (ParameterizedType) mockedType);
      }

      addGenericTypeMappingsForSuperTypes(realClass);
   }

   private void addGenericTypeMappingsForSuperTypes(Class<?> realClass)
   {
      Type superType = realClass;

      while (superType instanceof Class<?> && superType != Object.class) {
         Class<?> superClass = (Class<?>) superType;
         superType = superClass.getGenericSuperclass();

         superType = addGenericTypeMappingsIfParameterized(superType);

         for (Type implementedInterface : superClass.getGenericInterfaces()) {
            addGenericTypeMappingsIfParameterized(implementedInterface);
         }
      }
   }

   private Type addGenericTypeMappingsIfParameterized(Type superType)
   {
      if (superType instanceof ParameterizedType) {
         ParameterizedType mockedSuperType = (ParameterizedType) superType;
         Type rawType = mockedSuperType.getRawType();
         addMappingsFromTypeParametersToTypeArguments((Class<?>) rawType, mockedSuperType);
         return rawType;
      }

      return superType;
   }

   private void addMappingsFromTypeParametersToTypeArguments(Class<?> mockedClass, ParameterizedType mockedType)
   {
      TypeVariable<?>[] typeParameters = mockedClass.getTypeParameters();
      Type[] typeArguments = mockedType.getActualTypeArguments();
      int n = typeParameters.length;

      for (int i = 0; i < n; i++) {
         Type typeArg = typeArguments[i];
         String typeArgName = null;
         String typeVarName = typeParameters[i].getName();

         if (typeArg instanceof Class<?>) {
            typeArgName = 'L' + ((Class<?>) typeArg).getName().replace('.', '/');
         }
         else if (typeArg instanceof TypeVariable<?>) {
            String intermediateTypeArg = 'T' + ((TypeVariable<?>) typeArg).getName();
            typeArgName = typeParametersToTypeArguments.get(intermediateTypeArg);
         }

         String mappedTypeArgName = typeArgName == null ? "Ljava/lang/Object" : typeArgName;
         typeParametersToTypeArguments.put('T' + typeVarName, mappedTypeArgName);
      }
   }

   public final class GenericSignature
   {
      private final String signature;
      private final List<String> parameters;

      GenericSignature(String signature)
      {
         this.signature = signature;

         int p = signature.indexOf('(');
         int q = signature.lastIndexOf(')');
         String parameterTypeDescs = signature.substring(p + 1, q);

         parameters = new ArrayList<String>();
         addTypeDescsToList(parameterTypeDescs);
      }

      private void addTypeDescsToList(String typeDescs)
      {
         int n = typeDescs.length();

         for (int i = 0; i < n; i++) {
            i = addNextParameter(typeDescs, i);
         }
      }

      private int addNextParameter(String parameterTypeDescs, int i)
      {
         int n = parameterTypeDescs.length();
         char c = parameterTypeDescs.charAt(i);
         int j = i;
         String parameter;

         if (c == 'T') {
            j = parameterTypeDescs.indexOf(';', i);
            parameter = parameterTypeDescs.substring(i, j);
         }
         else if (c == 'L' || c == '[') {
            do {
               j++;
               if (j == n) break;
               c = parameterTypeDescs.charAt(j);
            } while (c != ';' && c != '<');

            parameter = parameterTypeDescs.substring(i, j);

            if (c == '<') {
               j = advanceToNextParameter(parameterTypeDescs, j);
            }
         }
         else {
            parameter = String.valueOf(c);
         }

         parameters.add(parameter);
         return j;
      }

      private int advanceToNextParameter(String parameterTypeDescs, int positionOfCurrentParameter)
      {
         int currentPos = positionOfCurrentParameter;
         int angleBracketDepth = 1;

         do {
            currentPos++;
            char c = parameterTypeDescs.charAt(currentPos);
            if (c == '>') angleBracketDepth--; else if (c == '<') angleBracketDepth++;
         } while (angleBracketDepth > 0);

         return currentPos + 1;
      }

      public boolean satisfiesGenericSignature(String otherSignature)
      {
         GenericSignature other = new GenericSignature(otherSignature);
         int n = parameters.size();

         if (n != other.parameters.size()) {
            return false;
         }

         for (int i = 0; i < n; i++) {
            String p1 = other.parameters.get(i);
            String p2 = parameters.get(i);

            if (!areParametersOfSameType(p1, p2)) {
               return false;
            }
         }

         return true;
      }

      private boolean areParametersOfSameType(String param1, String param2)
      {
         if (param1.equals(param2)) return true;
         if (param1.charAt(0) != 'T') return false;
         String typeArg1 = typeParametersToTypeArguments.get(param1);
         return param2.equals(typeArg1);
      }

      String resolvedReturnType()
      {
         int p = signature.lastIndexOf(')') + 1;
         int q = signature.length();
         String returnType = signature.substring(p, q);
         String resolvedReturnType = replaceParametersWithActualTypes(returnType);

         StringBuilder finalSignature = new StringBuilder(signature);
         finalSignature.replace(p, q, resolvedReturnType);
         return finalSignature.toString();
      }

      private String replaceParametersWithActualTypes(String typeDesc)
      {
         if (typeDesc.charAt(0) == 'T') {
            String typeParameter = typeDesc.substring(0, typeDesc.length() - 1);
            String typeArg = typeParametersToTypeArguments.get(typeParameter);
            return typeArg == null ? typeDesc : typeArg + ';';
         }

         int p = typeDesc.indexOf('<');

         if (p < 0) {
            return typeDesc;
         }

         String resolvedTypeDesc = typeDesc;

         for (Entry<String, String> paramAndArg : typeParametersToTypeArguments.entrySet()) {
            String typeParam = paramAndArg.getKey() + ';';
            String typeArg = paramAndArg.getValue() + ';';
            resolvedTypeDesc = resolvedTypeDesc.replace(typeParam, typeArg);
         }

         return resolvedTypeDesc;
      }
   }

   public GenericSignature parseSignature(String signature)
   {
      return new GenericSignature(signature);
   }

   public String resolveReturnType(String signature)
   {
      GenericSignature genericSignature = new GenericSignature(signature);
      return genericSignature.resolvedReturnType();
   }
}
