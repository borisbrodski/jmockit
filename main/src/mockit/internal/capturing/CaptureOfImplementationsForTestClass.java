/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.capturing;

import mockit.external.asm4.*;
import mockit.internal.*;

public final class CaptureOfImplementationsForTestClass extends CaptureOfImplementations
{
   public CaptureOfImplementationsForTestClass() {}

   @Override
   public ClassVisitor createModifier(ClassLoader classLoader, ClassReader cr, String baseTypeDesc)
   {
      return new StubOutModifier(cr, null);
   }
}
