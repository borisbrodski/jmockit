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
package mockit;

import java.util.*;

import mockit.internal.expectations.*;

/**
 * Base class whose subclasses are defined in test code, and whose instances define a set of
 * expected method/constructor invocations on the mocked types (classes or interfaces) declared
 * through one or more <em>mock fields</em> and/or <em>mock parameters</em>.
 * A (local) mock field is any field declared in a subclass which is either non-private or annotated
 * with {@link Mocked}.
 * <p/>
 * Typically, this class is used by extending it with <em>anonymous inner classes</em>
 * (named as <em>expectation blocks</em>) inside test methods, which record expectations on the
 * mocked types by calling instance methods on mock fields/parameters, static methods on mocked
 * classes, and/or constructors of mocked classes.
 * Arguments passed in such calls are later matched to the actual arguments passed from the code
 * under test.
 * <p/>
 * Any mock fields declared within an expectation block will only be accessible for mock invocations
 * inside this particular block.
 * An alternative is to declare mock fields of the <em>test class</em> itself, so that all of its
 * test methods can share the same mock fields. Such fields need to be annotated with 
 * {@code @Mocked}, though.
 * <p/>
 * There are several API fields and methods (all of them {@code protected final}) which the
 * expectation block can use for recording desired return values and exceptions/errors to be thrown
 * (see {@link #result}), and for specifying argument matching constraints such as
 * {@link #withEqual(Object)}.
 * <p/>
 * Individual expectations are set during the <em>record phase</em>, and later exercised during the
 * <em>replay phase</em> of the test.
 * At the end of the test, the test runner will automatically assert that all recorded invocations 
 * were actually replayed as expected.
 * <p/>
 * Additional features and details about the process above are as follows:
 * <ul>
 * <li>
 * A <strong>mock field</strong> can be of any non-primitive type, including interfaces, abstract
 * classes, and concrete classes (even {@code final} classes).
 * An instance will be automatically created when the subclass gets instantiated, unless the field
 * is {@code final} (in which case, the test code itself will have the responsibility of
 * obtaining an appropriate instance). This instance will be a mock object that can be used from
 * that point forward; however, <strong>static methods</strong> and <strong>constructors</strong>
 * belonging to the mocked class and its super-classes will also be mocked, and any invocations made
 * to them will work just as a regular instance method invocation would.
 * </li>
 * <li>
 * Unless specified otherwise, all expectations defined inside an {@code Expectations} immediate
 * subclass will be <em>strict</em>, meaning that the recorded invocations are <em>expected</em> to
 * occur in the same order during the replay phase, and that non-recorded invocations are <em>not
 * allowed</em>.
 * This default behavior can be overridden for individual expectations through the
 * {@link #notStrict()} method, and for whole mocked types through the {@link NonStrict} annotation.
 * </li>
 * <li>
 * There is a set of API methods that allow the {@linkplain #newInstance(String, Class[], Object...)
 * instantiation of non-accessible (to the test) classes}, the
 * {@linkplain #invoke(Object, String, Object...) invocation of non-accessible methods}, and the
 * {@linkplain #setField(Object, String, Object) setting of non-accessible fields}.
 * Most tests shouldn't need these facilities, though.
 * </li>
 * <li>
 * A set of special API fields provides the ability to specify how many {@linkplain #times times} a
 * recorded invocation is expected to occur during replay, the {@linkplain #minTimes minimum
 * number of times} it's expected, or the {@linkplain #maxTimes maximum number of times} it will be
 * allowed to occur.
 * </li>
 * <li>
 * By default, the exact instance on which instance method invocations occur during the replay phase
 * is <em>not</em> verified to be the same as the instance used when recording the corresponding
 * expectation.
 * If such verification is needed, the {@link #onInstance(Object)} method should be used.
 * </li>
 * <li>
 * There are additional constructors which provide other features:
 * {@linkplain #Expectations(Object...) dynamic partial mocking}, and
 * {@linkplain #Expectations(int, Object...) iterated invocations}.
 * </li>
 * </ul>
 * <a href="http://jmockit.googlecode.com/svn/trunk/www/tutorial/BehaviorBasedTesting.html#expectation">Tutorial</a>
 *
 * @see #Expectations()
 */
public class Expectations extends Invocations
{
   private final RecordAndReplayExecution execution;

   /**
    * A value assigned to this field will be taken as the result for the current expectation.
    * <p/>
    * If the value is of type {@link Throwable} then it will be <em>thrown</em> when a matching
    * invocation occurs in the replay phase.
    * Otherwise, it's assumed to be a <em>return value</em> for a non-void method, and will be
    * returned at replay time from a matching invocation.
    * Attempting to return a value that is incompatible with the method return type will cause a
    * {@code ClassCastException} to be thrown at replay time.
    * <p/>
    * If the current expectation is for a method which actually <em>returns</em> an exception or
    * error (as opposed to <em>throwing</em> one), then the {@link #returns(Object)} method should
    * be used instead.
    * <p/>
    * If the value assigned to the field is of a type assignable to {@link java.util.Collection} or
    * to {@link java.util.Iterator}, then it is taken as a sequence of <em>consecutive results</em>
    * for the current expectation.
    * Another way to specify consecutive results is to simply write multiple consecutive assignments
    * to the field.
    *
    * @see #returns(Object)
    * @see #returns(Object, Object...)
    */
   protected static Object result;

