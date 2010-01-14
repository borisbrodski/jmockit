/*
 * JMockit Verifications
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
package mockit;

import java.lang.reflect.*;
import java.util.*;

import mockit.internal.state.*;
import mockit.internal.expectations.*;
import mockit.internal.util.*;

/**
 * Base class whose subclasses are defined in test code, and whose instances define a set of
 * invocations on mocks to be verified against the actual invocations executed during the replay
 * phase of the test.
 * The order of the invocations is not relevant, and any subset of the potential invocations in the
 * replay phase can be verified (ie, not all of them need to be verified on each use of this class).
 * <p/>
 * Since each subclass will typically take the form of an anonymous class with no methods but only
 * an instance initialization block, we name such constructs <em>verification blocks</em>.
 * In the particular case of using this class directly, we have an <em>unordered</em> verification
 * block.
 * <p/>
 * Such blocks can appear alone in a test or in conjunction with
 * {@linkplain Expectations strict expectation blocks} which record at least one non-strict
 * expectation, and/or {@linkplain NonStrictExpectations non-strict expectation blocks}.
 * (Strict expectations are unconditionally and automatically verified as invocations occur in the
 * replay phase, and at the end of the test to account for any missing invocations.)
 * <p/>
 * Note that while an expectation block can appear only <em>before</em> the replay phase of the
 * test, a verification block can appear only <em>after</em> that phase.
 * <p/>
 * For an invocation inside a verification block to succeed (ie, pass verification), a
 * corresponding invocation must have occurred during the replay phase of the test, at least once.
 * Such an invocation may or may not have been previously recorded in an expectation block.
 * <p/>
 * The mocked types used inside the verification block can be all the ones that are in scope: mock
 * fields of the test class, and mock parameters of the test method. In addition, local mock fields
 * declared inside expectation blocks can be <em>imported</em> into the verification block by
 * declaring a field of the desired mocked type inside this block (not necessarily with the same
 * name as the imported mock field, although that is recommended for clarity).
 * <p/>
 * Just like it is valid to have multiple expectation blocks in a test, it is also valid to have
 * multiple (non-nested) verification blocks. The relative order of the blocks is not relevant.
 * Such blocks can be of different types. (Typically, when using multiple verification blocks there
 * will be a mix of ordered and unordered ones.)
 * <p/>
 * <a href="http://jmockit.googlecode.com/svn/trunk/www/tutorial/BehaviorBasedTesting.html#verification">Tutorial</a>
 *
 * @see Expectations#notStrict()
 * @see NonStrict
 * @see #Verifications()
 * @see #Verifications(int)
 */
public class Verifications extends Invocations
{
   final VerificationPhase verificationPhase;

   /**
    * Begins verification on the mocks invoked during the replay phase of the test.
    */
   protected Verifications()
   {
      this(false);
   }

   /**
    * Begins verification on the mocks invoked during the replay phase of the test, considering that
    * such invocations occurred in a given number of iterations.
    * <p/>
    * The effect of specifying a number of iterations larger than 1 (one) is equivalent to
    * multiplying by that number the lower and upper invocation count limits for each invocation
    * inside the verification block.
    * 
    * @param numberOfIterations the positive number of iterations for the whole set of invocations
    * verified inside the block; when not specified, 1 (one) iteration is assumed
    */
   protected Verifications(int numberOfIterations)
   {
      this(false);
      verificationPhase.setNumberOfIterations(numberOfIterations);
   }

   Verifications(boolean inOrder)
   {
      RecordAndReplayExecution instance =
         TestRun.getExecutingTest().getRecordAndReplayForVerifications();

      Map<Type,Object> availableLocalMocks = instance.getLocalMocks();

      if (!availableLocalMocks.isEmpty()) {
         importMocksIntoLocalFields(getClass(), availableLocalMocks);
      }

      verificationPhase = instance.startVerifications(inOrder);
   }

   private void importMocksIntoLocalFields(Class<?> ownerClass, Map<Type, Object> localMocks)
   {
      Field[] fields = ownerClass.getDeclaredFields();

      for (Field fieldToImport : fields) {
         if (!Modifier.isFinal(fieldToImport.getModifiers())) {
            importMockIntoLocalField(localMocks, fieldToImport);
         }
      }

      Class<?> superClass = ownerClass.getSuperclass();

      if (
         superClass != Verifications.class && superClass != VerificationsInOrder.class &&
         superClass != FullVerifications.class && superClass != FullVerificationsInOrder.class
      ) {
         importMocksIntoLocalFields(superClass, localMocks);
      }
   }

   private void importMockIntoLocalField(Map<Type, Object> localMocks, Field field)
   {
      Type mockedType = field.getGenericType();
      Object owner = localMocks.get(mockedType);

      if (owner != null) {
         Object mock = Utilities.getField(owner.getClass(), mockedType, owner);
         Utilities.setFieldValue(field, this, mock);
      }
   }

   @Override
   final VerificationPhase getCurrentPhase()
   {
      return verificationPhase;
   }
}
