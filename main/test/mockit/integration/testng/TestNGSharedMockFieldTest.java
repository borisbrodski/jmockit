/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.integration.testng;

import static org.testng.Assert.*;
import org.testng.annotations.*;

import mockit.*;

public final class TestNGSharedMockFieldTest
{
   public interface Dependency
   {
      boolean doSomething();
      void doSomethingElse();
   }

   @NonStrict Dependency mock;

   @Test
   public void recordExpectationsOnSharedMock()
   {
      new Expectations() {{
         mock.doSomething(); result = true;
      }};

      assertTrue(mock.doSomething());
   }

   @Test
   public void recordExpectationsOnSharedMockAgain()
   {
      new Expectations() {{
         mock.doSomething(); result = true;
         mock.doSomethingElse();
      }};

      assertTrue(mock.doSomething());
   }
}
