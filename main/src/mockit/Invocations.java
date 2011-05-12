/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import java.util.regex.*;

import mockit.external.hamcrest.*;
import mockit.external.hamcrest.Matcher;
import mockit.external.hamcrest.core.*;
import mockit.external.hamcrest.number.*;
import mockit.internal.expectations.*;
import mockit.internal.startup.*;
import mockit.internal.util.*;

/**
 * Provides common user API for both the {@linkplain Expectations record} and {@linkplain Verifications verification}
 * phases of a test.
 */
abstract class Invocations
{
   static
   {
      Startup.verifyInitialization();
   }

   /**
    * Matches any {@code Object} reference for the relevant parameter.
    * Note that the use of this field will usually require a cast to the specific parameter type.
    * If there is any other parameter for which an argument matching constraint can be specified,
    * though, the {@code null} reference can be passed instead, as it will also match any
    * reference during the replay phase.
    * <p/>
    * Note: in invocations to <em>non-accessible</em> methods or constructors (for example, with
    * {@link #invoke(Object, String, Object...)}), use {@link #withAny} instead.
    *
    * @see #anyInt
    */
   protected static final Object any = null;

   /**
    * Matches any {@code String} value for the relevant parameter.
    *
    * @see #anyInt
    */
   // This is intentional: the empty string causes the compiler to not generate a field read,
   // while the null reference is inconvenient with the invoke(...) methods:
   protected static final String anyString = new String();

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
    * <p/>
    * <a href="http://jmockit.googlecode.com/svn/trunk/www/tutorial/BehaviorBasedTesting.html#hamcrest">In the Tutorial</a>
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
    * An object assigned to this field will be taken as a handler for each invocation matching the current expectation,
    * with the purpose of validating invocation arguments.
    * Note that for a <em>recorded</em> expectation such invocations are the ones that will be executed during the
    * <em>replay</em> phase, while for a <em>verified</em> expectation they are the ones actually executed during that
    * phase.
    * <p/>
    * The object assigned can be of any type, provided its class has a single non-private method
    * (therefore, additional methods are allowed and ignored, as long as they are {@code private}).
    * This <em>handler method</em> can have any name, as long as its parameters match the ones defined in the mocked
    * method or constructor associated with the expectation.
    * Corresponding parameters don't need to have the exact same declared type, though, as long as each possible
    * invocation argument can be passed to the corresponding parameter in the handler method.
    * <p/>
    * In the case of an expectation recorded for a non-{@code void} method, the handler method is also responsible for
    * returning appropriate values to be used by the caller (which normally belongs to the code under test).
    * That is, the {@code result} field or the {@code returns(...)} method should <em>not</em> be used together with an
    * assignment to this field.
    * The same observation applies to the throwing of exceptions/errors from a recorded expectation
    * (which can also be done for constructors and {@code void} methods).
    * <p/>
    * When used for an expectation inside a <em>verification</em> block, on the other hand, the handler method should
    * normally have a {@code void} return type. Any value eventually returned by the method will be silently ignored in
    * this case. Note that a handler method for a verified expectation also shouldn't intentionally throw exceptions or
    * errors, since the verified invocation(s) already happened in the replay phase; any exception/error actually thrown
    * will simply propagate back to the test method.
    * <p/>
    * Just like with {@linkplain mockit.Delegate delegate classes}, the handler method can declare its first parameter
    * as being of type {@link mockit.Invocation}.
    * <p/>
    * <a href="http://jmockit.googlecode.com/svn/trunk/www/tutorial/BehaviorBasedTesting.html#forEachInvocation">In the Tutorial</a>
    */
   protected static Object forEachInvocation;

   /**
    * A non-negative value assigned to this field will be taken as the exact number of times that
    * invocations matching the current expectation should occur during replay.
    * <p/>
    * <a href="http://jmockit.googlecode.com/svn/trunk/www/tutorial/BehaviorBasedTesting.html#constraints">In the Tutorial</a>
    *
    * @see #minTimes
    * @see #maxTimes
    */
   protected static int times;

   /**
    * A non-negative value assigned to this field will be taken as the minimum number of times that invocations matching
    * the current expectation should occur during replay.
    * <em>Zero</em> or a <em>negative</em> value implies there is no lower limit.
    * The <em>maximum</em> number of times is automatically adjusted to allow any number of invocations.
    * <p/>
    * Both {@code minTimes} and {@code maxTimes} can be specified for the same expectation, as long as {@code minTimes}
    * is assigned first.
    *
    * @see #times
    * @see #maxTimes
    */
   protected static int minTimes;

