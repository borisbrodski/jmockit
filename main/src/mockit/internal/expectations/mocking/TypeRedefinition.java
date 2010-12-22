/*
 * JMockit Expectations & Verifications
 * Copyright (c) 2006-2010 Rogério Liesenfeld
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
package mockit.internal.expectations.mocking;

import mockit.external.asm.*;
import mockit.internal.util.*;

final class TypeRedefinition extends BaseTypeRedefinition
{
   private final Object parentObject;

   TypeRedefinition(Object parentObject, MockedType typeMetadata)
   {
      super(typeMetadata.getClassType());
      this.parentObject = parentObject;
      this.typeMetadata = typeMetadata;
   }

   void redefineTypeForFinalField()
   {
      typeMetadata.buildMockingConfiguration();
      adjustTargetClassIfRealClassNameSpecified();

      if (targetClass == null || targetClass.isInterface()) {
         throw new IllegalArgumentException(
            "Final mock field must be of a class type, or otherwise the real class must be " +
            "specified through the @Mocked annotation:\n" + typeMetadata.mockId);
      }

      Integer mockedClassId = redefineClassesFromCache();

      if (mockedClassId != null) {
         redefineMethodsAndConstructorsInTargetType();
         storeRedefinedClassesInCache(mockedClassId);
      }
   }

   Object redefineType()
   {
      typeMetadata.buildMockingConfiguration();
      adjustTargetClassIfRealClassNameSpecified();

      return redefineType(typeMetadata.declaredType);
   }

   private void adjustTargetClassIfRealClassNameSpecified()
   {
      String realClassName = typeMetadata.getRealClassName();

      if (realClassName.length() > 0) {
         targetClass = Utilities.loadClass(realClassName);
      }
   }

   @Override
   ExpectationsModifier createModifier(Class<?> realClass, ClassReader classReader)
   {
      ExpectationsModifier modifier = new ExpectationsModifier(realClass.getClassLoader(), classReader, typeMetadata);

      if (typeMetadata.injectable) {
         modifier.useDynamicMockingForInstanceMethods(typeMetadata);
      }

      return modifier;
   }

   @Override
   String getNameForConcreteSubclassToCreate()
   {
      Package testPackage = parentObject.getClass().getPackage();
      String prefix = testPackage == null ? "" : testPackage.getName() + '.';

      return prefix + Utilities.GENERATED_SUBCLASS_PREFIX + typeMetadata.mockId;
   }
}
