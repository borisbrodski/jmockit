/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import java.lang.reflect.*;
import java.util.*;

import mockit.internal.state.*;
import mockit.internal.expectations.*;
import mockit.internal.util.*;

/**
 * Base class whose subclasses are defined in test code, and whose instances define a set of invocations on mocked
 * types/instances to be verified against the actual invocations executed during the replay phase of the test.
 * The order of the invocations is not relevant, and any subset of the potential invocations in the replay phase can be
 * verified (ie, not all of them need to be verified on each use of this class).
 * <p/>
 * Since each user-defined subclass will typically take the form of an anonymous class with no methods but only an
 * instance initialization block, we name such constructs <em>verification blocks</em>.
 * When extending this class directly (as opposed to extending one of the three specializations available in the API),
 * we have an <em>unordered</em> verification block.
 * <p/>
 * Such blocks can appear alone in a test or (more typically) in conjunction with
 * {@linkplain NonStrictExpectations non-strict expectation blocks}.
 * It's also possible to have an {@linkplain Expectations strict expectation block} in the same test, provided at least
 * one non-strict expectation is recorded in it (strict expectations are <em>implicitly</em> verified as invocations
 * occur in the replay phase, and at the end of the test to account for any missing invocations - they cannot be
 * verified explicitly).
 * <p/>
 * Note that while an expectation block can appear only <em>before</em> the replay phase of the test, a verification
 * block can appear only <em>after</em> that phase.
 * <p/>
 * For an invocation inside a verification block to succeed (ie, pass verification), a corresponding invocation must
 * have occurred during the replay phase of the test, <em>at least once</em>.
 * Such an invocation may or may not have been previously recorded in an expectation block.
 * This is only the <em>default</em> verification behavior, though. Just like with recorded expectations, it's possible
 * to specify different invocation count constraints through the {@link #times}, {@link #minTimes}, and
 * {@link #maxTimes} fields.
 * <p/>
 * The mocked types used inside the verification block can be all the ones that are in scope: mock fields of the test
 * class and mock parameters of the test method. In addition, local mock fields declared inside expectation blocks can
 * be <em>imported</em> into the verification block by declaring a field of the desired mocked type inside this block -
 * though not necessarily with the same name as the imported mock field, although it's recommended for clarity.
 * <p/>
 * Just like it is valid to have multiple expectation blocks in a test, it is also valid to have multiple (non-nested)
 * verification blocks. The relative order of the blocks is not relevant.
 * Such blocks can be of different types. (Typically, when using multiple verification blocks there will be a mix of
 * ordered and unordered ones.)
 * <p/>
 * <a href="http://jmockit.googlecode.com/svn/trunk/www/tutorial/BehaviorBasedTesting.html#verification">In the Tutorial</a>
 *
 * @see Expectations#notStrict()
 * @see NonStrict
 * @see #Verifications()
 * @see #Verifications(int)
 */
public abstract class Verifications extends Invocations
{
   final BaseVerificationPhase verificationPhase;

   /**
    * Begins verification on the mocked types/instances invoked during the replay phase of the test.
    */
   protected Verifications()
   {
      this(false);
   }

   /**
    * Begins verification on the mocked types/instances invoked during the replay phase of the test, considering that
    * such invocations occurred in a given number of iterations.
    * <p/>
    * The effect of specifying a (positive) number of iterations is equivalent to setting to that number the lower and
    * upper invocation count limits for each expectation verified inside the block.
    * If, however, the lower/upper limit is explicitly specified for an expectation, the given number of iterations
    * becomes a multiplier.
    * When not specified, at least one matching invocation will be required to have occurred; therefore, specifying
    * <em>1 (one)</em> iteration is different from not specifying the number of iterations at all.
    * <p/>
    * <a href="http://jmockit.googlecode.com/svn/trunk/www/tutorial/BehaviorBasedTesting.html#iterations">In the Tutorial</a>
    *
    * @param numberOfIterations the positive number of iterations for the whole set of invocations verified inside the
    * block
    *
    * @see #times
    * @see #minTimes
    * @see #maxTimes
    */
   protected Verifications(int numberOfIterations)
   {
      this(false);
      verificationPhase.setNumberOfIterations(numberOfIterations);
   }

   Verifications(boolean inOrder)
   {
      RecordAndReplayExecution instance = TestRun.getExecutingTest().getRecordAndReplayForVerifications();

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
   final BaseVerificationPhase getCurrentPhase()
   {
      return verificationPhase;
   }
}
