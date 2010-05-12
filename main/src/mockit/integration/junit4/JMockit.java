/*
 * JMockit
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
package mockit.integration.junit4;

import org.junit.runners.*;
import org.junit.runners.model.*;

import mockit.internal.startup.*;

/**
 * A test runner for <em>JUnit 4.5+</em>, with special modifications to integrate with JMockit.
 * Normally, though, it shouldn't be necessary to use this class. Instead, simply make sure that
 * {@code jmockit.jar} precedes {@code junit-4.n.jar} in the classpath, when running on JDK 1.6+
 * (if running on JDK 1.5, then "-javaagent:jmockit.jar" will be mandatory and this class would have
 * no effect since JMockit would already be initialized).
 * <p/>
 * If you happen to already have a {@code @RunWith} annotation in your JUnit test classes, you
 * can still activate integration by simply making sure that JMockit is initialized before any test
 * is executed. For example, the following static block in the test class or in a base test class
 * will get the job done:
 * <pre>
   static
   {
      Mockit.setUpMocks();
   }
 * </pre>
 * <a href="http://jmockit.googlecode.com/svn/trunk/www/tutorial/RunningTests.html">Tutorial</a>
 *
 * @see JMockitTest
 * @deprecated Use the {@code -javaagent:jmockit.jar} JVM parameter, or make sure that
 * {@code junit.jar} (4.5+) appears before {@code jmockit.jar} in the classpath if running under
 * JDK 1.6.
 */
@Deprecated
public final class JMockit extends BlockJUnit4ClassRunner
{
   static
   {
      Startup.initializeIfNeeded();
   }

   /**
    * Constructs a new instance of the test runner.
    *
    * @throws InitializationError if the test class is malformed
    */
   public JMockit(Class<?> testClass) throws InitializationError
   {
      super(testClass);
   }
}
