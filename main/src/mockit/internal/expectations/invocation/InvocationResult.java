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
package mockit.internal.expectations.invocation;

import java.lang.reflect.*;
import java.util.*;

import mockit.*;
import mockit.internal.util.*;
import mockit.external.asm.Type;

abstract class InvocationResult
{
   InvocationResult next;

   abstract Object produceResult(
      ExpectedInvocation invocation, InvocationConstraints constraints, Object[] args)
      throws Throwable;

   static final class ReturnValueResult extends InvocationResult
   {
      private final Object returnValue;

      ReturnValueResult(Object returnValue) { this.returnValue = returnValue; }

      @Override
      Object produceResult(
         ExpectedInvocation invocation, InvocationConstraints constraints, Object[] args)
      {
         return returnValue;
      }
   }

   static final class ThrowableResult extends InvocationResult
   {
      private final Throwable throwable;

      ThrowableResult(Throwable throwable) { this.throwable = throwable; }

      @Override
      Object produceResult(
         ExpectedInvocation invocation, InvocationConstraints constraints, Object[] args)
         throws Throwable
      {
         throwable.fillInStackTrace();
         throw throwable;
      }
   }

   static final class DelegatedResult extends InvocationResult
   {
      private final Delegate delegate;
      private Method delegateMethod;
      private boolean hasInvocationParameter;
      private Object[] args;

      DelegatedResult(Delegate delegate)
      {
         this.delegate = delegate;

         Method[] declaredMethods = delegate.getClass().getDeclaredMethods();

         if (declaredMethods.length == 1) {
            delegateMethod = declaredMethods[0];
            determineWhetherDelegateMethodHasInvocationParameter();
         }
         else {
            delegateMethod = null;
         }
      }

      private void determineWhetherDelegateMethodHasInvocationParameter()
      {
         Class<?>[] parameters = delegateMethod.getParameterTypes();
         hasInvocationParameter = parameters.length > 0 && parameters[0] == Invocation.class;
      }

      @Override
      Object produceResult(
         ExpectedInvocation invocation, InvocationConstraints constraints, Object[] args)
         throws Throwable
      {
         this.args = args;

         if (delegateMethod == null) {
            String methodName = adaptNameAndArgumentsForDelegate(invocation);
            delegateMethod = Utilities.findCompatibleMethod(delegate, methodName, args);
            determineWhetherDelegateMethodHasInvocationParameter();
         }

         Object result;

         if (hasInvocationParameter) {
            result = executeDelegateWithInvocationContext(constraints, delegateMethod);
         }
         else {
            result = Utilities.invoke(delegate, delegateMethod, args);
         }

         return result;
      }

      private String adaptNameAndArgumentsForDelegate(ExpectedInvocation invocation)
      {
         String methodNameAndDesc = invocation.getMethodNameAndDescription();
         int leftParen = methodNameAndDesc.indexOf('(');

         replaceNullArgumentsWithClassObjectsIfAny(methodNameAndDesc, leftParen);

         String methodName = methodNameAndDesc.substring(0, leftParen);

         if ("<init>".equals(methodName)) {
            methodName = "$init";
         }

         return methodName;
      }

      private void replaceNullArgumentsWithClassObjectsIfAny(
         String methodNameAndDesc, int leftParen)
      {
         Type[] argTypes = null;

         for (int i = 0; i < args.length; i++) {
            if (args[i] == null) {
               if (argTypes == null) {
                  String methodDesc = methodNameAndDesc.substring(leftParen);
                  argTypes = Type.getArgumentTypes(methodDesc);
               }

               args[i] = Utilities.getClassForType(argTypes[i]);
            }
         }
      }

      private Object executeDelegateWithInvocationContext(
         InvocationConstraints constraints, Method delegateMethod)
      {
         Invocation invocation =
            new DelegateInvocation(
               constraints.invocationCount, constraints.minInvocations, constraints.maxInvocations);
         Object[] delegateArgs =
            getDelegateArgumentsWithExtraInvocationObject(invocation);

         try {
            return Utilities.invoke(delegate, delegateMethod, delegateArgs);
         }
         finally {
            constraints.setLimits(invocation.getMinInvocations(), invocation.getMaxInvocations());
         }
      }

      private Object[] getDelegateArgumentsWithExtraInvocationObject(Invocation invocation)
      {
         Object[] delegateArgs = new Object[args.length + 1];
         delegateArgs[0] = invocation;
         System.arraycopy(args, 0, delegateArgs, 1, args.length);
         return delegateArgs;
      }
   }

   static final class DeferredReturnValues extends InvocationResult
   {
      private final Iterator<?> values;

      DeferredReturnValues(Iterator<?> values) { this.values = values; }

      @Override
      Object produceResult(
         ExpectedInvocation invocation, InvocationConstraints constraints, Object[] args)
         throws Throwable
      {
         return values.hasNext() ? values.next() : null;
      }
   }
}
