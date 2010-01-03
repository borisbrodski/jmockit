/*
 * JMockit
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
package mockit.internal.startup;

import java.lang.instrument.*;
import java.security.*;
import java.util.regex.*;

import mockit.external.asm.*;
import mockit.internal.*;
import mockit.internal.annotations.*;
import mockit.internal.core.*;

final class ExternalMockTransformer implements ClassFileTransformer
{
   private final String mockClassName;
   private final Pattern realClassNameRegex;
   private final MockMethods mocks;

   ExternalMockTransformer(
      String mockClassName, boolean mockClassIsAnnotated, String realClassNameRegex)
   {
      this.mockClassName = mockClassName;
      this.realClassNameRegex = Pattern.compile(realClassNameRegex);
      mocks = collectMocksFromExternalMockClass(mockClassIsAnnotated);
   }

   private MockMethods collectMocksFromExternalMockClass(boolean mockClassIsAnnotated)
   {
      MockMethods mocksCollected;
      ClassVisitor mockCollector;

      if (mockClassIsAnnotated) {
         AnnotatedMockMethods annotatedMocks = new AnnotatedMockMethods(null);
         mocksCollected = annotatedMocks;
         mockCollector = new AnnotatedMockMethodCollector(annotatedMocks);
      }
      else {
         mocksCollected = new MockMethods();
         mockCollector = new MockMethodCollector(mocksCollected, false);
      }

      ClassReader mcReader = ClassFile.createClassFileReader(mockClassName);
      mcReader.accept(mockCollector, true);

      if (mocksCollected.getMethodCount() == 0) {
         throw new IllegalArgumentException(
            "External mock class " + mockClassName + " with no mocks");
      }

      return mocksCollected;
   }

   public byte[] transform(
      ClassLoader loader, String className, Class<?> classBeingRedefined,
      ProtectionDomain protectionDomain, byte[] classfileBuffer)
   {
      // Assumes classes starting with "$" are dynamically generated, as an optimization.
      if (classBeingRedefined == null && className.charAt(0) != '$') {
         return modifyClassIfMatchesRealClassRegex(className, classfileBuffer);
      }

      return null;
   }

   private byte[] modifyClassIfMatchesRealClassRegex(String className, byte[] classfileBuffer)
   {
      String fqClassName = className.replace('/', '.');

      if (realClassNameRegex.matcher(fqClassName).matches()) {
         System.out.println("JMockit: Redefining " + fqClassName + " with " + mockClassName);
         return modifyRealClass(className, classfileBuffer);
      }

      return null;
   }

   private byte[] modifyRealClass(String realClassDescriptor, byte[] classfileBuffer)
   {
      ClassReader rcReader = new ClassReader(classfileBuffer);
      // TODO: instantiate mock?
      ClassWriter rcWriter;

      if (mocks instanceof AnnotatedMockMethods) {
         rcWriter =
            new AnnotationsModifier(rcReader, realClassDescriptor, (AnnotatedMockMethods) mocks);
      }
      else {
         rcWriter = new RealClassModifier(rcReader, realClassDescriptor, null, mocks, true);
      }

      RedefinitionEngine redefinition = new RedefinitionEngine(null, null, null, mocks);
      
      return redefinition.modifyRealClass(rcReader, rcWriter, mockClassName);
   }
}
