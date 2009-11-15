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

import org.junit.*;
import org.junit.runner.*;

import mockit.internal.startup.*;

/**
 * A convenience base class for tests which use the JMockit toolkit with <em>JUnit 4.5+</em>.
 * Normally, though, it shouldn't be necessary to use this class. Instead, simply make sure that
 * {@code jmockit.jar} precedes {@code junit-4.n.jar} in the classpath, when running on JDK 1.6+
 * (if running on JDK 1.5, then "-javaagent:jmockit.jar" will be mandatory and this class would have
 * no effect since JMockit would already be initialized).
 * <p/>
 * Besides being annotated with {@code @RunWith(JMockit.class)}, the use of this base class for
 * your test classes has the advantage of inheriting {@link org.junit.Assert}, which eliminates the
 * need to statically import its methods.
 * <p/>
 * If JMockit is not explicitly initialized before the first test executes, it will still get
 * initialized on the first call to a method in the API. However, this will probably cause the first
 * test, and possibly others, to fail.
 * <p/>
 * <a href="http://jmockit.googlecode.com/svn/trunk/www/tutorial/RunningTests.html">Tutorial</a>
 */
@RunWith(JMockit.class)
@Ignore
public class JMockitTest extends Assert
{
   static
   {
      Startup.verifyInitialization();
   }
}
