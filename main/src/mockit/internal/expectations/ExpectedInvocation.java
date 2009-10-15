/*
 * JMockit Expectations
 * Copyright (c) 2006-2009 Rogério Liesenfeld
 * All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package mockit.internal.expectations;

import java.lang.reflect.*;
import java.util.*;

import mockit.internal.util.*;

public class ExpectedInvocation
{
   final Object instance;
   final boolean methodWithVarargs;
   final String classDesc;
   final String methodNameAndDesc;
   private final boolean isConstructor;
   private final boolean matchInstance;
   protected Object[] invocationArgs;
   private final Map<Object, Object> recordToReplayInstanceMap;
   private final ExpectationError invocationCause;
   private final Object defaultReturnValue;

   private static final class ExpectationError extends AssertionError
   {
      private String message;

      @Override
      public String toString() { return message; }
   }

   ExpectedInvocation(
      Object mock, int methodAccess, String mockedClassDesc, String mockNameAndDesc,
      boolean matchInstance, Object[] args, Map<Object, Object> recordToReplayInstanceMap)
   {
      instance = mock;
      methodWithVarargs = (methodAccess & 128) != 0;
      classDesc = mockedClassDesc;
      methodNameAndDesc = mockNameAndDesc;
      isConstructor = mockNameAndDesc.startsWith("<init>");
      this.matchInstance = matchInstance;
      invocationArgs = args;
      this.recordToReplayInstanceMap = recordToReplayInstanceMap;
      invocationCause = new ExpectationError();
      defaultReturnValue = DefaultValues.computeForReturnType(mockNameAndDesc);
   }

   ExpectedInvocation(Object mock, String mockedClassDesc, String mockNameAndDesc, Object[] args)
   {
      instance = mock;
      methodWithVarargs = false;
      classDesc = mockedClassDesc;
      methodNameAndDesc = mockNameAndDesc;
      isConstructor = false;
      matchInstance = false;
      invocationArgs = args;
      recordToReplayInstanceMap = null;
      invocationCause = null;
      defaultReturnValue = null;
   }

   public String getClassName()
   {
      return classDesc.replace('/', '.');
   }

   public String getMethodNameAndDescription()
   {
      return methodNameAndDesc;
   }

   final boolean isMatch(Object replayInstance, String invokedClassDesc, String invokedMethod)
   {
      return
         invokedMethod.equals(methodNameAndDesc) && invokedClassDesc.equals(classDesc) &&
         (isConstructor || isEquivalentInstance(replayInstance));
   }

   private boolean isEquivalentInstance(Object replayInstance)
   {
      return
         !matchInstance ||
         replayInstance == instance ||
         replayInstance == recordToReplayInstanceMap.get(instance);
   }

   final AssertionError errorForUnexpectedInvocation()
   {
      return newErrorWithCause("Unexpected invocation", "Unexpected invocation of");
   }

   private AssertionError newErrorWithCause(String title, String message)
   {
      AssertionError error = new AssertionError(message + this);

      if (invocationCause != null) {
         invocationCause.message = title;
         Utilities.filterStackTrace(invocationCause);
         error.initCause(invocationCause);
      }

      return error;
   }

   final AssertionError errorForMissingInvocation()
   {
      return newErrorWithCause("Missing invocation",  "Missing invocation of");
   }

   final AssertionError errorForMissingInvocations(int totalMissing)
   {
      String plural = totalMissing == 1 ? "" : "s";
      return newErrorWithCause(
         "Missing invocations", "Missing " + totalMissing + " invocation" + plural + " to");
   }

   final AssertionError errorForUnexpectedInvocation(
      Object mock, String invokedClassDesc, String invokedMethod)
   {
      String instanceDescription = mock == null ? "" : "\non instance: " + objectIdentity(mock);
      return newErrorWithCause(
         "Unexpected invocation",
         "Unexpected invocation of:\n" + new MethodFormatter(invokedClassDesc, invokedMethod) +
         instanceDescription + "\nwhen was expecting an invocation of");
   }

   final AssertionError errorForUnexpectedInvocations(int totalUnexpected)
   {
      String plural = totalUnexpected == 1 ? "" : "s";
      return newErrorWithCause(
         "Unexpected invocations", totalUnexpected + " unexpected invocation" + plural + " to");
   }

   private String objectIdentity(Object mock)
   {
      return mock.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(mock));
   }

   @Override
   public final String toString()
   {
      StringBuilder desc = new StringBuilder(30);

      desc.append(":\n").append(invokedMethodSignature());

      if (invocationArgs.length > 0) {
         desc.append("\nwith arguments: ");
         String sep = "";

         for (Object arg : invocationArgs) {
            desc.append(sep);
            appendParameterValue(desc, arg);
            sep = ", ";
         }
      }

      if (instance != null) {
         desc.append("\non mock instance: ").append(objectIdentity(instance));
      }

      return desc.toString();
   }

   final MethodFormatter invokedMethodSignature()
   {
      return new MethodFormatter(classDesc, methodNameAndDesc);
   }

   AssertionError assertThatInvocationArgumentsMatch(Object[] replayArgs)
   {
      int argCount = replayArgs.length;

      if (methodWithVarargs) {
         AssertionError error = assertEquals(invocationArgs, replayArgs, argCount - 1);

         if (error != null) {
            return error;
         }

         Object[] expectedValues = getVarArgs(invocationArgs);
         Object[] actualValues = getVarArgs(replayArgs);

         if (expectedValues.length != actualValues.length) {
            return new AssertionError(
               "Expected " + expectedValues.length + " values for varargs parameter, got " +
               actualValues.length);
         }

         error = assertEquals(expectedValues, actualValues, expectedValues.length);

         if (error != null) {
            return new AssertionError("Varargs " + error);
         }

         return null;
      }
      else {
         return assertEquals(invocationArgs, replayArgs, argCount);
      }
   }

   final Object[] getVarArgs(Object[] args)
   {
      Object lastArg = args[args.length - 1];

      if (lastArg == null)
      {
         return new Object[1];
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

   private AssertionError assertEquals(Object[] expectedValues, Object[] actualValues, int count)
   {
      for (int i = 0; i < count; i++) {
         Object expected = expectedValues[i];
         Object actual = actualValues[i];

         if (
            actual == null && expected != null ||
            actual != null && expected == null ||
            actual != null && actual != expected &&
            actual != recordToReplayInstanceMap.get(expected) && !actual.equals(expected)
         ) {
            return argumentMismatchErrorMessage(i, expected, actual);
         }
      }

      return null;
   }

   final AssertionError argumentMismatchErrorMessage(int paramIndex, Object expected, Object actual)
   {
      StringBuilder message = new StringBuilder(50);

      message.append("Parameter ").append(paramIndex);
      message.append(" of ").append(invokedMethodSignature());
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
         message.append(getParameterValueAsString(parameterValue));
      }
   }

   String getParameterValueAsString(Object parameterValue)
   {
      if (parameterValue instanceof Number || parameterValue instanceof Boolean) {
         return parameterValue.toString();
      }

      // Other toString() implementations may result in an Error, so we take the safe path.
      return objectIdentity(parameterValue);
   }

   final Object getDefaultValueForReturnType()
   {
      return defaultReturnValue;
   }
}
