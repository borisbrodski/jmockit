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
package mockit.integration.junit3.internal;

import java.lang.reflect.*;

import junit.framework.*;

import mockit.*;

/**
 * Startup mock which works in conjunction with {@linkplain JUnitTestCaseDecorator} to provide
 * JUnit 3.8 integration.
 * <p/>
 * This class is not supposed to be accessed from user code. JMockit will automatically load it at
 * startup.
 */
@MockClass(realClass = TestSuite.class)
public final class TestSuiteDecorator
{
   @Mock
   public boolean isTestMethod(Method m)
   {
      return
         !Modifier.isStatic(m.getModifiers()) && m.getReturnType() == Void.TYPE && 
         m.getName().startsWith("test");
   }
}