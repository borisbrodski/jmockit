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

import org.junit.runner.notification.*;
import org.junit.runner.*;

import mockit.*;
import mockit.integration.*;
import mockit.internal.state.*;
import mockit.internal.util.*;

/**
 * Startup mock which works in conjunction with {@link BlockJUnit4ClassRunnerDecorator} to provide
 * JUnit 4.5+ integration.
 * <p/>
 * This class is not supposed to be accessed from user code. JMockit will automatically load it at
 * startup.
 */
@MockClass(realClass = RunNotifier.class)
public final class RunNotifierDecorator
{
   public RunNotifier it;

   @Mock(reentrant = true)
   public void fireTestStarted(Description description)
   {
      Class<?> currentTestClass = TestRun.getCurrentTestClass();

      if (currentTestClass != null) {
         String testClassName = getTestClassName(description);

         if (!testClassName.equals(currentTestClass.getName())) {
            Class<?> testClass = Utilities.loadClass(testClassName);
            discardCurrentTestClassIfNoLongerValid(testClass);
         }
      }

      it.fireTestStarted(description);
   }

   private String getTestClassName(Description description)
   {
      String testClassName = description.getDisplayName();
      int p = testClassName.indexOf('(');
      return testClassName.substring(p + 1, testClassName.length() - 1);
   }

   private void discardCurrentTestClassIfNoLongerValid(Class<?> testClass)
   {
      Class<?> currentTestClass = TestRun.getCurrentTestClass();

      if (!currentTestClass.isAssignableFrom(testClass)) {
         TestRunnerDecorator.cleanUpMocksFromPreviousTestClass();
         TestRun.setCurrentTestClass(null);
      }
   }
}
