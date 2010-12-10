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
import java.util.*;

import mockit.external.asm.*;
import mockit.external.asm.commons.*;
import mockit.internal.*;

public final class SuperConstructorCollector extends EmptyVisitor
{
   public static final SuperConstructorCollector INSTANCE = new SuperConstructorCollector();

   private final Map<String, String> cache = new HashMap<String, String>();
   @SuppressWarnings({"FieldAccessedSynchronizedAndUnsynchronized"})
   private String constructorDesc;

   private SuperConstructorCollector() {}

   public synchronized String findConstructor(String className)
   {
      constructorDesc = cache.get(className);

      if (constructorDesc != null) {
         return constructorDesc;
      }

      ClassReader cr = createClassReader(className);

      try { cr.accept(this, true); } catch (VisitInterruptedException ignore) {}
      cache.put(className, constructorDesc);
      
      return constructorDesc;
   }

   private ClassReader createClassReader(String className)
   {
      try {
         return ClassFile.readClass(className);
      }
      catch (IOException e) {
         throw new RuntimeException("Failed to read class file for " + className, e);
      }
   }

   @Override
   public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions)
   {
      if ("<init>".equals(name)) {
         constructorDesc = desc;
         throw VisitInterruptedException.INSTANCE;
      }

      return null;
   }

   @Override
   public FieldVisitor visitField(int access, String name, String desc, String signature, Object value)
   {
      return null;
   }
}
