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

import java.util.logging.*;

import mockit.*;

/**
 * A mock class containing mocks and stubs for the standard logging API in the JDK
 * (java.util.logging).
 * <p/>
 * When a test class is annotated as <code>@UsingMocksAndStubs(JDKLoggingMocks.class)</code>, all
 * production code touched by the tests in that class will receive mock <code>Logger</code>
 * instances instead of real ones, when one of the factory methods in class
 * <code>java.util.logging.Logger</code> is called.
 */
@SuppressWarnings({"UnusedDeclaration"})
@MockClass(realClass = Logger.class)
public final class JDKLoggingMocks
{
   private static final Logger MOCK_LOG = new Logger(null, null) {};

   private JDKLoggingMocks() {}

   /**
    * Returns a singleton mock <code>Logger</code> instance, whose methods do nothing.
    */
   @Mock public static Logger getAnonymousLogger() { return MOCK_LOG; }

   /**
    * Returns a singleton mock <code>Logger</code> instance, whose methods do nothing.
    */
   @Mock public static Logger getAnonymousLogger(String resourceBundleName) { return MOCK_LOG; }

   /**
    * Returns a singleton mock <code>Logger</code> instance, whose methods do nothing.
    */
   @Mock public static Logger getLogger(String name) { return MOCK_LOG; }

   /**
    * Returns a singleton mock <code>Logger</code> instance, whose methods do nothing.
    */
   @Mock public static Logger getLogger(String name, String resourceBundleName) { return MOCK_LOG; }
}
