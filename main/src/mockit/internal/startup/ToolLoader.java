/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.startup;

import java.lang.instrument.*;

import mockit.external.asm.*;
import mockit.external.asm.commons.*;
import mockit.internal.*;
import mockit.internal.util.*;

final class ToolLoader implements ClassVisitor
{
   private final String toolClassName;
   private final String toolArgs;
   private boolean loadClassFileTransformer;

   ToolLoader(String toolClassName, String toolArgs)
   {
      this.toolClassName = toolClassName;
      this.toolArgs = toolArgs;
   }

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

   public AnnotationVisitor visitAnnotation(String desc, boolean visible) { return new EmptyVisitor(); }
   public void visitSource(String source, String debug) {}
   public void visitOuterClass(String owner, String name, String desc) {}
   public void visitAttribute(Attribute attr) {}
   public void visitInnerClass(String name, String outerName, String innerName, int access) {}
   public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) { return null; }
   public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions)
   { return null; }

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
