/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations.invocation;

import java.lang.reflect.*;
import java.util.*;

import mockit.external.asm4.*;
import mockit.internal.*;
import mockit.internal.expectations.argumentMatching.*;
import mockit.internal.state.*;
import mockit.internal.util.*;

public final class InvocationArguments
{
   private static final Object[] NULL_VARARGS = new Object[0];

   final String classDesc;
   final String methodNameAndDesc;
   final String genericSignature;
   final String[] exceptions;
   private final int methodAccess;
   private Object[] invocationArgs;
   private List<ArgumentMatcher> matchers;
   private RealMethod realMethod;

   InvocationArguments(
      int access, String classDesc, String methodNameAndDesc, String genericSignature, String exceptions, Object[] args)
   {
      methodAccess = access;
      this.classDesc = classDesc;
      this.methodNameAndDesc = methodNameAndDesc;
      this.genericSignature = genericSignature;
      this.exceptions = exceptions == null ? null : exceptions.split(" ");
      invocationArgs = args;
   }

   String getClassName() { return classDesc.replace('/', '.'); }
   String getGenericSignature() { return genericSignature == null ? methodNameAndDesc : genericSignature; }
   boolean isForConstructor() { return methodNameAndDesc.charAt(0) == '<'; }

   public Object[] getValues() { return invocationArgs; }
   void setValues(Object[] values) { invocationArgs = values; }

   public void setValuesWithNoMatchers(Object[] argsToVerify)
   {
      invocationArgs = argsToVerify;
      matchers = null;
   }

   public List<ArgumentMatcher> getMatchers() { return matchers; }
   public void setMatchers(List<ArgumentMatcher> matchers) { this.matchers = matchers; }

   public Object[] prepareForVerification(Object[] argsToVerify, List<ArgumentMatcher> matchers)
   {
      Object[] replayArgs = invocationArgs;
      invocationArgs = argsToVerify;
      this.matchers = matchers;
      return replayArgs;
   }

   @SuppressWarnings("OverlyLongMethod")
   public boolean isMatch(Object[] replayArgs, Map<Object, Object> instanceMap)
   {
      TestRun.enterNoMockingZone();

      try {
         if (matchers == null) {
            return areEqual(replayArgs, instanceMap);
         }

         int argCount = replayArgs.length;
         Object[] replayVarArgs = replayArgs;
         Object[] invocationVarArgs = invocationArgs;
         int varArgsCount = 0;

         if (isVarargsMethod()) {
            invocationVarArgs = getVarArgs(invocationArgs);
            replayVarArgs = getVarArgs(replayArgs);

            if (invocationVarArgs != NULL_VARARGS) {
               varArgsCount = replayVarArgs.length;

               if (varArgsCount != invocationVarArgs.length) {
                  return false;
               }
            }

            argCount--;
         }

         int n = argCount + varArgsCount;

         for (int i = 0; i < n; i++) {
            Object actual = getArgument(replayArgs, replayVarArgs, argCount, i);
            ArgumentMatcher expected = getArgumentMatcher(i);

            if (expected == null) {
               Object arg = getArgument(invocationArgs, invocationVarArgs, argCount, i);
               if (arg == null) continue;
               expected = new EqualityMatcher(arg);
            }

            if (!expected.matches(actual)) {
               return false;
            }
         }
      }
      finally {
         TestRun.exitNoMockingZone();
      }

      return true;
   }

   private boolean areEqual(Object[] replayArgs, Map<Object, Object> instanceMap)
   {
      int argCount = replayArgs.length;

      if (!isVarargsMethod()) {
         return areEqual(invocationArgs, replayArgs, argCount, instanceMap);
      }

      if (!areEqual(invocationArgs, replayArgs, argCount - 1, instanceMap)) {
         return false;
      }

      Object[] expectedValues = getVarArgs(invocationArgs);
      Object[] actualValues = getVarArgs(replayArgs);

      return
         expectedValues.length == actualValues.length &&
         areEqual(expectedValues, actualValues, expectedValues.length, instanceMap);
   }

   private boolean isVarargsMethod() { return (methodAccess & Opcodes.ACC_VARARGS) != 0; }

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

