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
package mockit.internal.expectations;

final class FailureState
{
   private final Thread testThread;
   private AssertionError errorThrownInAnotherThread;

   /**
    * Holds an error associated to an ExpectedInvocation that is to be reported to the user.
    * <p/>
    * This field is also set if and when an unexpected invocation is detected, so that any future
    * invocations in this same phase execution can rethrow the original error instead of throwing a
    * new one, which would hide the original.
    * Such a situation can happen when test code or the code under test contains a "catch" or
    * "finally" block where a mock invocation is made after a previous such invocation in the "try"
    * block already failed.
    */
   private AssertionError errorThrown;

   FailureState()
   {
      testThread = Thread.currentThread();
   }

   AssertionError getErrorThrown() { return errorThrown; }
   void setErrorThrown(AssertionError error) { errorThrown = error; }
   void clearErrorThrown() { errorThrown = null; }

   void reportErrorThrownIfAny()
   {
      if (errorThrown != null) {
         if (testThread == Thread.currentThread()) {
            throw errorThrown;
         }
         else {
            errorThrownInAnotherThread = errorThrown;
         }
      }
   }

   AssertionError getErrorThrownInAnotherThreadIfAny(AssertionError errorFromTestThread)
   {
      return errorThrownInAnotherThread == null ? errorFromTestThread : errorThrownInAnotherThread;
   }
}
