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
import java.io.*;

import org.junit.*;

import mockit.integration.junit4.*;
import mockit.*;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.BasicMarkerFactory;
import org.apache.commons.logging.*;

@UsingMocksAndStubs(
   {JDKLoggingMocks.class, Log4jMocks.class, CommonsLoggingMocks.class, Slf4jMocks.class})
public final class LoggingIntegrationsTest extends JMockitTest
{
   private static PrintStream originalErr;

   @BeforeClass
   public static void redirectSystemOut()
   {
      originalErr = System.err;

      OutputStream testOutput = new OutputStream()
      {
         @Override
         public void write(int b) { fail("Logger wrote output message!"); }
      };

      System.setErr(new PrintStream(testOutput));
   }

   @AfterClass
   public static void restoreSystemErr()
   {
      System.setErr(originalErr);
   }

   @Test
   public void jdkLoggingShouldLogNothing()
   {
      Logger log1 = Logger.getAnonymousLogger();
      Logger log2 = Logger.getAnonymousLogger("bundle");
      Logger log3 = Logger.getLogger(LoggingIntegrationsTest.class.getName());
      Logger log4 = Logger.getLogger(LoggingIntegrationsTest.class.getName(), "bundle");

      assertFalse(log1.isLoggable(Level.ALL));
      log1.severe("testing that logger does nothing");
      log2.setLevel(Level.WARNING);
      log2.info("testing that logger does nothing");
      log3.warning("testing that logger does nothing");
      log4.fine("testing that logger does nothing");
      log4.finest("testing that logger does nothing");
   }

   @Test
   public void log4jShouldLogNothing()
   {
      org.apache.log4j.Logger log1 = org.apache.log4j.Logger.getLogger("test");
      org.apache.log4j.Logger log2 =
         org.apache.log4j.Logger.getLogger(LoggingIntegrationsTest.class);
      org.apache.log4j.Logger log3 = org.apache.log4j.Logger.getLogger("test", null);
      org.apache.log4j.Logger log4 = org.apache.log4j.Logger.getRootLogger();

      assertFalse(log1.isTraceEnabled());
      log1.error("testing that log4j does nothing");
      log2.setLevel(org.apache.log4j.Level.FATAL);
      log2.debug("testing that log4j does nothing");
      log3.fatal("testing that log4j does nothing");
      log4.info("testing that log4j does nothing");
   }

   @Test
   public void commonsLoggingShouldLogNothing()
   {
      Log log1 = LogFactory.getLog("test");
      Log log2 = LogFactory.getLog(LoggingIntegrationsTest.class);

      assertFalse(log1.isTraceEnabled());
      log1.error("testing that log does nothing");
      assertFalse(log1.isDebugEnabled());
      log2.trace("test");
      log2.debug("testing that log does nothing");
   }

   @Test
   public void slf4jShouldLogNothing()
   {
      org.slf4j.Logger log1 = LoggerFactory.getLogger("test");
      org.slf4j.Logger log2 = LoggerFactory.getLogger(LoggingIntegrationsTest.class);

      assertFalse(log1.isTraceEnabled());
      log1.error("testing that logger does nothing", 1, "2");
      assertFalse(log1.isDebugEnabled());
      log2.trace(new BasicMarkerFactory().getMarker("m"), "test");
      log2.debug("testing that logger does nothing");
   }
}
