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

import mockit.*;
import mockit.internal.util.*;
import org.objectweb.asm2.Type;

abstract class InvocationResult {
   InvocationResult next;

   abstract Object produceResult(Expectation expectation, Object[] args) throws Throwable;

   static final class ReturnValueResult extends InvocationResult {
      private final Object returnValue;

      ReturnValueResult(Object returnValue) {
         this.returnValue = returnValue;
      }

      @Override
      Object produceResult(Expectation expectation, Object[] args) {
         return returnValue;
      }
   }

   static final class ThrowableResult extends InvocationResult {
      private final Throwable throwable;

      ThrowableResult(Throwable throwable) {
         this.throwable = throwable;
      }

      @Override
      Object produceResult(Expectation expectation, Object[] args)
            throws Throwable {
         throwable.fillInStackTrace();
         throw throwable;
      }
   }

   static final class DelegatedResult extends InvocationResult {
      private final Delegate delegate;
      private final Method singleMethod;

      DelegatedResult(Delegate delegate) {
         this.delegate = delegate;

         Method[] declaredMethods = delegate.getClass().getDeclaredMethods();
         singleMethod = declaredMethods.length == 1 ? declaredMethods[0] : null;
      }

      @Override
      Object produceResult(Expectation expectation, Object[] args) throws Throwable {
         String methodNameAndDesc = expectation.expectedInvocation
               .getMethodNameAndDescription();
         int leftParen = methodNameAndDesc.indexOf('(');

         if (singleMethod == null) {
            replaceNullArgumentsWithClassObjectsIfAny(args, methodNameAndDesc, leftParen);
         }

         String methodName = methodNameAndDesc.substring(0, leftParen);

         if ("<init>".equals(methodName)) {
            methodName = "$init";
         }

         Invocation invocation = getInvocationFromConstraints(expectation.constraints);
         Object[] argsPlus = new Object[args.length + 1];
         argsPlus[0] = invocation;
         System.arraycopy(args, 0, argsPlus, 1, args.length);

         Object result = null;
         try {
            try {
               if (singleMethod != null) {
                  result = Utilities.invoke(delegate, singleMethod, argsPlus);
               } else {
                  result = Utilities.invoke(delegate, methodName, argsPlus);
               }
            } catch (IllegalArgumentException iae) {
               if (singleMethod != null) {
                  result = Utilities.invoke(delegate, singleMethod, args);
               } else {
                  result = Utilities.invoke(delegate, methodName, args);
               }
            }
         } finally {
            setConstraintsFromInvocation(invocation, expectation.constraints);
         }
         return result;
      }

      private Invocation getInvocationFromConstraints(InvocationConstraints constraints) {
         Invocation invocation = new DelegateInvocation(constraints.invocationCount,
               constraints.minInvocations, constraints.maxInvocations);
         return invocation;
      }

      private void setConstraintsFromInvocation(Invocation invocation, InvocationConstraints constraints) {
         constraints.setLimits(invocation.getMinInvocations(), invocation
               .getMaxInvocations());
      }

      private void replaceNullArgumentsWithClassObjectsIfAny(Object[] args, String methodNameAndDesc, int leftParen) {
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
   }

   static final class DeferredReturnValues extends InvocationResult {
      private final Iterator<?> values;

      DeferredReturnValues(Iterator<?> values) {
         this.values = values;
      }

      @Override
      Object produceResult(Expectation expectation, Object[] args) throws Throwable {
         return values.hasNext() ? values.next() : null;
      }
   }
}
