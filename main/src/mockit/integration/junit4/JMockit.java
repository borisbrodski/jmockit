/*
 * JMockit
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
package mockit.integration.junit4;

import org.junit.runners.*;
import org.junit.runners.model.*;

import mockit.*;
import mockit.internal.startup.*;

/**
 * A test runner for <em>JUnit 4.5+</em>, with special modifications to integrate with JMockit.
 * <p/>
 * Specifying this runner with {@code @RunWith} in every test class is <strong>not</strong> the
 * only way to apply those modifications, though. When the {@code -javaagent:jmockit.jar} JVM
 * argument is specified, JMockit will automatically apply them to the standard JUnit test runner.
 * Additionally, the modifications will be applied (if they have not been already) the first time
 * a method belonging to the JMockit API is called, for example in a test or setup method.
 * <p/>
 * In any case, the integration adds the following benefits to test code:
 * <ol>
 * <li>
 * Unexpected invocations specified through the Annotations or the Expectations API are
 * automatically verified just before the execution of a test ends (that is, after the test itself
 * executed, but before any {@code @After} methods are executed).
 * </li>
 * <li>
 * Any mock classes applied with the Core or Annotations API from inside a test method will be
 * discarded before the execution of the test method ends, so it is not necessary to call
 * {@link Mockit#tearDownMocks()} (or {@link Mockit#restoreAllOriginalDefinitions()}) at the end of
 * the test, be it from inside the test method itself or from an {@code @After} method.
 * The same is <em>not</em> true for mocks applied in a {@code @Before} method, however: you
 * <em>will</em> need to explicitly tear down those mock classes in an {@code @After} method, so
 * that the mocked real classes are properly restored to their original definitions.
 * </li>
 * <li>
 * Any {@linkplain mockit.MockClass mock class} set up for the whole test class (either through
 * a call to {@link Mockit#setUpMocks} from inside a {@code @BeforeClass} method, or by
 * annotating the test class with {@link mockit.UsingMocksAndStubs}) will only apply to the test
 * methods in this same test class.
 * That is, you should not explicitly tell JMockit to restore the mocked classes in an
 * {@code @AfterClass} method.
 * </li>
 * </ol>
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
 */
public final class JMockit extends BlockJUnit4ClassRunner
{
   static
   {
      Startup.verifyInitialization();
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
