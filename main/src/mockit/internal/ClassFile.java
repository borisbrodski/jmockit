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

import java.io.*;
import java.lang.reflect.*;

import mockit.internal.state.*;
import org.objectweb.asm2.*;

public final class ClassFile
{
   public static ClassReader createClassFileReader(String className)
   {
      byte[] fixedClassfile = TestRun.mockFixture().getFixedClassfile(className);

      if (fixedClassfile != null) {
         return new ClassReader(fixedClassfile);
      }

      try {
         return readClass(className);
      }
      catch (IOException e) {
         throw new RuntimeException("Failed to read class file for " + className, e);
      }
   }

   public static ClassReader readClass(String className) throws IOException
   {
      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
      String classFileName = className.replace('.', '/') + ".class";
      InputStream classFile = classLoader.getResourceAsStream(classFileName);

      return new ClassReader(classFile);
   }

   private final ClassReader reader;

   public ClassFile(Class<?> aClass, boolean fromLastRedefinitionIfAny)
   {
      String className = aClass.getName();
      byte[] classfile =
         Proxy.isProxyClass(aClass) ? TestRun.proxyClasses().getClassfile(className) : null;

      if (classfile == null && fromLastRedefinitionIfAny) {
         classfile = TestRun.mockFixture().getRedefinedClassfile(aClass);
      }

      reader = classfile == null ? createClassFileReader(className) : new ClassReader(classfile);
   }

   public ClassReader getReader()
   {
      return reader;
   }

   public byte[] getBytecode()
   {
      return reader.b;
   }
}
