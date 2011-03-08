/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations.invocation;

import java.lang.reflect.*;
import java.util.*;

import mockit.external.asm.*;
import mockit.external.hamcrest.*;
import mockit.external.hamcrest.core.*;
import mockit.internal.util.*;

public final class InvocationArguments
{
   private static final Object[] NULL_VARARGS = new Object[0];

   final String classDesc;
   final String methodNameAndDesc;
   private final int methodAccess;
   private Object[] invocationArgs;
   private List<Matcher<?>> matchers;

   InvocationArguments(int access, String classDesc, String methodNameAndDesc, Object[] args)
   {
      methodAccess = access;
      this.classDesc = classDesc;
      this.methodNameAndDesc = methodNameAndDesc;
      invocationArgs = args;
   }

   public Object[] getValues()
   {
      return invocationArgs;
   }

   public void setValuesWithNoMatchers(Object[] argsToVerify)
   {
      invocationArgs = argsToVerify;
      matchers = null;
   }

   public List<Matcher<?>> getMatchers()
   {
      return matchers;
   }

   public void setMatchers(List<Matcher<?>> matchers)
   {
      this.matchers = matchers;
   }

   public Object[] prepareForVerification(Object[] argsToVerify, List<Matcher<?>> matchers)
   {
      Object[] replayArgs = invocationArgs;
      invocationArgs = argsToVerify;
      this.matchers = matchers;
      return replayArgs;
   }

   public AssertionError assertMatch(Object[] replayArgs, Map<Object, Object> instanceMap)
   {
      if (matchers == null) {
         return assertEquality(replayArgs, instanceMap);
      }

      int argCount = replayArgs.length;
      Object[] replayVarArgs = replayArgs;
      Object[] invocationVarArgs = invocationArgs;
      int varArgsCount = 0;

      if ((methodAccess & Opcodes.ACC_VARARGS) != 0) {
         invocationVarArgs = getVarArgs(invocationArgs);
         replayVarArgs = getVarArgs(replayArgs);

         if (invocationVarArgs != NULL_VARARGS) {
            varArgsCount = replayVarArgs.length;

            if (varArgsCount != invocationVarArgs.length) {
               return errorForVarargsArraysOfDifferentLengths(invocationVarArgs, replayVarArgs);
            }
         }

         argCount--;
      }

      int n = argCount + varArgsCount;

      for (int i = 0; i < n; i++) {
         Object actual = getArgument(replayArgs, replayVarArgs, argCount, i);
         Matcher<?> expected = i < matchers.size() ? matchers.get(i) : null;

         if (expected == null) {
            Object arg = getArgument(invocationArgs, invocationVarArgs, argCount, i);
            expected = arg == null ? new IsAnything() : new IsEqual<Object>(arg);
         }

         if (!expected.matches(actual)) {
            return argumentMismatchMessage(i, expected, actual);
         }
      }

      return null;
   }

   private Object getArgument(Object[] regularArgs, Object[] varArgs, int regularArgCount, int i)
   {
      return i < regularArgCount ? regularArgs[i] : varArgs[i - regularArgCount];
   }

   private AssertionError assertEquality(Object[] replayArgs, Map<Object, Object> instanceMap)
   {
      int argCount = replayArgs.length;

      if ((methodAccess & Opcodes.ACC_VARARGS) == 0) {
         return assertEquals(invocationArgs, replayArgs, argCount, instanceMap);
      }

      AssertionError nonVarargsError = assertEquals(invocationArgs, replayArgs, argCount - 1, instanceMap);

      if (nonVarargsError != null) {
         return nonVarargsError;
      }

      Object[] expectedValues = getVarArgs(invocationArgs);
      Object[] actualValues = getVarArgs(replayArgs);

      if (expectedValues.length != actualValues.length) {
         return errorForVarargsArraysOfDifferentLengths(expectedValues, actualValues);
      }

      AssertionError varargsError = assertEquals(expectedValues, actualValues, expectedValues.length, instanceMap);

      if (varargsError != null) {
         return new AssertionError("Varargs " + varargsError);
      }

      return null;
   }

   private AssertionError errorForVarargsArraysOfDifferentLengths(Object[] expectedValues, Object[] actualValues)
   {
      return new AssertionError(
         "Expected " + expectedValues.length + " values for varargs parameter, got " + actualValues.length);
   }

   private AssertionError assertEquals(
      Object[] expectedValues, Object[] actualValues, int count, Map<Object, Object> instanceMap)
   {
      for (int i = 0; i < count; i++) {
         Object expected = expectedValues[i];
         Object actual = actualValues[i];

         if (
            actual == null && expected != null ||
            actual != null && expected == null ||
            actual != null && actual != expected && actual != instanceMap.get(expected) &&
            !IsEqual.areEqual(actual, expected)
         ) {
            return argumentMismatchMessage(i, expected, actual);
         }
      }

      return null;
   }

   private Object[] getVarArgs(Object[] args)
   {
      Object lastArg = args[args.length - 1];

      if (lastArg == null)
      {
         return NULL_VARARGS;
      }
      else if (lastArg instanceof Object[]) {
         return (Object[]) lastArg;
      }

      int varArgsLength = Array.getLength(lastArg);
      Object[] results = new Object[varArgsLength];

      for (int i = 0; i < varArgsLength; i++)
      {
         results[i] = Array.get(lastArg, i);
      }

      return results;
   }

   private AssertionError argumentMismatchMessage(int paramIndex, Object expected, Object actual)
   {
      StringBuilder message = new StringBuilder(50);

      message.append("Parameter ").append(paramIndex);
      message.append(" of ").append(new MethodFormatter(classDesc, methodNameAndDesc));
      message.append(" expected ");
      appendParameterValue(message, expected);
      message.append(", got ");
      appendParameterValue(message, actual);

      return new AssertionError(message.toString());
   }

   private void appendParameterValue(StringBuilder message, Object parameterValue)
   {
      if (parameterValue == null) {
         message.append("null");
      }
      else if (parameterValue instanceof CharSequence || parameterValue instanceof Appendable) {
         message.append('"').append(parameterValue).append('"');
      }
      else if (parameterValue instanceof Character) {
         message.append('\'').append(parameterValue).append('\'');
      }
      else {
         message.append(parameterValue);
      }
   }

   @Override
   public String toString()
   {
      StringBuilder desc = new StringBuilder(30);
      desc.append(":\n").append(new MethodFormatter(classDesc, methodNameAndDesc));

      if (invocationArgs.length > 0) {
         desc.append("\nwith arguments: ");
         String sep = "";

         for (Object arg : invocationArgs) {
            desc.append(sep);
            appendParameterValue(desc, arg);
            sep = ", ";
         }
      }

      return desc.toString();
   }
}
