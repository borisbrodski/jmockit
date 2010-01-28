/*
 * JMockit Expectations & Verifications
 * Copyright (c) 2006-2010 Rogério Liesenfeld
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
package mockit.internal.expectations.invocation;

import java.util.*;

import mockit.external.asm.Type;

import mockit.internal.expectations.*;
import mockit.internal.util.*;

public final class ExpectedInvocation
{
   private static final Object UNDEFINED_DEFAULT_RETURN = new Object();

   public final Object instance;
   public boolean matchInstance;
   public final InvocationArguments arguments;
   public CharSequence customErrorMessage;
   private final ExpectationError invocationCause;
   private Object defaultReturnValue;

   public ExpectedInvocation(
      Object mock, int methodAccess, String mockedClassDesc, String mockNameAndDesc,
      boolean matchInstance, Object[] args)
   {
      instance = mock;
      this.matchInstance = matchInstance;
      arguments = new InvocationArguments(methodAccess, mockedClassDesc, mockNameAndDesc, args);
      invocationCause = new ExpectationError();
      defaultReturnValue = UNDEFINED_DEFAULT_RETURN;
   }

   // Simple getters //////////////////////////////////////////////////////////////////////////////

   public String getClassDesc()
   {
      return arguments.classDesc;
   }

   public String getClassName()
   {
      return arguments.classDesc.replace('/', '.');
   }

   public String getMethodNameAndDescription()
   {
      return arguments.methodNameAndDesc;
   }

   public Object[] getArgumentValues()
   {
      return arguments.getValues();
   }

   // Matching based on instance or mocked type ///////////////////////////////////////////////////

   public boolean isMatch(String invokedClassDesc, String invokedMethod)
   {
      return invokedClassDesc.equals(arguments.classDesc) && isMatchingMethod(invokedMethod);
   }

   public boolean isMatch(Object replayInstance, Map<Object, Object> instanceMap)
   {
      return
         arguments.methodNameAndDesc.charAt(0) == '<' ||
         !matchInstance || isEquivalentInstance(replayInstance, instanceMap);
   }

   public boolean isMatch(
      Object replayInstance, String invokedClassDesc, String invokedMethod,
      Map<Object, Object> instanceMap)
   {
      return
         isMatch(invokedClassDesc, invokedMethod) &&
         (arguments.methodNameAndDesc.charAt(0) == '<' ||
          !matchInstance || isEquivalentInstance(replayInstance, instanceMap));
   }

   private boolean isMatchingMethod(String invokedMethod)
   {
      String nameAndDesc = arguments.methodNameAndDesc;
      int i = 0;

      // Will return false if the method names or parameters are different:
      while (true) {
         char c = nameAndDesc.charAt(i);

         if (c != invokedMethod.charAt(i)) {
            return false;
         }

         i++;

         if (c == ')') {
            break;
         }
      }

      int n = invokedMethod.length();

      if (n == nameAndDesc.length()) {
         int j = i;

         // Given return types of same length, will return true if they are identical:
         while (true) {
            char c = nameAndDesc.charAt(j);

            if (c != invokedMethod.charAt(j)) {
               break;
            }

            j++;

            if (j == n) {
               return true;
            }
         }
      }

      // At this point the methods are known to differ only in return type, so check if the return
      // type of the recorded one is assignable to the return type of the one invoked:
      Type rt1 = Type.getType(nameAndDesc.substring(i));
      Type rt2 = Type.getType(invokedMethod.substring(i));

      return Utilities.getClassForType(rt2).isAssignableFrom(Utilities.getClassForType(rt1));
   }

   public boolean isEquivalentInstance(Object mock, Map<Object, Object> instanceMap)
   {
      return mock == instance || mock == instanceMap.get(instance);
   }

   // Creation of AssertionError instances for invocation mismatch reporting //////////////////////

   public ExpectedInvocation(Object mock, String classDesc, String methodNameAndDesc, Object[] args)
   {
      instance = mock;
      matchInstance = false;
      arguments = new InvocationArguments(0, classDesc, methodNameAndDesc, args);
      invocationCause = null;
   }

   public AssertionError errorForUnexpectedInvocation()
   {
      return newErrorWithCause("Unexpected invocation", "Unexpected invocation of");
   }

   private AssertionError newErrorWithCause(String title, String message)
   {
      String errorMessage = message + toString();

      if (customErrorMessage != null) {
         errorMessage = customErrorMessage + "\n" + errorMessage;
      }

      AssertionError error = new AssertionError(errorMessage);

      if (invocationCause != null) {
         invocationCause.defineCause(title, error);
      }

      return error;
   }

   public AssertionError errorForMissingInvocation()
   {
      return newErrorWithCause("Missing invocation",  "Missing invocation of");
   }

   public AssertionError errorForMissingInvocations(int totalMissing)
   {
      String plural = totalMissing == 1 ? "" : "s";
      return newErrorWithCause(
         "Missing invocations", "Missing " + totalMissing + " invocation" + plural + " to");
   }

   public AssertionError errorForUnexpectedInvocation(
      Object mock, String invokedClassDesc, String invokedMethod)
   {
      String instanceDescription =
         mock == null ? "" : "\non instance: " + Utilities.objectIdentity(mock);

      return newErrorWithCause(
         "Unexpected invocation",
         "Unexpected invocation of:\n" + new MethodFormatter(invokedClassDesc, invokedMethod) +
         instanceDescription + "\nwhen was expecting an invocation of");
   }

   public AssertionError errorForUnexpectedInvocations(int totalUnexpected)
   {
      String plural = totalUnexpected == 1 ? "" : "s";
      return newErrorWithCause(
         "Unexpected invocations", totalUnexpected + " unexpected invocation" + plural + " to");
   }

   @Override
   public String toString()
   {
      String desc = arguments.toString();

      if (instance != null) {
         desc += "\non mock instance: " + Utilities.objectIdentity(instance);
      }

      return desc;
   }

   // Default return value ////////////////////////////////////////////////////////////////////////

   public Object getDefaultValueForReturnType(TestOnlyPhase phase)
   {
      if (defaultReturnValue == UNDEFINED_DEFAULT_RETURN) {
         String returnTypeDesc = DefaultValues.getReturnTypeDesc(getMethodNameAndDescription());
         defaultReturnValue = DefaultValues.computeForType(returnTypeDesc);

         if (defaultReturnValue == null && returnTypeDesc.charAt(0) == 'L') {
            String mockedTypeDesc = getClassDesc();
            Object cascadedMock = 
               MockedTypeCascade.getMock(mockedTypeDesc, instance, returnTypeDesc);

            if (cascadedMock != null) {
               if (phase != null) {
                  phase.setNextInstanceToMatch(cascadedMock);
               }

               defaultReturnValue = cascadedMock;
            }
         }
      }

      return defaultReturnValue;
   }
}
