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
package mockit.integration.junit3;

import junit.framework.*;
import mockit.internal.startup.*;

/**
 * Simply makes sure that JMockit is initialized before the first JUnit 3.8 test runs.
 * <p/>
 * If using the JVM parameter "-javaagent:jmockit.jar" or using JDK 1.6+ with {@code jmockit.jar}
 * preceding {@code junit-4.n.jar} in the classpath, this class doesn't need to be used.
 * Otherwise, all JUnit 3.8 test classes will need to either extend it or verify the initialization
 * of JMockit by calling {@code Mockit.setUpMocks()} in a static initializer or constructor of the
 * test class.
 * <p/>
 * <a href="http://jmockit.googlecode.com/svn/trunk/www/tutorial/RunningTests.html">Tutorial</a>
 */
@SuppressWarnings({"JUnitTestCaseWithNoTests", "UnconstructableJUnitTestCase", "UnusedDeclaration"})
public class JMockitTestCase extends TestCase
{
   static
   {
      Startup.verifyInitialization();
   }

   public JMockitTestCase() {}
   public JMockitTestCase(String name) { super(name); }
}