   private boolean areEqual(Object[] expectedValues, Object[] actualValues, int count, Map<Object, Object> instanceMap)
   {
      for (int i = 0; i < count; i++) {
         if (isNotEqual(expectedValues[i], actualValues[i], instanceMap)) {
            return false;
         }
      }

      return true;
   }

   private boolean isNotEqual(Object expected, Object actual, Map<Object, Object> instanceMap)
   {
      return
         actual == null && expected != null ||
         actual != null && expected == null ||
         actual != null && actual != expected && actual != instanceMap.get(expected) &&
         !EqualityMatcher.areEqualWhenNonNull(actual, expected);
   }

   private Object getArgument(Object[] regularArgs, Object[] varArgs, int regularArgCount, int parameterIndex)
   {
      return parameterIndex < regularArgCount ? regularArgs[parameterIndex] : varArgs[parameterIndex - regularArgCount];
   }

   private ArgumentMatcher getArgumentMatcher(int parameterIndex)
   {
      ArgumentMatcher matcher = parameterIndex < matchers.size() ? matchers.get(parameterIndex) : null;

      if (matcher == null && parameterIndex < invocationArgs.length && invocationArgs[parameterIndex] == null) {
         matcher = AlwaysTrueMatcher.INSTANCE;
      }
      
      return matcher;
   }

   public Error assertMatch(Object[] replayArgs, Map<Object, Object> instanceMap)
   {
      if (matchers == null) {
         return assertEquality(replayArgs, instanceMap);
      }

      int argCount = replayArgs.length;
      Object[] replayVarArgs = replayArgs;
      Object[] invocationVarArgs = invocationArgs;
      int varArgsCount = 0;

      if (isVarargsMethod()) {
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
         ArgumentMatcher expected = getArgumentMatcher(i);

         if (expected == null) {
            Object arg = getArgument(invocationArgs, invocationVarArgs, argCount, i);
            if (arg == null) continue;
            expected = new EqualityMatcher(arg);
         }

         if (!expected.matches(actual)) {
            return argumentMismatchMessage(i, expected, actual);
         }
      }

      return null;
   }

   private Error assertEquality(Object[] replayArgs, Map<Object, Object> instanceMap)
   {
      int argCount = replayArgs.length;

      if (!isVarargsMethod()) {
         return assertEquals(invocationArgs, replayArgs, argCount, instanceMap);
      }

      Error nonVarargsError = assertEquals(invocationArgs, replayArgs, argCount - 1, instanceMap);

      if (nonVarargsError != null) {
         return nonVarargsError;
      }

      Object[] expectedValues = getVarArgs(invocationArgs);
      Object[] actualValues = getVarArgs(replayArgs);

      if (expectedValues.length != actualValues.length) {
         return errorForVarargsArraysOfDifferentLengths(expectedValues, actualValues);
      }

      Error varargsError = assertEquals(expectedValues, actualValues, expectedValues.length, instanceMap);

      if (varargsError != null) {
         return new UnexpectedInvocation("Varargs " + varargsError);
      }

      return null;
   }

   private Error errorForVarargsArraysOfDifferentLengths(Object[] expectedValues, Object[] actualValues)
   {
      return new UnexpectedInvocation(
         "Expected " + expectedValues.length + " values for varargs parameter, got " + actualValues.length);
   }

   private Error assertEquals(
      Object[] expectedValues, Object[] actualValues, int count, Map<Object, Object> instanceMap)
   {
      for (int i = 0; i < count; i++) {
         Object expected = expectedValues[i];
         Object actual = actualValues[i];

         if (isNotEqual(expected, actual, instanceMap)) {
            return argumentMismatchMessage(i, expected, actual);
         }
      }

      return null;
   }

   private Error argumentMismatchMessage(int paramIndex, Object expected, Object actual)
   {
      ArgumentMismatch message = new ArgumentMismatch();

      message.append("Parameter ");

      String parameterName = ParameterNames.getName(classDesc, methodNameAndDesc, paramIndex);

      if (parameterName == null) {
         message.append(paramIndex);
      }
      else {
         message.appendFormatted(parameterName);
      }

      message.append(" of ").append(new MethodFormatter(classDesc, methodNameAndDesc).toString());
      message.append(" expected ").appendFormatted(expected);

      if (!message.isFinished()) {
         message.append(", got ").appendFormatted(actual);
      }

      return new UnexpectedInvocation(message.toString());
   }

