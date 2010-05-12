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
package mockit.integration.testng;

import mockit.internal.startup.*;

/**
 * Simply makes sure that JMockit is initialized before the first TestNG test runs.
 * <p/>
 * If using the JVM parameter "-javaagent:jmockit.jar" this class doesn't need to be used.
 * For another way to initialize JMockit while avoiding the need to extend this class, see the
 * {@link mockit.integration.testng.Initializer}.
 * <p/>
 * <a href="http://jmockit.googlecode.com/svn/trunk/www/tutorial/RunningTests.html">Tutorial</a>
 *
 * @deprecated Use the {@code -javaagent:jmockit.jar} JVM parameter, or make sure that TestNG uses
 * the {@link mockit.integration.testng.Initializer} class as a listener.
 */
@Deprecated
public class JMockitTestNG
{
   static
   {
      Startup.initializeIfNeeded();
   }

   protected JMockitTestNG() {}
}