   /**
    * Initializes this set of expectations, entering the <em>record</em> phase.
    * <p/>
    * For each associated {@linkplain Mocked mocked type}, the following tasks are performed:
    * <ol>
    * <li>
    * Redefines the <em>target class for mocking</em> derived from the mocked type.
    * </li>
    * <li>
    * If the declared type to be mocked is an abstract class, then generates a concrete subclass
    * with mock implementations for all inherited abstract methods.
    * </li>
    * <li>
    * If the mocked type is the declared type of a non-<code>final</code> instance field, then
    * creates and assigns a new mock instance to that field.
    * </li>
    * </ol>
    * After this, test code can start recording invocations to the mocked types and mock instances.
    * Each and every such call made from inside the expectation block is recorded.
    */
   protected Expectations()
   {
      execution = new RecordAndReplayExecution(this, (Object[]) null);
   }

   /**
    * Same as {@link #Expectations()}, except that one or more classes will be partially mocked
    * according to the expectations recorded in the expectation block.
    * Such classes are those directly specified as well as those to which any given instances
    * belong.
    * <p/>
    * During the replay phase, any invocations to one of these classes or instances will execute
    * real production code, unless a matching invocation was previously recorded as an expectation
    * inside the block.
    * <p/>
    * For a given <em>object</em> (of any valid mockable type) that is to be partially mocked, all
    * methods will be considered for mocking, from the concrete class of the given object up to but
    * not including {@code java.lang.Object}. The constructors of those classes, though, will not be
    * considered.
    * For a given {@code Class} object, on the other hand, both constructors and methods will be
    * considered for mocking, but only those belonging to the specified class.
    *
    * @param classesOrObjectsToBePartiallyMocked one or more classes or objects whose classes are
    * to be considered for partial mocking
    *
    * @throws IllegalArgumentException if given a class literal for an interface, an annotation, an
    * array, or a primitive/wrapper type, or given a value of such a type
    */
   protected Expectations(Object... classesOrObjectsToBePartiallyMocked)
   {
      execution = new RecordAndReplayExecution(this, classesOrObjectsToBePartiallyMocked);
   }

   /**
    * Identical to {@link #Expectations(Object...)}, but considering that the invocations inside the
    * block will occur in a given number of iterations.
    * <p/>
    * The effect of specifying a number of iterations larger than 1 (one) is equivalent to
    * duplicating (like in "copy & paste") the whole sequence of <em>strict</em> invocations in the
    * block.
    * For any <em>non-strict</em> invocation inside the same block, the effect will be equivalent to
    * multiplying the minimum and maximum invocation count by the specified number of iterations.
    * <p/>
    * It's also valid to have multiple expectation blocks for the same test, each with an arbitrary
    * number of iterations, and containing any mix of strict and non-strict expectations.
    * 
    * @param numberOfIterations the positive number of iterations for the whole set of invocations
    * recorded inside the block; when not specified, 1 (one) iteration is assumed
    */
   protected Expectations(int numberOfIterations, Object... classesOrObjectsToBePartiallyMocked)
   {
      this(classesOrObjectsToBePartiallyMocked);
      getCurrentPhase().setNumberOfIterations(numberOfIterations);
   }

   @Override
   final RecordPhase getCurrentPhase()
   {
      return execution.getRecordPhase();
   }

   // Methods for setting expected return values and thrown exceptions ////////////////////////////

   /**
    * Specifies that the previously recorded method invocation will return a given value.
    * <p/>
    * More than one return value can be specified for the same invocation by simply calling this
    * method multiple times, with the desired consecutive values to be later returned.
    * For an strict expectation, the maximum number of expected invocations is automatically
    * adjusted so that one invocation for each return value is allowed.
    * If even more invocations are explicitly allowed then the last recorded return value is used
    * for all remaining invocations during the replay phase.
    * <p/>
    * If the recorded invocation is for a method that does <em>not</em> return a
    * {@linkplain Collection collection} or {@linkplain Iterator iterator}, then a collection (of
    * any concrete type) or an iterator can be passed as argument to this method.
    * In the first case, the elements inside the collection will be successively returned by
    * sequential invocations to the recorded method, with the maximum number of invocations
    * automatically adjusted to the number of elements in the collection.
    * In the second case, each invocation to the recorded method will cause the next value from the
    * iterator to be returned, until no more elements remain, without any predefined upper limit.
    * For a recorded method that does return a collection or iterator, passing a collection/iterator
    * to this method will have the regular effect of causing the collection/iterator to be later
    * returned, as expected.
    * <p/>
    * For a non-void method, if no return value is recorded then all invocations to it will return
    * the appropriate default value according to the method return type:
    * <ul>
    * <li>Primitive: the standard default value is returned (ie {@code false} for
    * {@code boolean}, '\0' for {@code char}, {@code 0} for {@code int}, and so on).</li>
    * <li>{@code java.util.Collection} or {@code java.util.List}: returns
    * {@link Collections#EMPTY_LIST}</li>
    * <li>{@code java.util.Set}: returns {@link Collections#EMPTY_SET}.</li>
    * <li>{@code java.util.SortedSet}: returns an unmodifiable empty sorted set.</li>
    * <li>{@code java.util.Map}: returns {@link Collections#EMPTY_MAP}.</li>
    * <li>{@code java.util.SortedMap}: returns an unmodifiable empty sorted map.</li>
    * <li>A reference type (including {@code String} and wrapper types for primitives, and excluding
    * the exact collection types above): returns {@code null}.</li>
    * <li>An array type: an array with zero elements (empty) in each dimension is returned.</li>
    * </ul>
    * The actual value(s) to be returned can be determined at replay time through a
    * {@link Delegate} instance, typically created as an anonymous class at the point this
    * method is called.
    *
    * @param value the value to be returned when the method is replayed; must be compatible with the
    * method's return type
    *
    * @throws IllegalStateException if not currently recording an invocation
    * @throws IllegalArgumentException if the given return value is not {@code null} but the
    * preceding mock invocation is to a constructor or {@code void} method
    *
    * @see #result
    * @see #returns(Object, Object...)
    */
   protected final void returns(Object value)
   {
      getCurrentExpectation().addReturnValueOrValues(value);
   }

