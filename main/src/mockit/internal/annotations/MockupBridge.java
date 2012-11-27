/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.annotations;

import java.lang.reflect.*;

import mockit.internal.*;
import mockit.internal.state.*;

public final class MockupBridge extends MockingBridge
{
   public static final MockingBridge MB = new MockupBridge();

   private MockupBridge() { super(MockupBridge.class); }

   public Object invoke(Object mocked, Method method, Object[] args) throws Throwable
   {
      boolean enteringMethod = (Boolean) args[0];
      String mockClassDesc = (String) args[1];

      if (notToBeMocked(mocked, mockClassDesc)) {
         return enteringMethod ? false : Void.class;
      }

      int mockStateIndex = (Integer) args[2];

      if (enteringMethod) {
         return TestRun.updateMockState(mockClassDesc, mockStateIndex);
      }
      else {
         TestRun.exitReentrantMock(mockClassDesc, mockStateIndex);
         return null;
      }
   }
}
