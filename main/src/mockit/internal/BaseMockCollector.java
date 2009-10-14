/*
 * JMockit Core
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
package mockit.internal;

import mockit.internal.core.*;
import org.objectweb.asm2.*;
import static org.objectweb.asm2.Opcodes.*;
import org.objectweb.asm2.commons.*;

public class BaseMockCollector extends EmptyVisitor
{
   private static final int INVALID_FIELD_ACCESS = ACC_FINAL + ACC_STATIC + ACC_SYNTHETIC;

   protected final MockMethods mockMethods;
   private String enclosingClassDescriptor;

   public BaseMockCollector(MockMethods mockMethods)
   {
      this.mockMethods = mockMethods;
   }

   @Override
   public final void visit(
      int version, int access, String name, String signature, String superName, String[] interfaces)
   {
      mockMethods.setMockClassInternalName(name);

      int p = name.lastIndexOf('$');

      if (p > 0) {
         enclosingClassDescriptor = "(L" + name.substring(0, p) + ";)V";
      }
   }

   @Override
   public final FieldVisitor visitField(
      int access, String name, String desc, String signature, Object value)
   {
      if ((access & INVALID_FIELD_ACCESS) == 0 && "it".equals(name)) {
         mockMethods.setWithItField(true);
      }

      return null;
   }

   @Override
   public MethodVisitor visitMethod(
      int access, String name, String desc, String signature, String[] exceptions)
   {
      if (enclosingClassDescriptor != null) {
         if ("<init>".equals(name) && desc.equals(enclosingClassDescriptor)) {
            mockMethods.setInnerMockClass(true);
            enclosingClassDescriptor = null;
         }
      }

      return null;
   }
}
