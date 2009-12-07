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

import java.util.*;

import mockit.external.hamcrest.*;
import mockit.external.hamcrest.core.*;

final class ExpectedInvocationWithMatchers extends ExpectedInvocation
{
   List<Matcher<?>> invocationArgMatchers;

   ExpectedInvocationWithMatchers(
      Object recordedInstance, int methodAccess, String classDesc, String methodNameAndDesc,
      boolean matchInstance, Object[] recordedCallArgs, List<Matcher<?>> argMatchers,
      Map<Object, Object> recordToReplayInstanceMap)
   {
      super(
         recordedInstance, methodAccess, classDesc, methodNameAndDesc, matchInstance,
         recordedCallArgs, recordToReplayInstanceMap);
      invocationArgMatchers = argMatchers;
   }

   Object[] prepareArgumentsForVerification(Object[] argsToVerify, List<Matcher<?>> argMatchers)
   {
      Object[] replayArgs = invocationArgs;
      invocationArgs = argsToVerify;
      invocationArgMatchers = argMatchers;
      return replayArgs;
   }

   @Override
   AssertionError assertThatInvocationArgumentsMatch(Object[] replayArgs)
   {
      if (invocationArgMatchers == null) {
         return super.assertThatInvocationArgumentsMatch(replayArgs);
      }

      int argCount = replayArgs.length;
      Object[] replayVarArgs = replayArgs;
      int varargsCount = 0;

      if (methodWithVarargs) {
         argCount--;
         replayVarArgs = getVarArgs(replayArgs);
         varargsCount = replayVarArgs.length;
      }

      int n = argCount + varargsCount;

      for (int i = 0; i < n; i++) {
         Object actual = i < argCount ? replayArgs[i] : replayVarArgs[i - argCount];
         Matcher<?> expected =
            i < invocationArgMatchers.size() ? invocationArgMatchers.get(i) : null;

         if (expected == null) {
            Object invocationArg = invocationArgs[i];
            expected =
               invocationArg == null ? new IsAnything() : new IsEqual<Object>(invocationArg);
         }

         if (!expected.matches(actual)) {
            return argumentMismatchErrorMessage(i, expected, actual);
         }
      }

      return null;
   }

   @Override
   String getParameterValueAsString(Object parameterValue)
   {
      if (parameterValue instanceof SelfDescribing || parameterValue instanceof Description) {
         return parameterValue.toString();
      }

      return super.getParameterValueAsString(parameterValue);
   }
}
