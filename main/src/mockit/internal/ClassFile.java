/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal;

import java.io.*;
import java.lang.reflect.*;

import mockit.external.asm4.*;
import mockit.internal.state.*;

public final class ClassFile
{
   public static ClassReader createClassFileReader(Class<?> aClass)
   {
      String className = aClass.getName();
      byte[] fixedClassfile = TestRun.mockFixture().getFixedClassfile(className);

      if (fixedClassfile != null) {
         return new ClassReader(fixedClassfile);
      }

      InputStream classFile = aClass.getResourceAsStream('/' + className.replace('.', '/') + ".class");

      if (classFile == null) {
         throw new RuntimeException("Failed to read class file for " + className);
      }

      try {
         return new ClassReader(classFile);
      }
      catch (IOException e) {
         throw new RuntimeException("Failed to read class file for " + className, e);
      }
   }

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
      InputStream classFile = readClassFromDisk(className.replace('.', '/'));
      return new ClassReader(classFile);
   }

   private static InputStream readClassFromDisk(String internalClassName)
   {
      String classDesc = internalClassName + ".class";
      ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
      InputStream inputStream = contextClassLoader.getResourceAsStream(classDesc);

      if (inputStream == null) {
         ClassLoader thisClassLoader = ClassFile.class.getClassLoader();

         if (thisClassLoader != contextClassLoader) {
            inputStream = thisClassLoader.getResourceAsStream(classDesc);
         }
      }

      if (inputStream == null) {
         throw new RuntimeException("Failed to read class file for " + internalClassName.replace('/', '.'));
      }

      return inputStream;
   }

   public static void visitClass(String internalClassName, ClassVisitor visitor)
   {
      InputStream classFile = readClassFromDisk(internalClassName);

      try {
         ClassReader cr = new ClassReader(classFile);
         cr.accept(visitor, ClassReader.SKIP_DEBUG);
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
