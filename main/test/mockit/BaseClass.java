/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import java.util.*;

class BaseClass
{
   protected int baseInt;
   protected String baseString;
   protected Set<Boolean> baseSet;
   private long longField;

   void setLongField(long value) { longField = value; }
}
