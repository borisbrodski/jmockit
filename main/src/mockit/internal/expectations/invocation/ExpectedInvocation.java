/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations.invocation;

import java.util.*;

import mockit.external.asm.Type;

import mockit.internal.expectations.*;
import mockit.internal.state.*;
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
   private Object cascadedMock;

   public ExpectedInvocation(
      Object mock, int access, String mockedClassDesc, String mockNameAndDesc, boolean matchInstance, Object[] args)
   {
      this(mock, access, mockedClassDesc, mockNameAndDesc, matchInstance, null, null, args);
   }

   public ExpectedInvocation(
      Object mock, int access, String mockedClassDesc, String mockNameAndDesc, boolean matchInstance,
      String genericSignature, String exceptions, Object[] args)
   {
      instance = mock;
      this.matchInstance = matchInstance;
      arguments = new InvocationArguments(access, mockedClassDesc, mockNameAndDesc, genericSignature, exceptions, args);
      invocationCause = new ExpectationError();
      determineDefaultReturnValueFromMethodSignature();
   }

   private void determineDefaultReturnValueFromMethodSignature()
   {
      String nameAndDesc = getMethodNameAndDescription();

      if ("equals(Ljava/lang/Object;)Z".equals(nameAndDesc)) {
         defaultReturnValue = instance == getArgumentValues()[0];
      }
      else if ("hashCode()I".equals(nameAndDesc)) {
         defaultReturnValue = System.identityHashCode(instance);
      }
      else if ("toString()Ljava/lang/String;".equals(nameAndDesc)) {
         defaultReturnValue = Utilities.objectIdentity(instance);
      }
      else {
         defaultReturnValue = UNDEFINED_DEFAULT_RETURN;
      }
   }

   // Simple getters //////////////////////////////////////////////////////////////////////////////////////////////////

   public String getClassDesc() { return arguments.classDesc; }
   public String getClassName() { return getClassDesc().replace('/', '.'); }
   public String getMethodNameAndDescription() { return arguments.methodNameAndDesc; }
   public Object[] getArgumentValues() { return arguments.getValues(); }

   // Matching based on instance or mocked type ///////////////////////////////////////////////////////////////////////

   public boolean isMatch(String invokedClassDesc, String invokedMethod)
   {
      return invokedClassDesc.equals(getClassDesc()) && isMatchingMethod(invokedMethod);
   }

   public boolean isMatch(Object replayInstance, Map<Object, Object> instanceMap)
   {
      return
         getMethodNameAndDescription().charAt(0) == '<' ||
         !matchInstance || isEquivalentInstance(replayInstance, instanceMap);
   }

   public boolean isMatch(
      Object replayInstance, String invokedClassDesc, String invokedMethod, Map<Object, Object> instanceMap)
   {
      return
         isMatch(invokedClassDesc, invokedMethod) &&
         (getMethodNameAndDescription().charAt(0) == '<' ||
          !matchInstance || isEquivalentInstance(replayInstance, instanceMap));
   }

   private boolean isMatchingMethod(String invokedMethod)
   {
      String nameAndDesc = getMethodNameAndDescription();
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

   public boolean isEquivalentInstance(Object mockedInstance, Map<Object, Object> instanceMap)
   {
      return
         mockedInstance == instance || mockedInstance == instanceMap.get(instance) ||
         TestRun.getExecutingTest().isInvokedInstanceEquivalentToCapturedInstance(instance, mockedInstance);
   }

   // Creation of AssertionError instances for invocation mismatch reporting //////////////////////////////////////////

   public ExpectedInvocation(Object mockedInstance, String classDesc, String methodNameAndDesc, Object[] args)
   {
      instance = mockedInstance;
      matchInstance = false;
      arguments = new InvocationArguments(0, classDesc, methodNameAndDesc, null, null, args);
      invocationCause = null;
   }

   public AssertionError errorForUnexpectedInvocation()
   {
      return newErrorWithCause("Unexpected invocation", "Unexpected invocation of" + this);
   }

   private AssertionError newErrorWithCause(String titleForCause, String initialMessage)
   {
      String errorMessage = initialMessage;

      if (customErrorMessage != null) {
         errorMessage = customErrorMessage + "\n" + errorMessage;
      }

      AssertionError error = new AssertionError(errorMessage);

      if (invocationCause != null) {
         invocationCause.defineCause(titleForCause, error);
      }

      return error;
   }

   public AssertionError errorForMissingInvocation()
   {
      return newErrorWithCause("Missing invocation", "Missing invocation of" + this);
   }

   public AssertionError errorForMissingInvocations(int totalMissing)
   {
      String message = "Missing " + totalMissing + invocationsToThis(totalMissing);
      return newErrorWithCause("Missing invocations", message);
   }

   private String invocationsToThis(int invocations)
   {
      String prefix = invocations == 1 ? " invocation to" : " invocations to";
      return prefix + this;
   }

   public AssertionError errorForUnexpectedInvocation(Object mock, String invokedClassDesc, String invokedMethod)
   {
      String instanceDescription = mock == null ? "" : "\non instance: " + Utilities.objectIdentity(mock);
      String message =
         "Unexpected invocation of:\n" + new MethodFormatter(invokedClassDesc, invokedMethod) + instanceDescription +
         "\nwhen was expecting an invocation of" + this;
      return newErrorWithCause("Unexpected invocation", message);
   }

   public AssertionError errorForUnexpectedInvocations(int totalUnexpected)
   {
      String message = totalUnexpected + " unexpected" + invocationsToThis(totalUnexpected);
      return newErrorWithCause("Unexpected invocations", message);
   }

   public AssertionError errorForUnexpectedInvocationBeforeAnother(ExpectedInvocation another)
   {
      return newErrorWithCause("Unexpected invocation" + this, "Unexpected invocation before" + another);
   }

   public AssertionError errorForUnexpectedInvocationAfterAnother(ExpectedInvocation another)
   {
      return newErrorWithCause("Unexpected invocation" + this, "Unexpected invocation after" + another);
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

   // Default return value ////////////////////////////////////////////////////////////////////////////////////////////

   public Object getDefaultValueForReturnType(TestOnlyPhase phase)
   {
      if (defaultReturnValue == UNDEFINED_DEFAULT_RETURN) {
         defaultReturnValue =
            TestRun.getExecutingTest().defaultResults.get(arguments.getGenericSignature(), arguments.exceptions);

         if (defaultReturnValue == null) {
            String returnTypeDesc = DefaultValues.getReturnTypeDesc(arguments.methodNameAndDesc);
            defaultReturnValue = DefaultValues.computeForType(returnTypeDesc);

            if (defaultReturnValue == null && returnTypeDesc.charAt(0) == 'L') {
               produceCascadedMockIfApplicable(phase, returnTypeDesc);
            }
         }
      }

      return defaultReturnValue;
   }

   private void produceCascadedMockIfApplicable(TestOnlyPhase phase, String returnTypeDesc)
   {
      String mockedTypeDesc = getClassDesc();
      cascadedMock = MockedTypeCascade.getMock(mockedTypeDesc, instance, returnTypeDesc);

      if (cascadedMock != null) {
         if (phase != null) {
            phase.setNextInstanceToMatch(cascadedMock);
         }

         defaultReturnValue = cascadedMock;
      }
   }

   public Object getCascadedMock() { return cascadedMock; }

   public void copyDefaultReturnValue(ExpectedInvocation other) { defaultReturnValue = other.defaultReturnValue; }
}
