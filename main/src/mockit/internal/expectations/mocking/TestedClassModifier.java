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
