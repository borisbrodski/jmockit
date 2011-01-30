/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.multicore;

import java.io.*;
import java.net.*;
import java.util.*;

final class CopyingClassLoader extends ClassLoader
{
   private static final ClassLoader SYSTEM_CL = ClassLoader.getSystemClassLoader();
   private static final byte[] CLASSFILE_BUFFER = new byte[2 * 1024 * 1024];

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
      synchronized (CopyingClassLoader.class) {
         if (
            name.startsWith("java.") || name.startsWith("sun.reflect.") ||
            name.startsWith("org.junit.") || name.startsWith("junit.framework.") ||
            name.startsWith("mockit.") && !name.startsWith("mockit.integration.logging.")
         ) {
            Class<?> theClass = SYSTEM_CL.loadClass(name);

            if (resolve) {
               resolveClass(theClass);
            }

            return theClass;
         }

         Class<?> copiedClass = getCopy(name);

         return copiedClass;
      }
   }

   @Override
   protected Class<?> findClass(String name)
   {
      synchronized (CopyingClassLoader.class) {
         Class<?> loadedClass = findLoadedClass(name);

         if (loadedClass != null) {
            return loadedClass;
         }

         definePackageForCopiedClass(name);
         int bytesRead = readClassFile(name);

         Class<?> copiedClass = defineClass(name, CLASSFILE_BUFFER, 0, bytesRead);

         return copiedClass;
      }
   }

   private int readClassFile(String className)
   {
      String classFileName = className.replace('.', '/') + ".class";
      InputStream input = SYSTEM_CL.getResourceAsStream(classFileName);
      int bytesRead = 0;

      while (true) {
         int n;

         try {
            n = input.read(CLASSFILE_BUFFER, bytesRead, CLASSFILE_BUFFER.length - bytesRead);
         }
         catch (IOException e) {
            throw new RuntimeException(e);
         }
         
         if (n == -1) {
            break;
         }

         bytesRead += n;
      }
      
      return bytesRead;
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
      URL resource;

      // Somehow, getResource returns null sometimes, but retrying eventually works.
      do {
         resource = SYSTEM_CL.getResource(name);
      }
      while (resource == null);

      return resource;
   }

   @Override
   protected Enumeration<URL> findResources(String name) throws IOException
   {
      return SYSTEM_CL.getResources(name);
   }
}
