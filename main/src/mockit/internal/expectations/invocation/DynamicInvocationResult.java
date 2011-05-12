/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations.invocation;

import java.lang.reflect.*;

import mockit.*;
import mockit.internal.expectations.*;
import mockit.internal.util.*;

abstract class DynamicInvocationResult extends InvocationResult
{
   final Object targetObject;
   Method methodToInvoke;
   int numberOfRegularParameters;
   private boolean hasInvocationParameter;

   DynamicInvocationResult(Object targetObject, Method methodToInvoke)
   {
      this.targetObject = targetObject;
      this.methodToInvoke = methodToInvoke;

      if (methodToInvoke != null) {
         determineWhetherMethodToInvokeHasInvocationParameter();
      }
   }

   final void determineWhetherMethodToInvokeHasInvocationParameter()
   {
      Class<?>[] parameters = methodToInvoke.getParameterTypes();
      int n = parameters.length;
      hasInvocationParameter = n > 0 && parameters[0] == Invocation.class;
      numberOfRegularParameters = hasInvocationParameter ? n - 1 : n;
   }

   public final Object invokeMethodOnTargetObject(
      Object mockOrRealObject, InvocationConstraints constraints, Object[] args)
   {
      Object result;

      if (hasInvocationParameter) {
         result = invokeMethodWithContext(mockOrRealObject, constraints, args);
      }
      else {
         result = executeMethodToInvoke(args);
      }

      return result;
   }

   private Object invokeMethodWithContext(Object mockOrRealObject, InvocationConstraints constraints, Object[] args)
   {
      Invocation invocation =
         new DelegateInvocation(
            mockOrRealObject, constraints.invocationCount, constraints.minInvocations, constraints.maxInvocations);
      Object[] delegateArgs = getArgumentsWithExtraInvocationObject(invocation, args);

      try {
         return executeMethodToInvoke(delegateArgs);
      }
      finally {
         constraints.setLimits(invocation.getMinInvocations(), invocation.getMaxInvocations());
      }
   }

   private Object[] getArgumentsWithExtraInvocationObject(Invocation invocation, Object[] args)
   {
      Object[] delegateArgs = new Object[args.length + 1];
      delegateArgs[0] = invocation;
      System.arraycopy(args, 0, delegateArgs, 1, args.length);
      return delegateArgs;
   }
   
   private Object executeMethodToInvoke(Object[] args)
   {
      if (!RecordAndReplayExecution.LOCK.isHeldByCurrentThread()) {
         return Utilities.invoke(targetObject, methodToInvoke, args);
      }

      RecordAndReplayExecution.LOCK.unlock();

      try {
         return Utilities.invoke(targetObject, methodToInvoke, args);
      }
      finally {
         //noinspection LockAcquiredButNotSafelyReleased
         RecordAndReplayExecution.LOCK.lock();
      }
   }
}