   /**
    * A non-negative value assigned to this field will be taken as the maximum number of times that invocations matching
    * the current expectation should occur during replay.
    * A <em>negative</em> value implies there is no upper limit.
    * <p/>
    * Both {@code minTimes} and {@code maxTimes} can be specified for the same expectation, as long as {@code minTimes}
    * is assigned first.
    *
    * @see #times
    * @see #minTimes
    */
   protected static int maxTimes;

   /**
    * A value assigned to this field will be used as a prefix for the error message to be reported
    * if and when the current expectation is violated.
    * <p/>
    * Inside an <em>expectation</em>/<em>verification</em> block, the assignment must follow the
    * invocation which records/verifies the expectation; if there is no current expectation at the
    * point the assignment appears, an {@code IllegalStateException} is thrown.
    * <p/>
    * Notice there are only two different ways in which an expectation can be violated: either an
    * <em>unexpected</em> invocation occurs, or a <em>missing</em> invocation is detected.
    */
   protected static CharSequence $;

   Invocations() {}

   abstract TestOnlyPhase getCurrentPhase();

   /**
    * Specify that the next invocation on the given mocked instance must match a corresponding invocation on the
    * <em>same</em> instance in the replay phase.
    * <p/>
    * By default, such instances can be different between the replay phase and the record or verify phase, even though
    * the method or constructor invoked is the same, and the invocation arguments all match.
    * The use of this method allows invocations to also be matched on the instance invoked.
    * <p/>
    * Typically, tests that need to match instance invocations on the mocked instances invoked will declare two or more
    * mock fields and/or parameters of the exact same mocked type. These instances will then be passed to the code under
    * test, which will invoke them during the replay phase.
    * To avoid the need to explicitly call {@code onInstance(Object)} on each of these different instances of the
    * same type, instance matching is <em>implied</em> (and automatically applied to all relevant invocations) whenever
    * two or more mocked instances of the same type are in scope for a given test method. This property of the API makes
    * the use of {@code onInstance} much less frequent than it might otherwise be.
    * <p/>
    * In most cases, an invocation to the given mocked instance will be made on the value returned by this method (ie,
    * a chained invocation).
    * However, in the situation where the tested method calls an instance method defined in a mocked super-class
    * (possibly an overridden method called through the {@code super} keyword), it will be necessary to match on a
    * different instance than the one used for recording invocations.
    * To do so, this method should be given the desired instance to match, while the invocation to be recorded should be
    * done on the available mocked instance, which must be a different one (otherwise a non-mocked method would get
    * executed).
    * This is valid only if the instance to be matched is assignable to the mocked type, and typically occurs when
    * partially mocking a class hierarchy.
    * <p/>
    * <a href="http://jmockit.googlecode.com/svn/trunk/www/tutorial/BehaviorBasedTesting.html#onInstance">In the Tutorial</a>
    *
    * @return the given mocked instance, allowing the invocation to be recorded/verified to immediately follow the call
    * to this method
    */
   protected final <T> T onInstance(T mockedInstance)
   {
      if (mockedInstance == null) {
         throw new NullPointerException("Missing mocked instance to match");
      }

      getCurrentPhase().setNextInstanceToMatch(mockedInstance);
      return mockedInstance;
   }

   // Methods for argument matching ///////////////////////////////////////////////////////////////////////////////////

   /**
    * Adds a custom argument matcher for a parameter in the current invocation.
    * <p/>
    * The given matcher can be any existing <strong>Hamcrest</strong> matcher or a user provided
    * one.
    * Additionally, it can be an instance of an arbitrary <em>invocation handler</em> class, similar
    * to those used with the {@link #forEachInvocation} field.
    * In this case, the non-{@code private} <em>handler method</em> must have a single parameter of
    * a type capable of receiving the relevant argument values.
    * The name of this handler method does not matter. Its return type, on the other hand, should
    * either be {@code boolean} or {@code void}. In the first case, a return value of
    * {@code true} will indicate a successful match for the actual invocation argument at replay
    * time, while a return of {@code false} will cause the test to fail. In the second case, instead
    * of returning a value the invocation handler method should validate the actual invocation 
    * argument through an {@code assert} statement or a JUnit/TestNG assertion.
    * <p/>
    * For additional details, refer to {@link #withEqual(Object)}.
    *
    * @param argValue an arbitrary value of the proper type, necessary to provide a valid argument
    * to the invocation parameter
    * @param argumentMatcher an instance of a class implementing the {@code org.hamcrest.Matcher}
    * interface, or any other instance with an appropriate invocation handler method
    *
    * @return the given {@code argValue}
    */
   protected final <T> T with(T argValue, Object argumentMatcher)
   {
      addMatcher(HamcrestAdapter.create(argumentMatcher));
      return argValue;
   }

