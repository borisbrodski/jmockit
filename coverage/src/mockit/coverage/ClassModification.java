/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.coverage;

import java.io.*;
import java.lang.instrument.*;
import java.security.*;
import java.util.*;

import mockit.external.asm.*;
import mockit.internal.startup.*;
import mockit.internal.state.TestRun;

final class ClassModification
{
   private final Set<String> modifiedClasses;
   private final ClassSelection classSelection;

   ClassModification(String[] args)
   {
      modifiedClasses = new HashSet<String>();
      classSelection = new ClassSelection(args);
   }

   void redefineClassesAlreadyLoadedForCoverage()
   {
      Class<?>[] loadedClasses =
         Startup.instrumentation().getInitiatedClasses(CodeCoverage.class.getClassLoader());

      for (Class<?> loadedClass : loadedClasses) {
         if (
            !loadedClass.isAnnotation() && !loadedClass.isSynthetic() &&
            isToBeConsideredForCoverage(loadedClass.getName(), loadedClass.getProtectionDomain())
         ) {
            redefineClassForCoverage(loadedClass);
         }
      }
   }

   private void redefineClassForCoverage(Class<?> loadedClass)
   {
      String className = loadedClass.getName();
      byte[] modifiedClassfile = readAndModifyClassForCoverage(loadedClass);

      if (modifiedClassfile != null) {
         redefineClassForCoverage(loadedClass, modifiedClassfile);
         registerClassAsModifiedForCoverage(className, modifiedClassfile);
      }
   }

   private byte[] readAndModifyClassForCoverage(Class<?> loadedClass)
   {
      try {
         return modifyClassForCoverage(loadedClass.getName(), null);
      }
      catch (VisitInterruptedException ignore) {
         // Ignore the class if the modification was refused for some reason.
      }
      catch (RuntimeException e) {
         e.printStackTrace();
      }
      catch (AssertionError e) {
         e.printStackTrace();
      }

      return null;
   }

   private void redefineClassForCoverage(Class<?> loadedClass, byte[] modifiedClassfile)
   {
      ClassDefinition[] classDefs = { new ClassDefinition(loadedClass, modifiedClassfile) };

      try {
         Startup.instrumentation().redefineClasses(classDefs);
      }
      catch (ClassNotFoundException e) {
         throw new RuntimeException(e);
      }
      catch (UnmodifiableClassException e) {
         throw new RuntimeException(e);
      }
   }

   byte[] modifyClassForCoverage(String className, byte[] classBytecode)
   {
      byte[] modifiedBytecode = CoverageModifier.recoverModifiedByteCodeIfAvailable(className);

      if (modifiedBytecode != null) {
         return modifiedBytecode;
      }

      ClassReader cr;

      if (classBytecode == null) {
         try {
            cr = new ClassReader(className);
         }
         catch (IOException e) {
            // Ignore the class if the ".class" file can't be located.
            return null;
         }
      }
      else {
         cr = new ClassReader(classBytecode);
      }

      CoverageModifier modifier = new CoverageModifier(cr);
      cr.accept(modifier, false);
      return modifier.toByteArray();
   }

   private boolean isToBeConsideredForCoverage(String className, ProtectionDomain protectionDomain)
   {
      return
         !modifiedClasses.contains(className) &&
         classSelection.isSelected(className, protectionDomain);
   }

   private void registerClassAsModifiedForCoverage(String className, byte[] modifiedClassfile)
   {
      modifiedClasses.add(className);
      TestRun.mockFixture().addFixedClass(className, modifiedClassfile);
   }

   byte[] modifyClass(String className, ProtectionDomain protectionDomain, byte[] originalClassfile)
   {
      boolean modifyClassForCoverage = isToBeConsideredForCoverage(className, protectionDomain);

      if (modifyClassForCoverage) {
         try {
            byte[] modifiedClassfile = modifyClassForCoverage(className, originalClassfile);
            registerClassAsModifiedForCoverage(className, modifiedClassfile);
            return modifiedClassfile;
         }
         catch (VisitInterruptedException ignore) {
            // Ignore the class if the modification was refused for some reason.
         }
         catch (RuntimeException e) {
            e.printStackTrace();
         }
         catch (AssertionError e) {
            e.printStackTrace();
         }
      }

      return originalClassfile;
   }
}
