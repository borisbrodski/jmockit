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
package mockit;

import java.util.regex.*;

import mockit.external.hamcrest.*;
import mockit.external.hamcrest.Matcher;
import mockit.external.hamcrest.core.*;
import mockit.external.hamcrest.number.*;
import mockit.internal.expectations.*;
import mockit.internal.util.*;

/**
 * Provides common user API for both the {@linkplain Expectations record} and
 * {@linkplain Verifications verification} phases of a test.
 */
abstract class Invocations
{
   /**
    * Matches any {@code Object} reference for the relevant parameter.
    * Note that the use of this field will usually require a cast to the specific parameter type.
    * If there is any other parameter for which an argument matching constraint can be specified,
    * though, the {@code null} reference can be passed instead, as it will also match any
    * reference during the replay phase.
    *
    * @see #anyInt
    */
   protected static final Object any = null;

   /**
    * Matches any {@code String} value for the relevant parameter.
    *
    * @see #anyInt
    */
   protected static final String anyString = null;

   /**
    * Matches any {@code long} or {@code Long} value for the relevant parameter.
    *
    * @see #anyInt
    */
   protected static final Long anyLong = 0L;

   /**
    * Matches any {@code int} or {@code Integer} value for the relevant parameter.
    * <p/>
    * When used as argument for a method/constructor invocation in the recording or verification
    * phase of a test, specifies the matching of <em>any</em> value passed as argument to
    * corresponding invocations in the replay phase.
    */
   protected static final Integer anyInt = 0;

   /**
    * Matches any {@code short} or {@code Short} value for the relevant parameter.
    *
    * @see #anyInt
    */
   protected static final Short anyShort = 0;

   /**
    * Matches any {@code byte} or {@code Byte} value for the relevant parameter.
    *
    * @see #anyInt
    */
   protected static final Byte anyByte = 0;

   /**
    * Matches any {@code boolean} or {@code Boolean} value for the relevant parameter.
    *
    * @see #anyInt
    */
   protected static final Boolean anyBoolean = false;

   /**
    * Matches any {@code char} or {@code Character} value for the relevant parameter.
    *
    * @see #anyInt
    */
   protected static final Character anyChar = '\0';

   /**
    * Matches any {@code double} or {@code Double} value for the relevant parameter.
    *
    * @see #anyInt
    */
   protected static final Double anyDouble = 0.0;

   /**
    * Matches any {@code float} or {@code Float} value for the relevant parameter.
    *
    * @see #anyInt
    */
   protected static final Float anyFloat = 0.0F;

   /**
    * A non-negative value assigned to this field will be taken as the exact number of times that
    * invocations matching the current expectation should occur during replay.
    * Each assignment to this field is equivalent to calling {@link #repeats(int)} with the assigned
    * value.
    *
    * @see #minTimes
    * @see #maxTimes
    */
   protected static int times;

   /**
    * A non-negative value assigned to this field will be taken as the minimum number of times that
    * invocations matching the current expectation should occur during replay.
    * Each assignment to this field is equivalent to calling {@link #repeatsAtLeast(int)} with the
    * assigned value.
    *
    * @see #times
    * @see #maxTimes
    */
   protected static int minTimes;

   /**
    * A non-negative value assigned to this field will be taken as the maximum number of times that
    * invocations matching the current expectation should occur during replay.
    * A <em>negative</em> value implies there is no upper limit.
    * Each assignment to this field is equivalent to calling {@link #repeatsAtMost(int)} with the
    * assigned value.
    *
    * @see #times
    * @see #minTimes
    */
   protected static int maxTimes;

   protected Invocations() {}

   abstract TestOnlyPhase getCurrentPhase();

   final Expectation getCurrentExpectation()
   {
      return getCurrentPhase().getCurrentExpectation();
   }

   // Methods for argument matching ///////////////////////////////////////////////////////////////

   /**
    * Adds a custom argument matcher for a parameter in the current invocation.
    * The given matcher can be any existing Hamcrest matcher or a user provided one.
    * <p/>
    * For additional details, refer to {@link #withEqual(Object)}.
    *
    * @param argValue an arbitrary value of the proper type, necessary to provide a valid argument
    * to the invocation parameter
    * @param argumentMatcher an instance of a class implementing the {@code org.hamcrest.Matcher}
    * interface
    *
    * @return the given {@code argValue}
    */
   protected final <T> T with(T argValue, Object argumentMatcher)
   {
      addMatcher(new HamcrestAdapter<T>(argumentMatcher));
      return argValue;
   }

