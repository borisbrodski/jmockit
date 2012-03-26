/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.coverage.modification;

import java.io.*;
import java.lang.instrument.*;
import java.security.*;
import java.util.*;

import mockit.coverage.standalone.*;
import mockit.external.asm4.*;

public final class ClassModification
{
   private final Set<String> modifiedClasses;
   private final ClassSelection classSelection;

   public ClassModification()
   {
      modifiedClasses = new HashSet<String>();
      classSelection = new ClassSelection();
      redefineClassesAlreadyLoadedForCoverage();
   }

   private void redefineClassesAlreadyLoadedForCoverage()
   {
      Class<?>[] loadedClasses = Startup.instrumentation().getAllLoadedClasses();

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
      byte[] modifiedClassfile = readAndModifyClassForCoverage(className);

      if (modifiedClassfile != null) {
         redefineClassForCoverage(loadedClass, modifiedClassfile);
         registerClassAsModifiedForCoverage(className, modifiedClassfile);
      }
   }

   private byte[] readAndModifyClassForCoverage(String className)
   {
      try {
         return modifyClassForCoverage(className, null);
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
      ClassDefinition[] classDefs = {new ClassDefinition(loadedClass, modifiedClassfile)};

      try {
         Startup.instrumentation().redefineClasses(classDefs);
      }
      catch (ClassNotFoundException e) {
         throw new RuntimeException(e);
      }
      catch (UnmodifiableClassException e) {
         throw new RuntimeException(e);
      }

      System.out.println("JMockit Coverage: " + loadedClass + " redefined");
   }

   private byte[] modifyClassForCoverage(String className, byte[] classBytecode)
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
      cr.accept(modifier, 0);
      return modifier.toByteArray();
   }

   private boolean isToBeConsideredForCoverage(String className, ProtectionDomain protectionDomain)
   {
      return !modifiedClasses.contains(className) && classSelection.isSelected(className, protectionDomain);
   }

   private void registerClassAsModifiedForCoverage(String className, byte[] modifiedClassfile)
   {
      modifiedClasses.add(className);

      if (!Startup.isStandalone()) {
         //noinspection UnnecessaryFullyQualifiedName
         mockit.internal.state.TestRun.mockFixture().addFixedClass(className, modifiedClassfile);
      }
   }

   public byte[] modifyClass(String className, ProtectionDomain protectionDomain, byte[] originalClassfile)
   {
      boolean modifyClassForCoverage = isToBeConsideredForCoverage(className, protectionDomain);

      if (modifyClassForCoverage) {
         try {
            byte[] modifiedClassfile = modifyClassForCoverage(className, originalClassfile);
            System.out.println("JMockit Coverage: " + className + " transformed");
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

      return null;
   }
}