   @Override
   public String toString()
   {
      MethodFormatter methodFormatter = new MethodFormatter(classDesc, methodNameAndDesc);

      ArgumentMismatch desc = new ArgumentMismatch();
      desc.append(":\n").append(methodFormatter.toString());

      int parameterCount = invocationArgs.length;

      if (parameterCount > 0) {
         desc.append('\n').append("with arguments: ");

         List<String> parameterTypes = methodFormatter.getParameterTypes();
         String sep = "";

         for (int i = 0; i < parameterCount; i++) {
            ArgumentMatcher matcher = matchers == null ? null : getArgumentMatcher(i);
            desc.append(sep).appendFormatted(parameterTypes.get(i), invocationArgs[i], matcher);
            sep = ", ";
         }
      }

      return desc.toString();
   }

   @SuppressWarnings({"OverlyLongMethod", "OverlyComplexMethod"})
   public boolean hasEquivalentMatchers(InvocationArguments other)
   {
      List<ArgumentMatcher> otherMatchers = other.matchers;

      if (otherMatchers == null || otherMatchers.size() != matchers.size()) {
         return false;
      }

      int i = 0;
      int m = matchers.size();

      while (i < m) {
         ArgumentMatcher matcher1 = matchers.get(i);
         ArgumentMatcher matcher2 = otherMatchers.get(i);

         if (matcher1 == null || matcher2 == null) {
            if (!EqualityMatcher.areEqual(invocationArgs[i], other.invocationArgs[i])) {
               return false;
            }
         }
         else if (matcher1 != matcher2) {
            if (matcher1.getClass() != matcher2.getClass() || matcher1.getClass() == HamcrestAdapter.class) {
               return false;
            }

            if (!equivalentMatches(matcher1, invocationArgs[i], matcher2, other.invocationArgs[i])) {
               return false;
            }
         }

         i++;
      }

      int argCount = invocationArgs.length;
      Object[] otherVarArgs = other.invocationArgs;
      Object[] thisVarArgs = invocationArgs;
      int varArgsCount = 0;

      if (isVarargsMethod()) {
         thisVarArgs = getVarArgs(invocationArgs);
         otherVarArgs = getVarArgs(other.invocationArgs);

         if (thisVarArgs != NULL_VARARGS) {
            varArgsCount = otherVarArgs.length;

            if (varArgsCount != thisVarArgs.length) {
               return false;
            }
         }

         argCount--;
      }

      int n = argCount + varArgsCount;

      while (i < n) {
         Object thisArg = getArgument(invocationArgs, thisVarArgs, argCount, i);
         Object otherArg = getArgument(other.invocationArgs, otherVarArgs, argCount, i);

         if (!EqualityMatcher.areEqual(thisArg, otherArg)) {
            return false;
         }

         i++;
      }

      return true;
   }

   private boolean equivalentMatches(ArgumentMatcher matcher1, Object arg1, ArgumentMatcher matcher2, Object arg2)
   {
      boolean matcher1MatchesArg2 = matcher1.matches(arg2);
      boolean matcher2MatchesArg1 = matcher2.matches(arg1);

      if (arg1 != null && arg2 != null && matcher1MatchesArg2 && matcher2MatchesArg1) {
         return true;
      }

      if (arg1 == arg2 && matcher1MatchesArg2 == matcher2MatchesArg1) { // both matchers fail
         ArgumentMismatch desc1 = new ArgumentMismatch();
         matcher1.writeMismatchPhrase(desc1);
         ArgumentMismatch desc2 = new ArgumentMismatch();
         matcher2.writeMismatchPhrase(desc2);
         return desc1.toString().equals(desc2.toString());
      }

      return false;
   }

   RealMethod getRealMethod()
   {
      if (realMethod == null) {
         realMethod = new RealMethod(getClassName(), methodNameAndDesc);
      }

      return realMethod;
   }
}