   /**
    * Adds a custom argument matcher for a parameter in the current invocation.
    * This works like {@link #with(Object, Object)}, but attempting to extract the argument value
    * from the supplied argument matcher.
    *
    * @param argumentMatcher an instance of a class implementing the {@code org.hamcrest.Matcher}
    * interface
    *
    * @return the value recorded inside the given argument matcher, or {@code null} if no such value
    * could be determined
    */
   protected final <T> T with(Object argumentMatcher)
   {
      HamcrestAdapter<T> adapter = new HamcrestAdapter<T>(argumentMatcher);
      addMatcher(adapter);

      Object argValue = adapter.getInnerValue();
      
      //noinspection unchecked
      return (T) argValue;
   }

   private void addMatcher(Matcher<?> matcher)
   {
      getCurrentPhase().addArgMatcher(matcher);
   }

   /**
    * Same as {@link #withAny(Object)}, but always returning {@code null}.
    */
   protected final <T> T withAny()
   {
      return withAny((T) null);
   }

   /**
    * Same as {@link #withEqual(Object)}, but matching any argument value of the appropriate type.
    * <p/>
    * Consider using instead the "any" field appropriate to the parameter type: {@link #any},
    * {@link #anyBoolean}, {@link #anyByte}, {@link #anyChar}, {@link #anyDouble},
    * {@link #anyFloat}, {@link #anyInt}, {@link #anyLong}, {@link #anyShort}, {@link #anyString}.
    *
    * @param arg an arbitrary value which will match any argument value in the replay phase
    *
    * @return the input argument
    */
   protected final <T> T withAny(T arg)
   {
      addMatcher(new IsAnything());
      return arg;
   }

   /**
    * When called as argument for a method/constructor invocation in the recording or verification
    * phase of a test, creates a new matcher that will check if the given value is
    * {@link Object#equals(Object) equal} to the corresponding invocation argument in the replay
    * phase.
    * <p/>
    * The matcher is added to the end of the list of argument matchers for the invocation being
    * recorded/verified. It cannot be reused for a different parameter.
    * <p/>
    * For methods with a <strong>varargs</strong> parameter, a corresponding matcher must be
    * specified for each element in the varargs array passed in the replay phase.
    *
    * @param arg the expected argument value
    *
    * @return the input argument
    */
   protected final <T> T withEqual(T arg)
   {
      //noinspection unchecked
      addMatcher(new IsEqual(arg));
      return arg;
   }

   /**
    * Same as {@link #withEqual(Object)}, but checking that a numeric invocation argument in the
    * replay phase is sufficiently close to the given value.
    */
   protected final double withEqual(double value, double delta)
   {
      addMatcher(new IsCloseTo(value, delta));
      return value;
   }

   /**
    * Same as {@link #withEqual(Object)}, but checking that a numeric invocation argument in the
    * replay phase is sufficiently close to the given value.
    */
   protected final float withEqual(float value, double delta)
   {
      addMatcher(new IsCloseTo(value, delta));
      return value;
   }

   /**
    * Same as {@link #withEqual(Object)}, but checking that an invocation argument in the replay
    * phase is an instance of the same class as the given object.
    * <p/>
    * Equivalent to a call <code>withInstanceOf(object.getClass())</code>, except that it returns
    * {@code object} instead of {@code null}.
    */
   protected final <T> T withInstanceLike(T object)
   {
      addMatcher(new IsInstanceOf(object.getClass()));
      return object;
   }

   /**
    * Same as {@link #withEqual(Object)}, but checking that an invocation argument in the replay
    * phase is an instance of the given class.
    *
    * @return always null; if you need a specific return value, use
    * {@link #withInstanceLike(Object)}
    */
   protected final <T> T withInstanceOf(Class<T> argClass)
   {
      addMatcher(new IsInstanceOf(argClass));
      return null;
   }

   /**
    * Same as {@link #withEqual(Object)}, but checking that the invocation argument in the replay
    * phase is different from the given value.
    */
   protected final <T> T withNotEqual(T arg)
   {
      //noinspection unchecked
      addMatcher(new IsNot<T>(new IsEqual(arg)));
      return arg;
   }

   /**
    * Same as {@link #withEqual(Object)}, but checking that an invocation argument in the replay
    * phase is not {@code null}.
    *
    * @return always {@code null}
    */
   protected final <T> T withNotNull()
   {
      addMatcher(new IsNot<T>(new IsNull<T>()));
      return null;
   }

   /**
    * Same as {@link #withEqual(Object)}, but checking that an invocation argument in the replay
    * phase is {@code null}.
    *
    * @return always {@code null}
    */
   protected final <T> T withNull()
   {
      addMatcher(new IsNull<T>());
      return null;
   }

