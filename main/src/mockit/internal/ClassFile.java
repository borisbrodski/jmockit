/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal;

import java.io.*;
import java.lang.reflect.*;

import mockit.external.asm.*;
import mockit.internal.state.*;

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

   public static void visitClass(String internalClassName, ClassVisitor visitor)
   {
      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
      InputStream classFile = classLoader.getResourceAsStream(internalClassName + ".class");

      try {
         ClassReader cr = new ClassReader(classFile);
         cr.accept(visitor, true);
      }
      catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   private final ClassReader reader;

   public ClassFile(Class<?> aClass, boolean fromLastRedefinitionIfAny)
   {
      String className = aClass.getName();
      byte[] classfile = Proxy.isProxyClass(aClass) ? TestRun.proxyClasses().getClassfile(className) : null;

      if (classfile == null && fromLastRedefinitionIfAny) {
         classfile = TestRun.mockFixture().getRedefinedClassfile(aClass);
      }

      reader = classfile == null ? createClassFileReader(className) : new ClassReader(classfile);
   }

   public ClassReader getReader() { return reader; }

   public byte[] getBytecode() { return reader.b; }
}
