/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.util;

import java.util.*;

public final class MethodFormatter
{
   private final StringBuilder out = new StringBuilder();

   private String classDesc;
   private String methodDesc;

   // Auxiliary variables for handling method parameter types:
   private int typeDescPos;
   private char typeCode;
   private int arrayDimensions;

   public MethodFormatter() {}

   public MethodFormatter(String classDesc, String methodDesc)
   {
      this.classDesc = classDesc;
      this.methodDesc = methodDesc;
      appendFriendlyMethodSignature("<init>");
   }

   @Override
   public String toString()
   {
      return out.toString();
   }

   public String friendlyMethodSignatures(String constructorName, Collection<String> classAndMethodDescs)
   {
      String sep = "";

      for (String classAndMethodDesc : classAndMethodDescs) {
         out.append(sep);
         separateClassAndMethodInternalDescriptions(classAndMethodDesc);
         appendFriendlyMethodSignature(constructorName);
         sep = ",\n";
      }

      return out.toString();
   }

   private void appendFriendlyMethodSignature(String constructorDesc)
   {
      String className = null;
      String constructorName = constructorDesc;

      if (classDesc != null) {
         className = classDesc.replace('/', '.');
         constructorName = getConstructorName(className);
      }

      String friendlyDesc = methodDesc.replace("<init>", constructorName).replace("$init", constructorName);
      int leftParenPos = friendlyDesc.indexOf('(');
      int rightParenPos = friendlyDesc.indexOf(')');

      if (!methodDesc.startsWith("<init>")) {
         String returnType = friendlyDesc.substring(rightParenPos + 1);
         appendFriendlyTypes(returnType);
         out.append(' ');
      }

      if (className != null) {
         out.append(className).append('#');
      }

      if (leftParenPos + 1 < rightParenPos) {
         out.append(friendlyDesc.substring(0, leftParenPos + 1));
         String parameterTypes = friendlyDesc.substring(leftParenPos + 1, rightParenPos);
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

   private void appendFriendlyTypes(String typeDescs)
   {
      String sep = "";

      for (String typeDesc : typeDescs.split(";")) {
         out.append(sep);

         if (typeDesc.charAt(0) == 'L') {
            out.append(friendlyReferenceType(typeDesc));
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

   private void appendFriendlyPrimitiveTypes(String typeDesc)
   {
      String sep = "";

      for (typeDescPos = 0; typeDescPos < typeDesc.length(); typeDescPos++) {
         typeCode = typeDesc.charAt(typeDescPos);
         advancePastArrayDimensionsIfAny(typeDesc);
         String paramType = getTypeNameForTypeDesc(typeDesc);
         out.append(sep).append(paramType);
         appendArrayBrackets();
         sep = ", ";
      }
   }

   @SuppressWarnings({"OverlyComplexMethod"})
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
