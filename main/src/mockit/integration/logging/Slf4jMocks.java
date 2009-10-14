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

import mockit.*;
import org.slf4j.*;

/**
 * A mock class containing mocks and stubs for the SLF4j API.
 * <p/>
 * When a test class is annotated as <code>@UsingMocksAndStubs(Slf4jMocks.class)</code>, all
 * production code touched by the tests in that class will receive mock <code>Logger</code>
 * instances instead of real ones, when one of the factory methods in class
 * <code>org.slf4j.LoggerFactory</code> is called.
 */
@SuppressWarnings({"UnusedDeclaration"})
@MockClass(realClass = LoggerFactory.class, stubs = "getILoggerFactory")
public final class Slf4jMocks
{
   private static final Logger MOCK_LOGGER = Mockit.newEmptyProxy(Logger.class);

   private Slf4jMocks() {}

   /**
    * Returns a singleton mock <code>Logger</code> instance, whose methods do nothing.
    */
   @Mock public static Logger getLogger(String name) { return MOCK_LOGGER; }

   /**
    * Returns a singleton mock <code>Logger</code> instance, whose methods do nothing.
    */
   @Mock public static Logger getLogger(Class<?> clazz) { return MOCK_LOGGER; }
}
