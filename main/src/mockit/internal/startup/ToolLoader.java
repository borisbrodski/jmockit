/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.startup;

import java.lang.instrument.*;

import mockit.external.asm4.*;
import mockit.internal.*;
import mockit.internal.util.*;

final class ToolLoader extends ClassVisitor
{
   private final String toolClassName;
   private final String toolArgs;
   private boolean loadClassFileTransformer;

   ToolLoader(String toolClassName, String toolArgs)
   {
      this.toolClassName = toolClassName;
      this.toolArgs = toolArgs;
   }

   @Override
   public void visit(int version, int access, String name, String signature, String superName, String[] interfaces)
   {
      if (interfaces != null && containsClassFileTransformer(interfaces)) {
         loadClassFileTransformer = true;
      }
   }

   private boolean containsClassFileTransformer(String[] interfaces)
   {
      for (String anInterface : interfaces) {
         if ("java/lang/instrument/ClassFileTransformer".equals(anInterface)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public void visitEnd()
   {
      if (loadClassFileTransformer) {
         createAndInstallSpecifiedClassFileTransformer();
      }
      else {
         setUpStartupMock();
      }
   }

   private void createAndInstallSpecifiedClassFileTransformer()
   {
      Class<ClassFileTransformer> transformerClass = Utilities.loadClass(toolClassName);
      ClassFileTransformer transformer =
         Utilities.newInstance(transformerClass, new Class<?>[] {String.class}, toolArgs);

      Startup.instrumentation().addTransformer(transformer);
   }

   private void setUpStartupMock()
   {
      Class<?> mockClass;

      try {
         mockClass = Class.forName(toolClassName);
      }
      catch (ClassNotFoundException e) {
         throw new RuntimeException(e);
      }

      try {
         new RedefinitionEngine(null, mockClass).setUpStartupMock();
      }
      catch (TypeNotPresentException e) {
         // OK, ignores the startup mock if the necessary third-party class files are not in the classpath.
         System.out.println(e);
      }
   }
}
