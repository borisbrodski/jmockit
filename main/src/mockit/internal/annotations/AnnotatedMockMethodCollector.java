/*
 * JMockit Annotations
 * Copyright (c) 2006-2009 Rogério Liesenfeld
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

import java.lang.reflect.*;

import org.objectweb.asm2.*;
import org.objectweb.asm2.commons.*;

import mockit.*;
import mockit.internal.*;

/**
 * Responsible for collecting the signatures of all methods and constructors defined in a given mock
 * class which are explicitly annotated as {@link mockit.Mock mocks}.
 */
public final class AnnotatedMockMethodCollector extends BaseMockCollector
{
   private final Class<?> realClass;

   public AnnotatedMockMethodCollector(AnnotatedMockMethods mockMethods)
   {
      super(mockMethods);
      realClass = mockMethods.realClass;
   }

   /**
    * Adds the method or constructor specified to the set of mock methods, representing it as
    * <code>name + desc</code>, as long as it's appropriate for such method or constructor to be a
    * mock, as indicated by its access modifiers and by the presence of the {@link Mock} annotation.
    *
    * @param access access modifiers, indicating "public", "static", and so on
    * @param name the method or constructor name
    * @param methodDesc internal JVM description of parameters and return type
    * @param signature generic signature for a Java 5 generic method, ignored since redefinition
    * only needs to consider the "erased" signature
    * @param exceptions zero or more thrown exceptions in the method "throws" clause, also ignored
    *
    * @return always null, since we are not interested in visiting the code of the method
    */
   @Override
   public MethodVisitor visitMethod(
      final int access, final String name, final String methodDesc,
      String signature, String[] exceptions)
   {
      super.visitMethod(access, name, methodDesc, signature, exceptions);

      if (isMethodWithInvalidAccess(access)) {
         return null;
      }

      return new EmptyVisitor()
      {
         @Override
         public AnnotationVisitor visitAnnotation(String desc, boolean visible)
         {
            if ("Lmockit/Mock;".equals(desc)) {
               String nameAndDesc =
                  mockMethods.addMethod(name, methodDesc, Modifier.isStatic(access));

               return new MockAnnotationVisitor(nameAndDesc);
            }

            return this;
         }
      };
   }

   private final class MockAnnotationVisitor extends EmptyVisitor
   {
      private final String mockNameAndDesc;
      private MockState mockState;

      private MockAnnotationVisitor(String mockNameAndDesc)
      {
         this.mockNameAndDesc = mockNameAndDesc;
      }

      @Override
      public void visit(String name, Object value)
      {
         if ("invocations".equals(name)) {
            getMockState().expectedInvocations = (Integer) value;
         }
         else if ("minInvocations".equals(name)) {
            getMockState().minExpectedInvocations = (Integer) value;
         }
         else if ("maxInvocations".equals(name)) {
            getMockState().maxExpectedInvocations = (Integer) value;
         }
         else {
            boolean reentrant = (Boolean) value;

            if (reentrant) {
               getMockState().makeReentrant();
            }
         }
      }

      private MockState getMockState()
      {
         if (mockState == null) {
            mockState = new MockState(realClass, mockNameAndDesc);
         }

         return mockState;
      }

      @Override
      public void visitEnd()
      {
         if (mockState != null) {
            ((AnnotatedMockMethods) mockMethods).addMockState(mockState);
         }
      }
   }
}
