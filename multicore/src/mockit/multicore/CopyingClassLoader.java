/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.multicore;

import java.io.*;
import java.net.*;
import java.util.*;

import mockit.internal.*;

final class CopyingClassLoader extends ClassLoader
{
   private static final ClassLoader SYSTEM_CL = ClassLoader.getSystemClassLoader();

   CopyingClassLoader()
   {
      super(SYSTEM_CL.getParent());
   }

   Class<?> getCopy(String className)
   {
      return findClass(className);
   }

   @Override
   protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException
   {
      if (
         name.startsWith("org.junit.") || name.startsWith("junit.framework.") ||
         name.startsWith("mockit.") && !name.startsWith("mockit.integration.logging.")
      ) {
         assert !resolve;
         return SYSTEM_CL.loadClass(name);
      }

      return super.loadClass(name, resolve);
   }

   @Override
   protected Class<?> findClass(String name)
   {
      Class<?> loadedClass = findLoadedClass(name);

      if (loadedClass != null) {
         return loadedClass;
      }

      definePackageForCopiedClass(name);
      
      byte[] classBytecode = ClassFile.createClassFileReader(name).b;
      return defineClass(name, classBytecode, 0, classBytecode.length);
   }

   private void definePackageForCopiedClass(String name)
   {
      int p = name.lastIndexOf('.');

      if (p > 0) {
         String packageName = name.substring(0, p);

         if (getPackage(packageName) == null) {
            definePackage(packageName, null, null, null, null, null, null, null);
         }
      }
   }

   boolean hasLoadedClass(Class<?> aClass)
   {
      return findLoadedClass(aClass.getName()) != null;
   }

   @Override
   protected URL findResource(String name)
   {
      return SYSTEM_CL.getResource(name);
   }

   @Override
   protected Enumeration<URL> findResources(String name) throws IOException
   {
      return SYSTEM_CL.getResources(name);
   }
}
