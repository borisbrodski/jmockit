/*
 * JMockit Core
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
package mockit.internal.annotations;

import java.util.*;

import static org.junit.Assert.*;
import org.junit.*;

import mockit.external.asm.*;
import mockit.internal.core.*;

import static mockit.external.asm.Opcodes.*;

public final class MockMethodCollectorTest
{
   final AnnotatedMockMethods mockMethods = new AnnotatedMockMethods(null);
   final MockMethodCollector collector = new MockMethodCollector(mockMethods, false);

   @Test
   public void visitPublicStaticVoidMethodWithTwoParameters()
   {
      assertVisitMethod(ACC_PUBLIC + ACC_STATIC, "someMethod", "(IL)V");
   }

   private void assertVisitMethod(int access, String name, String desc)
   {
      int previousMethodCount = mockMethods.getMethodCount();

      MethodVisitor codeVisitor = collector.visitMethod(access, name, desc, null, null);

      assertNull(codeVisitor);
      assertEquals(previousMethodCount + 1, mockMethods.getMethodCount());
      assertTrue(mockMethods.containsMethod(name, desc));
   }

   @Test
   public void visitInstanceStringMethodWithNoParameters()
   {
      assertVisitMethod(ACC_PUBLIC, "someMethod", "()Ljava/lang/String;");
   }

   @Test
   public void visitPublicConstructorWithOneParameter()
   {
      assertVisitMethod(ACC_PUBLIC, "<init>", "(J)V");
   }

   @Test
   public void visitDefaultConstructor()
   {
      assertVisitWhatIsNotAllowedToBeAMock(ACC_PUBLIC, "<init>", "()V");
   }

   private void assertVisitWhatIsNotAllowedToBeAMock(int access, String name, String desc)
   {
      Collection<String> previousMethods = mockMethods.methods;

      MethodVisitor codeVisitor = collector.visitMethod(access, name, desc, null, null);

      assertNull(codeVisitor);
      assertEquals(previousMethods, mockMethods.methods);
   }

   @Test
   public void visitNonPublicConstructor()
   {
      assertVisitWhatIsNotAllowedToBeAMock(ACC_PROTECTED, "<init>", "()V");
   }

   @Test
   public void visitSyntheticMethod()
   {
      assertVisitWhatIsNotAllowedToBeAMock(ACC_SYNTHETIC, "method", "()V");
   }

   @Test
   public void visitBridgeMethod()
   {
      assertVisitWhatIsNotAllowedToBeAMock(ACC_BRIDGE, "method", "()V");
   }

   @Test
   public void visitAbstractMethod()
   {
      assertVisitWhatIsNotAllowedToBeAMock(ACC_ABSTRACT, "method", "()V");
   }

   @Test
   public void visitNativeMethod()
   {
      assertVisitWhatIsNotAllowedToBeAMock(ACC_NATIVE, "method", "()V");
   }

   @Test
   public void visitSecondMethod()
   {
      String firstMethodName = "aMethod";
      String firstMethodDesc = "()V";
      mockMethods.addMethod(false, firstMethodName, firstMethodDesc, false);

      assertVisitMethod(ACC_PUBLIC + ACC_FINAL, "booleanMethod", "()Z");
      assertTrue(mockMethods.containsMethod(firstMethodName, firstMethodDesc));
   }
}
