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
package mockit.integration.logging;

import org.apache.log4j.*;
import org.apache.log4j.spi.*;

import mockit.*;
import static mockit.Instantiation.*;

/**
 * A mock class containing mocks and stubs for the Log4j API.
 * <p/>
 * When a test class is annotated as {@code @UsingMocksAndStubs(Log4jMocks.class)}, all
 * production code touched by the tests in that class will receive mock {@code Logger} instances
 * instead of real ones, when one of the factory methods in class {@code org.apache.log4j.Logger}
 * is called.
 * <p/>
 * <a href="http://jmockit.googlecode.com/svn/trunk/www/tutorial/UsingMocksAndStubs.html">Tutorial</a>
 */
@SuppressWarnings({"UnusedDeclaration"})
@MockClass(
   realClass = Logger.class, instantiation = PerMockSetup, stubs = {"trace", "isTraceEnabled"})
public final class Log4jMocks
{
   private static final Logger MOCK_LOGGER = new RootLogger(Level.OFF);

   public Log4jMocks()
   {
      Mockit.stubOut(Category.class);
   }

   /**
    * Returns a singleton mock {@code Logger} instance, whose methods do nothing.
    */
   @Mock public static Logger getLogger(String name) { return MOCK_LOGGER; }

   /**
    * Returns a singleton mock {@code Logger} instance, whose methods do nothing.
    */
   @Mock public static Logger getLogger(Class<?> clazz) { return MOCK_LOGGER; }

   /**
    * Returns a singleton mock {@code Logger} instance, whose methods do nothing.
    */
   @Mock public static Logger getRootLogger() { return MOCK_LOGGER; }

   /**
    * Returns a singleton mock {@code Logger} instance, whose methods do nothing.
    */
   @Mock public static Logger getLogger(String name, LoggerFactory lf) { return MOCK_LOGGER; }
}
