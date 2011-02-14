/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations.invocation;

import mockit.internal.util.*;

final class ExpectationError extends AssertionError
{
   private String message;

   @Override
   public String toString() { return message; }

   void prepareForDisplay(String title)
   {
      message = title;
      Utilities.filterStackTrace(this);
   }

   void defineCause(String title, AssertionError error)
   {
      prepareForDisplay(title);
      error.initCause(this);
   }
}
