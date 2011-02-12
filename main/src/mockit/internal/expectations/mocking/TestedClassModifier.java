/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations.mocking;

import java.util.*;

import mockit.external.asm.*;
import mockit.internal.*;

final class TestedClassModifier extends BaseClassModifier
{
   private final List<MockedType> mockedTypes;

   TestedClassModifier(ClassReader classReader, List<MockedType> mockedTypes)
   {
      super(classReader);
      this.mockedTypes = mockedTypes;
   }
   
   
}