   /**
    * Same as {@link #withEqual(Object)}, but checking that an invocation argument in the replay
    * phase is the exact same instance as the one in the recorded/verified invocation.
    */
   protected final <T> T withSameInstance(T object)
   {
      //noinspection unchecked
      addMatcher(new IsSame(object));
      return object;
   }

   // Text-related matchers ///////////////////////////////////////////////////////////////////////

   /**
    * Same as {@link #withEqual(Object)}, but checking that a textual invocation argument in the
    * replay phase contains the given text as a substring.
    */
   protected final <T extends CharSequence> T withSubstring(T text)
   {
      addMatcher(new StringContains(text));
      return text;
   }

   /**
    * Same as {@link #withEqual(Object)}, but checking that a textual invocation argument in the
    * replay phase starts with the given text.
    */
   protected final <T extends CharSequence> T withPrefix(T text)
   {
      addMatcher(new StringStartsWith(text));
      return text;
   }

   /**
    * Same as {@link #withEqual(Object)}, but checking that a textual invocation argument in the
    * replay phase ends with the given text.
    */
   protected final <T extends CharSequence> T withSuffix(T text)
   {
      addMatcher(new StringEndsWith(text));
      return text;
   }

   /**
    * Same as {@link #withEqual(Object)}, but checking that a textual invocation argument in the
    * replay phase matches the given {@link Pattern regular expression}.
    * <p/>
    * Note that this can be used for any string comparison, including case insensitive ones (with
    * {@code "(?i)"} in the regex).
    *
    * @see Pattern#compile(String, int)
    */
   protected final <T extends CharSequence> T withMatch(T regex)
   {
      final Pattern pattern = Pattern.compile(regex.toString());

      addMatcher(new BaseMatcher<T>()
      {
         public boolean matches(Object item)
         {
            return pattern.matcher((CharSequence) item).matches();
         }

         public void describeTo(Description description)
         {
            description.appendText("a string matching ").appendValue(pattern);
         }
      });

      return regex;
   }

   // Methods for setting expectation constraints /////////////////////////////////////////////////

   /**
    * Specifies the exact number of invocations for the current expectation.
    * <p/>
    * As an alternative, consider using the {@link #times} field.
    *
    * @param exactInvocationCount exact number of times the invocation is expected to occur during
    * the replay phase
    *
    * @see #repeats(int, int)
    * @see #repeatsAtLeast(int)
    * @see #repeatsAtMost(int)
    */
   protected final void repeats(int exactInvocationCount)
   {
      getCurrentPhase().handleInvocationCountConstraint(exactInvocationCount, exactInvocationCount);
   }

   /**
    * Specifies a range for the number of invocations of the current expectation.
    * <p/>
    * As an alternative, consider using the {@link #minTimes} and {@link #maxTimes} fields.
    *
    * @param minInvocations minimum number of times the invocation is expected to occur during the
    * replay phase
    * @param maxInvocations maximum number of times the invocation is allowed to occur during the
    * replay phase, or {@literal -1} for no upper limit
    *
    * @see #repeats(int)
    * @see #repeatsAtLeast(int)
    * @see #repeatsAtMost(int)
    */
   protected final void repeats(int minInvocations, int maxInvocations)
   {
      getCurrentPhase().handleInvocationCountConstraint(minInvocations, maxInvocations);
   }

   /**
    * Specifies a lower limit for the number of invocations of the current expectation.
    * <p/>
    * The upper limit is automatically adjusted so that any number of invocations beyond the
    * specified minimum is allowed. The upper limit can still be specified for the same expectation,
    * but consider instead using {@link #repeats(int, int)} to specify both limits at the same time.
    * <p/>
    * As an alternative, consider using the {@link #minTimes} field.
    *
    * @param minInvocations minimum number of times the invocation is expected to occur during the
    * replay phase
    *
    * @see #repeats(int)
    * @see #repeats(int, int)
    * @see #repeatsAtMost(int)
    */
   protected final void repeatsAtLeast(int minInvocations)
   {
      getCurrentPhase().handleInvocationCountConstraint(minInvocations, -1);
   }

   /**
    * Specifies an upper limit for the number of invocations of the current expectation.
    * <p/>
    * The lower limit is automatically adjusted, if needed, to be no more than the specified
    * maximum.
    * <p/>
    * As an alternative, consider using the {@link #maxTimes} field.
    *
    * @param maxInvocations maximum number of times the invocation is allowed to occur during the
    * replay phase, or {@literal -1} for no upper limit
    *
    * @see #repeats(int)
    * @see #repeats(int, int)
    * @see #repeatsAtLeast(int)
    */
   protected final void repeatsAtMost(int maxInvocations)
   {
      getCurrentPhase().setMaxInvocationCount(maxInvocations);
   }

