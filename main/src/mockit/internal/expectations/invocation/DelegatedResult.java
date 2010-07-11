/*
 * JMockit Expectations
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

import java.lang.reflect.*;

import mockit.*;
import mockit.external.asm.Type;
import mockit.internal.util.*;

final class DelegatedResult extends DynamicInvocationResult
{
   DelegatedResult(Delegate delegate)
   {
      super(delegate, findDelegateMethodIfSingle(delegate));
   }

   private static Method findDelegateMethodIfSingle(Delegate delegate)
   {
      Method[] declaredMethods = delegate.getClass().getDeclaredMethods();
      return declaredMethods.length == 1 ? declaredMethods[0] : null;
   }

   @Override
   Object produceResult(
      Object invokedObject, ExpectedInvocation invocation, InvocationConstraints constraints, 
      Object[] args)
      throws Throwable
   {
      if (methodToInvoke == null) {
         String methodName = adaptNameAndArgumentsForDelegate(invocation, args);
         methodToInvoke = Utilities.findCompatibleMethod(targetObject, methodName, args);
         determineWhetherMethodToInvokeHasInvocationParameter();
      }
      else if (numberOfRegularParameters == 0 && args.length > 0) {
         //noinspection AssignmentToMethodParameter
         args = Utilities.NO_ARGS;
      }

      return invokeMethodOnTargetObject(invokedObject, constraints, args);
   }

   private String adaptNameAndArgumentsForDelegate(ExpectedInvocation invocation, Object[] args)
   {
      String methodNameAndDesc = invocation.getMethodNameAndDescription();
      int leftParen = methodNameAndDesc.indexOf('(');

      replaceNullArgumentsWithClassObjectsIfAny(methodNameAndDesc, leftParen, args);

      String methodName = methodNameAndDesc.substring(0, leftParen);

      if ("<init>".equals(methodName)) {
         methodName = "$init";
      }

      return methodName;
   }

   private void replaceNullArgumentsWithClassObjectsIfAny(
      String methodNameAndDesc, int leftParen, Object[] args)
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
}