   /**
    * Adds a custom argument matcher for a parameter in the current invocation.
    * This works like {@link #with(Object, Object)}, but attempting to extract the argument value
    * from the supplied argument matcher.
    *
    * @param argumentMatcher an instance of a class implementing the {@code org.hamcrest.Matcher}
    * interface, or any other instance with an appropriate invocation handler method
    *
    * @return the value recorded inside the given argument matcher, or {@code null} if no such value
    * could be determined
    */
   protected final <T> T with(Object argumentMatcher)
   {
      HamcrestAdapter<T> adapter = HamcrestAdapter.create(argumentMatcher);
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
    * Same as {@link #withEqual(Object)}, but matching any argument value of the appropriate type.
    * <p/>
    * Consider using instead the "anyXyz" field appropriate to the parameter type:
    * {@link #anyBoolean}, {@link #anyByte}, {@link #anyChar}, {@link #anyDouble},
    * {@link #anyFloat}, {@link #anyInt}, {@link #anyLong}, {@link #anyShort}, {@link #anyString},
    * or {@link #any} for other reference types.
    * <p/>
    * Note: when using {@link #invoke(Object, String, Object...)}, etc., it's valid to pass
    * {@code withAny(ParameterType.class)} if an actual instance of the parameter type cannot be
    * created.
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
    * <a href="http://jmockit.googlecode.com/svn/trunk/www/tutorial/BehaviorBasedTesting.html#hamcrest">In the Tutorial</a>
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
    * Equivalent to a call <code>withInstanceOf(arg.getClass())</code>, except that it returns
    * {@code arg} instead of {@code null}.
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

   // Text-related matchers ///////////////////////////////////////////////////////////////////////////////////////////

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

   // Methods for instantiating non-accessible classes ////////////////////////////////////////////////////////////////

   /**
    * Specifies an expected constructor invocation for a given class.
    * <p/>
    * This is useful for invoking non-accessible constructors (private ones, for example) from the
    * test, which otherwise could not be called normally.
    * <p/>
    * <a href="http://jmockit.googlecode.com/svn/trunk/www/tutorial/BehaviorBasedTesting.html#deencapsulation">In the Tutorial</a>
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
   protected final <T> T newInstance(String className, Class<?>[] parameterTypes, Object... initArgs)
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
      return (T) Utilities.newInnerInstance(innerClassSimpleName, outerClassInstance, nonNullInitArgs);
   }

   // Methods for invoking non-accessible methods on instances or classes /////////////////////////////////////////////

   /**
    * Specifies an expected invocation to a given instance method, with a given list of arguments.
    * <p/>
    * This is useful when the next expected method is not accessible (private, for example) from the test, and
    * therefore can not be called normally. It should <strong>not</strong> be used for calling accessible methods.
    * <p/>
    * Additionally, this can also be used to directly test private methods, when there is no other way to do so, or it
    * would be too difficult by indirect means. Note that in such a case the target instance will actually be a "real"
    * (non-mocked) object, not a mocked instance.
    * <p/>
    * <a href="http://jmockit.googlecode.com/svn/trunk/www/tutorial/BehaviorBasedTesting.html#deencapsulation">In the Tutorial</a>
    *
    * @param objectWithMethod the instance on which the invocation is to be done; must not be null
    * @param methodName the name of the expected method
    * @param methodArgs zero or more non-null expected parameter values for the invocation; if a
    * null value needs to be passed, the Class object for the parameter type must be passed instead
    *
    * @return the return value from the invoked method, wrapped if primitive
    *
    * @see #invoke(Class, String, Object...)
    */
   protected final <T> T invoke(Object objectWithMethod, String methodName, Object... methodArgs)
   {
      //noinspection unchecked
      return (T) Utilities.invoke(objectWithMethod.getClass(), objectWithMethod, methodName, methodArgs);
   }

   /**
    * Specifies an expected invocation to a given static method, with a given list of arguments.
    * <p/>
    * This is useful when the next expected method is not accessible (private, for example) from the
    * test, and therefore cannot be called normally. It should <strong>not</strong> be used for
    * calling accessible methods.
    * <p/>
    * Additionally, this can also be used to directly test private methods, when there is no other
    * way to do so, or it would be too difficult by indirect means. Note that in such a case the
    * target class will normally be a "real", non-mocked, class in the code under test.
    *
    * @param methodOwner the class on which the invocation is to be done; must not be null
    * @param methodName the name of the expected static method
    * @param methodArgs zero or more non-null expected parameter values for the invocation; if a
    * null value needs to be passed, the Class object for the parameter type must be passed instead
    */
   protected final <T> T invoke(Class<?> methodOwner, String methodName, Object... methodArgs)
   {
      //noinspection unchecked
      return (T) Utilities.invoke(methodOwner, null, methodName, methodArgs);
   }

   // Methods for getting/setting non-accessible fields on instances or classes ///////////////////////////////////////

   /**
    * Gets the value of a non-accessible field from a given object.
    * <p/>
    * <a href="http://jmockit.googlecode.com/svn/trunk/www/tutorial/BehaviorBasedTesting.html#deencapsulation">In the Tutorial</a>
    *
    * @param fieldOwner the instance from which to get the field value
    * @param fieldName the name of the field to get
    *
    * @see #setField(Object, String, Object)
    */
   protected final <T> T getField(Object fieldOwner, String fieldName)
   {
      //noinspection unchecked
      return (T) Utilities.getField(fieldOwner.getClass(), fieldName, fieldOwner);
   }

   /**
    * Gets the value of a non-accessible field from a given object, <em>assuming</em> there is only
    * one field declared in the class of the given object whose type can receive values of the
    * specified field type.
    *
    * @param fieldOwner the instance from which to get the field value
    * @param fieldType the declared type of the field, or a sub-type of the declared field type
    *
    * @see #getField(Object, String)
    *
    * @throws IllegalArgumentException if either the desired field is not found, or more than one is
    */
   protected final <T> T getField(Object fieldOwner, Class<T> fieldType)
   {
      //noinspection unchecked
      return Utilities.getField(fieldOwner.getClass(), fieldType, fieldOwner);
   }

   /**
    * Gets the value of a non-accessible static field defined in a given class.
    *
    * @param fieldOwner the class from which to get the field value
    * @param fieldName the name of the static field to get
    *
    * @see #setField(Class, String, Object)
    */
   protected final <T> T getField(Class<?> fieldOwner, String fieldName)
   {
      //noinspection unchecked
      return (T) Utilities.getField(fieldOwner, fieldName, null);
   }

   /**
    * Gets the value of a non-accessible static field defined in a given class.
    *
    * @param fieldOwner the class from which to get the field value
    * @param fieldType the declared type of the field, or a sub-type of the declared field type
    *
    * @see #setField(Class, String, Object)
    */
   protected final <T> T getField(Class<?> fieldOwner, Class<T> fieldType)
   {
      //noinspection unchecked
      return Utilities.getField(fieldOwner, fieldType, null);
   }

   /**
    * Sets the value of a non-accessible field on a given object.
    * <p/>
    * <a href="http://jmockit.googlecode.com/svn/trunk/www/tutorial/BehaviorBasedTesting.html#deencapsulation">In the Tutorial</a>
    *
    * @param fieldOwner the instance on which to set the field value
    * @param fieldName the name of the field to set
    * @param fieldValue the value to set the field to
    *
    * @see #setField(Class, String, Object)
    */
   protected final void setField(Object fieldOwner, String fieldName, Object fieldValue)
   {
      Utilities.setField(fieldOwner.getClass(), fieldOwner, fieldName, fieldValue);
   }

   /**
    * Same as {@link #setField(Object, String, Object)}, except that the field is looked up by the
    * type of the given field value instead of by name.
    *
    * @throws IllegalArgumentException if no field or more than one is found in the target class to
    * which the given value can be assigned
    */
   protected final void setField(Object fieldOwner, Object fieldValue)
   {
      Utilities.setField(fieldOwner.getClass(), fieldOwner, null, fieldValue);
   }

   /**
    * Sets the value of a non-accessible static field on a given class.
    *
    * @param fieldOwner the class on which the static field is defined
    * @param fieldName the name of the field to set
    * @param fieldValue the value to set the field to
    */
   protected final void setField(Class<?> fieldOwner, String fieldName, Object fieldValue)
   {
      Utilities.setField(fieldOwner, null, fieldName, fieldValue);
   }

   /**
    * Same as {@link #setField(Class, String, Object)}, except that the field is looked up by the
    * type of the given field value instead of by name.
    *
    * @param fieldOwner  the class on which the static field is defined
    * @param fieldValue the value to set the field to
    */
   protected final void setField(Class<?> fieldOwner, Object fieldValue)
   {
      Utilities.setField(fieldOwner, null, null, fieldValue);
   }
}