   // Methods for instantiating non-accessible classes ////////////////////////////////////////////

   /**
    * Specifies an expected constructor invocation for a given class.
    * <p/>
    * This is useful for invoking non-accessible constructors (private ones, for example) from the
    * test, which otherwise could not be called normally.
    *
    * @param className the fully qualified name of the desired class (which should not be accessible
    * to the test; otherwise just refer to it in code)
    * @param parameterTypes the formal parameter types for the desired constructor, possibly empty
    * @param initArgs the invocation arguments for the constructor, which must be consistent with
    * the specified parameter types
    * @param <T> interface or super-class type to which the returned instance should be assignable
    *
    * @return a newly created instance of the specified class, initialized with the specified
    * constructor and arguments
    *
    * @see #newInstance(String, Object...)
    * @see #newInnerInstance(String, Object, Object...)
    */
   protected final <T> T newInstance(
      String className, Class<?>[] parameterTypes, Object... initArgs)
   {
      //noinspection unchecked
      return (T) Utilities.newInstance(className, parameterTypes, initArgs);
   }

   /**
    * The same as {@link #newInstance(String, Class[], Object...)}, but for the case where each
    * initialization argument is known to be null or non-null at the call point.
    * <p/>
    * However, {@code null} argument values cannot directly be passed to this method;
    * if the parameter value must be {@code null}, then the corresponding {@code Class} literal must
    * be passed instead.
    *
    * @throws IllegalArgumentException if one of the given arguments is {@code null}
    */
   protected final <T> T newInstance(String className, Object... nonNullInitArgs)
   {
      //noinspection unchecked
      return (T) Utilities.newInstance(className, nonNullInitArgs);
   }

   /**
    * The same as {@link #newInstance(String, Class[], Object...)}, but for instantiating an inner
    * non-accessible class of some other class, and where all other (if any) initialization
    * arguments are known to be non null.
    *
    * @param innerClassSimpleName simple name of the inner class, that is, the part after the "$"
    * character in its full name
    * @param outerClassInstance the outer class instance to which the inner class instance will
    * belong
    */
   protected final <T> T newInnerInstance(
      String innerClassSimpleName, Object outerClassInstance, Object... nonNullInitArgs)
   {
      //noinspection unchecked
      return
         (T) Utilities.newInnerInstance(innerClassSimpleName, outerClassInstance, nonNullInitArgs);
   }

   // Methods for invoking non-accessible methods on mock instances/classes ///////////////////////

   /**
    * Specifies an expected invocation to a given instance method, with a given list of arguments.
    * <p/>
    * This is useful when the next expected method is not accessible (private, for example) from the
    * test, and therefore can not be called normally. It should <strong>not</strong> be used for
    * calling accessible methods.
    * <p/>
    * Additionally, this can also be used to directly test private methods, when there is no other
    * way to do so, or it would be too difficult by indirect means. Note that in such a case the
    * target instance will actually be a "real" object, not a mock.
    *
    * @param mock the mock field instance on which the invocation is to be done; must not be null
    * @param methodName the name of the expected method
    * @param methodArgs zero or more non-null expected parameter values for the invocation; if a
    * null value needs to be passed, the Class object for the parameter type must be passed instead
    *
    * @return the return value from the invoked method, wrapped if primitive
    *
    * @see #invoke(Class, String, Object...)
    */
   protected final <T> T invoke(Object mock, String methodName, Object... methodArgs)
   {
      //noinspection unchecked
      return (T) Utilities.invoke(mock.getClass(), mock, methodName, methodArgs);
   }

   /**
    * Specifies an expected invocation to a given static method, with a given list of arguments.
    * <p/>
    * This is useful when the next expected method is not accessible (private, for example) from the
    * test, and therefore can not be called normally. It should <strong>not</strong> be used for
    * calling accessible methods.
    * <p/>
    * Additionally, this can also be used to directly test private methods, when there is no other
    * way to do so, or it would be too difficult by indirect means. Note that in such a case the
    * target class will normally be a "real", non-mocked, class in the code under test.
    *
    * @param mockClass the class on which the invocation is to be done; must not be null
    * @param methodName the name of the expected static method
    * @param methodArgs zero or more non-null expected parameter values for the invocation; if a
    * null value needs to be passed, the Class object for the parameter type must be passed instead
    */
   protected final <T> T invoke(Class<?> mockClass, String methodName, Object... methodArgs)
   {
      //noinspection unchecked
      return (T) Utilities.invoke(mockClass, null, methodName, methodArgs);
   }

