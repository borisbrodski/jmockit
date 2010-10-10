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
package mockit.internal.util;

import java.io.*;

import mockit.external.asm.*;
import mockit.external.asm.commons.*;
import mockit.internal.*;

public final class SuperConstructorCollector extends EmptyVisitor
{
   private final int desiredConstructorNo;
   private int constructorNo;
   private String targetConstructorDesc;

   public SuperConstructorCollector(int desiredConstructorNo)
   {
      this.desiredConstructorNo = desiredConstructorNo;
   }

   public String findConstructor(String superClassName)
   {
      constructorNo = 0;
      targetConstructorDesc = null;

      ClassReader cr;

      try {
         cr = ClassFile.readClass(superClassName);
      }
      catch (IOException e) {
         throw new RuntimeException("Failed to read class file for " + superClassName, e);
      }

      cr.accept(this, true);

      if (targetConstructorDesc == null) {
         throw new IllegalArgumentException(
            "Constructor number " + desiredConstructorNo + " not found in " + superClassName);
      }

      return targetConstructorDesc;
   }

   @Override
   public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions)
   {
      if ("<init>".equals(name)) {
         constructorNo++;

         if (constructorNo == desiredConstructorNo) {
            targetConstructorDesc = desc;
         }
      }

      return null;
   }

   @Override
   public FieldVisitor visitField(
      int access, String name, String desc, String signature, Object value)
   {
      return null;
   }
}
