/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.util;

import java.util.*;

import mockit.internal.state.*;

public final class MethodFormatter
{
   private final StringBuilder out;

   private String classDesc;
   private String methodDesc;

   // Auxiliary fields for handling method parameters:
   private int parameterIndex;
   private int typeDescPos;
   private char typeCode;
   private int arrayDimensions;

   public MethodFormatter() { out = new StringBuilder(); }

   public MethodFormatter(String classDesc, String methodDesc)
   {
      this();
      this.classDesc = classDesc;
      this.methodDesc = methodDesc;
      appendFriendlyMethodSignature();
   }

   @Override
   public String toString() { return out.toString(); }

   public String friendlyMethodSignatures(Collection<String> classAndMethodDescs)
   {
      String sep = "";

      for (String classAndMethodDesc : classAndMethodDescs) {
         out.append(sep);
         separateClassAndMethodInternalDescriptions(classAndMethodDesc);
         appendFriendlyMethodSignature();
         sep = ",\n";
      }

      return out.toString();
   }

   private void separateClassAndMethodInternalDescriptions(String classAndMethodDesc)
   {
      int p = classAndMethodDesc.indexOf('#');

      if (p >= 0) {
         classDesc = classAndMethodDesc.substring(0, p);
         methodDesc = classAndMethodDesc.substring(p + 1);
      }
      else {
         classDesc = null;
         methodDesc = classAndMethodDesc;
      }
   }

   private void appendFriendlyMethodSignature()
   {
      String friendlyDesc = methodDesc;

      if (classDesc != null) {
         String className = classDesc.replace('/', '.');
         out.append(className).append('#');

         String constructorName = getConstructorName(className);
         friendlyDesc = friendlyDesc.replace("<init>", constructorName);
      }

      int leftParenNextPos = friendlyDesc.indexOf('(') + 1;
      int rightParenPos = friendlyDesc.indexOf(')');

      if (leftParenNextPos < rightParenPos) {
         out.append(friendlyDesc.substring(0, leftParenNextPos));
         String parameterTypes = friendlyDesc.substring(leftParenNextPos, rightParenPos);
         parameterIndex = 0;
         appendFriendlyTypes(parameterTypes);
         out.append(')');
      }
      else {
         out.append(friendlyDesc.substring(0, rightParenPos + 1));
      }
   }

   private String getConstructorName(String className)
   {
      int p = className.lastIndexOf('.');
      String constructorName = p < 0 ? className : className.substring(p + 1);

      //noinspection ReuseOfLocalVariable
      p = constructorName.lastIndexOf('$');

      if (p > 0) {
         constructorName = constructorName.substring(p + 1);
      }

      return constructorName;
   }

   private void appendFriendlyTypes(String typeDescs)
   {
      String sep = "";

      for (String typeDesc : typeDescs.split(";")) {
         out.append(sep);

         if (typeDesc.charAt(0) == 'L') {
            out.append(friendlyReferenceType(typeDesc));
            appendParameterName();
         }
         else {
            appendFriendlyPrimitiveTypes(typeDesc);
         }

         sep = ", ";
      }
   }

   private String friendlyReferenceType(String typeDesc)
   {
      return typeDesc.substring(1).replace("java/lang/", "").replace('/', '.');
   }

   private void appendParameterName()
   {
      if (classDesc != null) {
         String name = ParameterNames.getName(classDesc, methodDesc, parameterIndex);

         if (name != null) {
            out.append(' ').append(name);
         }
      }

      parameterIndex++;
   }

   private void appendFriendlyPrimitiveTypes(String typeDesc)
   {
      String sep = "";

      for (typeDescPos = 0; typeDescPos < typeDesc.length(); typeDescPos++) {
         typeCode = typeDesc.charAt(typeDescPos);
         advancePastArrayDimensionsIfAny(typeDesc);

         String paramType = getTypeNameForTypeDesc(typeDesc);
         out.append(sep).append(paramType);

         appendArrayBrackets();
         appendParameterName();
         sep = ", ";
      }
   }

   @SuppressWarnings("OverlyComplexMethod")
   private String getTypeNameForTypeDesc(String typeDesc)
   {
      String paramType;

      switch (typeCode) {
         case 'B': return "byte";
         case 'C': return "char";
         case 'D': return "double";
         case 'F': return "float";
         case 'I': return "int";
         case 'J': return "long";
         case 'S': return "short";
         case 'V': return "void";
         case 'Z': return "boolean";
         case 'L':
            paramType = friendlyReferenceType(typeDesc.substring(typeDescPos));
            typeDescPos = typeDesc.length();
            break;
         default:
            paramType = typeDesc.substring(typeDescPos);
            typeDescPos = typeDesc.length();
      }

      return paramType;
   }

   private void advancePastArrayDimensionsIfAny(String param)
   {
      arrayDimensions = 0;

      while (typeCode == '[') {
         typeDescPos++;
         typeCode = param.charAt(typeDescPos);
         arrayDimensions++;
      }
   }

   private void appendArrayBrackets()
   {
      for (int i = 0; i < arrayDimensions; i++) {
         out.append("[]");
      }
   }
}
