/*
 * The TypeSafe classes, and their descendants, need a mechanism to find out what type has been used
 * as a parameter for the concrete matcher.
 * Unfortunately, this type is lost during type erasure so we need to use reflection
 * to get it back, by picking out the type of a known parameter to a known method. 
 * The catch is that, with bridging methods, this type is only visible in the class that actually
 * implements the expected method, so the ReflectiveTypeFinder needs to be applied to that class or
 * a subtype.
 *
 * For example, the abstract <code>TypeSafeDiagnosingMatcher&lt;T&gt;</code> defines an abstract
 * method
 * <pre>protected abstract boolean matchesSafely(T item, Description mismatchDescription);</pre>.
 * By default it uses <code>new ReflectiveTypeFinder("matchesSafely", 2, 0); </code> to find the
 * parameterized type. If we create a <code>TypeSafeDiagnosingMatcher&lt;String&gt;</code>, the type
 * finder will return <code>String.class</code>.
 *
 * A <code>FeatureMatcher</code> is an abstract subclass of <code>TypeSafeDiagnosingMatcher</code>. 
 * Although it has a templated implementation of
 * <code>matchesSafely(&lt;T&gt;, Description);</code>, the actual run-time signature of this is
 * <code>matchesSafely(Object, Description);</code>.
 * Instead, we must find the type by reflecting on the concrete implementation of
 * <pre>protected abstract U featureValueOf(T actual);</pre>
 * a method which is declared in <code>FeatureMatcher</code>.
 *
 * In short, use this to extract a type from a method in the leaf class of a templated class
 * hierarchy.
 *
 * @author Steve Freeman
 * @author Nat Pryce
 */
package mockit.external.hamcrest.internal;

import java.lang.reflect.Method;

public final class ReflectiveTypeFinder
{
   public Class<?> findExpectedType(Class<?> fromClass)
   {
      for (Class<?> c = fromClass; c != Object.class; c = c.getSuperclass()) {
         for (Method method : c.getDeclaredMethods()) {
            if (canObtainExpectedTypeFrom(method)) {
               return method.getParameterTypes()[0];
            }
         }
      }

      throw new Error("Cannot determine correct type for matchesSafely() method.");
   }

   private boolean canObtainExpectedTypeFrom(Method method)
   {
      return
         !method.isSynthetic() && "matchesSafely".equals(method.getName()) &&
         method.getParameterTypes().length == 1;
   }
}