   /**
    * Equivalent to calling {@link #returns(Object)} two or more times in sequence, except when the
    * associated method can return a {@code List} of values.
    * Specifically, if said method has a return type which is an ordered collection type that can
    * receive a {@link List} value, then the given sequence of values will be converted into an
    * {@code ArrayList}; this list will then be returned by matching invocations at replay time.
    * <p/>
    * The current expectation will have its upper invocation count automatically set to the total
    * number of values specified to be returned. This upper limit can be overridden through the
    * {@code maxTimes} field, if necessary.
    *
    * @param firstValue the first value to be returned in the replay phase
    * @param remainingValues the remaining values to be returned, in the same order
    *
    * @throws IllegalStateException if not currently recording an invocation
    * @throws IllegalArgumentException if one of the given return values is not {@code null} but the
    * preceding mock invocation is to a constructor or {@code void} method
    */
   protected final void returns(Object firstValue, Object... remainingValues)
   {
      getCurrentExpectation().addSequenceOfReturnValues(firstValue, remainingValues);
   }

   /**
    * Specifies that the preceding method/constructor invocation will throw an exception when
    * executed in the replay phase.
    * <p/>
    * Just like with {@link #returns(Object)}, multiple consecutive exceptions to be thrown can be
    * specified by calling this method multiple times for the same invocation.
    *
    * @param exception the exception that will be thrown when the invocation is replayed
    *
    * @throws IllegalStateException if not currently recording an invocation
    *
    * @deprecated Use {@link #result} instead.
    */
   @Deprecated
   protected final void throwsException(Exception exception)
   {
      getCurrentExpectation().getResults().addThrowable(exception);
   }

   /**
    * Specifies that the preceding method/constructor invocation will throw an error when replayed.
    * <p/>
    * Just like with {@link #returns(Object)}, multiple consecutive errors to be thrown can be
    * specified by calling this method multiple times for the same invocation.
    *
    * @param error the error that will be thrown when the invocation is replayed
    *
    * @throws IllegalStateException if not currently recording an invocation
    *
    * @deprecated Use {@link #result} instead.
    */
   @Deprecated
   protected final void throwsError(Error error)
   {
      getCurrentExpectation().getResults().addThrowable(error);
   }

   // Methods for defining expectation strictness /////////////////////////////////////////////////

   /**
    * Marks the preceding mock invocation as belonging to a <em>non-strict</em> expectation.
    * Note that all invocations to {@link NonStrict} mocks will be automatically considered
    * non-strict. The same is true for all invocations inside a {@link NonStrictExpectations} block.
    * <p/>
    * For a non-strict expectation, any number (including zero) of invocations with matching
    * arguments can occur while in the replay phase, in any order, and they will all produce the
    * same result (usually, the {@linkplain #returns(Object) specified return value}).
    * Two or more non-strict expectations can be recorded for the same method or constructor, as
    * long as the arguments differ. Argument matchers can be used as well.
    * <p/>
    * Expected invocation counts can also be specified for a non-strict expectation (with one of
    * the "times" fields).
    */
   protected final void notStrict()
   {
      getCurrentPhase().setNotStrict();
   }

   // Other methods ///////////////////////////////////////////////////////////////////////////////

   /**
    * Ends the recording of expected invocations.
    * After this, the mock fields will be in the <strong>replay</strong> phase, when invocations are
    * verified against previously recorded invocations.
    * <p/>
    * <strong>Warning</strong>:
    * This method is called automatically as soon as the initialization of an expectation block is
    * complete, so usually tests should not explicitly call it. There may be some rare situations,
    * however, where ending the recording phase while still inside an expectation block will be
    * convenient.
    */
   public final void endRecording()
   {
      execution.endRecording();
   }
}