   // Methods for getting/setting non-accessible fields on mock instances/classes /////////////////

   /**
    * Gets the value of a field from a given object (usually a mock).
    * <p/>
    * This may be useful when a mock object has a field not accessible from the test (private, for
    * example), and there is some method under test which writes to the field.
    *
    * @param mock the instance from which to get the field value
    * @param fieldName the name of the field to get
    *
    * @see #setField(Object, String, Object)
    */
   protected final <T> T getField(Object mock, String fieldName)
   {
      //noinspection unchecked
      return (T) Utilities.getField(mock.getClass(), fieldName, mock);
   }

   /**
    * Gets the value of a field from a given object (usually a mock),
    * <em>assuming</em> there is only one field declared in the class of the given object whose type
    * can receive values of the specified field type.
    * <p/>
    * This may be useful when a mock object has a field not accessible from the test (private, for
    * example), and there is some method under test which writes to the field.
    *
    * @param objectWithField the instance from which to get the field value
    * @param fieldType the declared type of the field, or a sub-type of the declared field type
    *
    * @see #getField(Object, String)
    *
    * @throws IllegalArgumentException if either the desired field is not found, or more than one is
    */
   protected final <T> T getField(Object objectWithField, Class<T> fieldType)
   {
      //noinspection unchecked
      return Utilities.getField(objectWithField.getClass(), fieldType, objectWithField);
   }

   /**
    * Gets the value of a static field defined in a given class.
    * <p/>
    * This may be useful when a class under test has a field not accessible from the test (private,
    * for example), and there is some method under test which writes to the field.
    *
    * @param realClass the class from which to get the field value
    * @param fieldName the name of the static field to get
    *
    * @see #setField(Class, String, Object)
    */
   protected final <T> T getField(Class<?> realClass, String fieldName)
   {
      //noinspection unchecked
      return (T) Utilities.getField(realClass, fieldName, null);
   }

   /**
    * Gets the value of a static field defined in a given class.
    * <p/>
    * This may be useful when a class under test has a field not accessible from the test (private,
    * for example), and there is some method under test which writes to the field.
    *
    * @param realClass the class from which to get the field value
    * @param fieldType the declared type of the field, or a sub-type of the declared field type
    *
    * @see #setField(Class, String, Object)
    */
   protected final <T> T getField(Class<?> realClass, Class<T> fieldType)
   {
      //noinspection unchecked
      return Utilities.getField(realClass, fieldType, null);
   }

   /**
    * Sets the value of a field on a given object (usually a mock).
    * <p/>
    * This may be useful when a mock object has a field not accessible from the test (private, for
    * example), and there is some method under test which needs to read the correct field value.
    *
    * @param mock the instance on which to set the field value
    * @param fieldName the name of the field to set
    * @param fieldValue the value to set the field to
    *
    * @see #setField(Class, String, Object)
    */
   protected final void setField(Object mock, String fieldName, Object fieldValue)
   {
      Utilities.setField(mock.getClass(), mock, fieldName, fieldValue);
   }

   /**
    * Same as {@link #setField(Object, String, Object)}, except that the field is looked up by the
    * type of the given field value instead of by name.
    *
    * @throws IllegalArgumentException if no field or more than one is found in the target class to
    * which the given value can be assigned
    */
   protected final void setField(Object mock, Object fieldValue)
   {
      Utilities.setField(mock.getClass(), mock, null, fieldValue);
   }

   /**
    * Sets the value of a static field on a given class.
    * <p/>
    * This may be useful when a mock object has a field not accessible from the test (private, for
    * example), and there is some method under test which needs to read the correct field value.
    *
    * @param mockClass  the class on which the static field is defined
    * @param fieldName  the name of the field to set
    * @param fieldValue the value to set the field to
    */
   protected final void setField(Class<?> mockClass, String fieldName, Object fieldValue)
   {
      Utilities.setField(mockClass, null, fieldName, fieldValue);
   }

   /**
    * Same as {@link #setField(Class, String, Object)}, except that the field is looked up by the
    * type of the given field value instead of by name.
    *
    * @param mockClass  the class on which the static field is defined
    * @param fieldValue the value to set the field to
    */
   protected final void setField(Class<?> mockClass, Object fieldValue)
   {
      Utilities.setField(mockClass, null, null, fieldValue);
   }
}
