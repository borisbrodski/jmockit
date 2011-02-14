/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
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
