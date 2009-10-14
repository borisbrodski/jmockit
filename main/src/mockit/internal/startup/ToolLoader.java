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
package mockit.internal.startup;

import java.lang.instrument.*;

import mockit.internal.*;
import mockit.internal.util.*;
import org.objectweb.asm2.*;
import org.objectweb.asm2.commons.*;

final class ToolLoader implements ClassVisitor
{
   private final String toolClassName;
   private final String toolArgs;
   private boolean annotatedStartupMock;
   private boolean loadClassFileTransformer;

   ToolLoader(String toolClassName, String toolArgs)
   {
      this.toolClassName = toolClassName;
      this.toolArgs = toolArgs;
   }

   public void visit(
      int version, int access, String name, String signature, String superName,
      String[] interfaces)
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

   public AnnotationVisitor visitAnnotation(String desc, boolean visible)
   {
      if ("Lmockit/MockClass;".equals(desc)) {
         annotatedStartupMock = true;
      }

      return new EmptyVisitor();
   }

   public void visitSource(String source, String debug) {}
   public void visitOuterClass(String owner, String name, String desc) {}
   public void visitAttribute(Attribute attr) {}
   public void visitInnerClass(String name, String outerName, String innerName, int access) {}
   public FieldVisitor visitField(
      int access, String name, String desc, String signature, Object value) { return null; }
   public MethodVisitor visitMethod(
      int access, String name, String desc, String signature, String[] exceptions)
   { return null; }

   public void visitEnd()
   {
      if (loadClassFileTransformer) {
         createAndInstallSpecifiedClassFileTransformer();
      }
      else if (annotatedStartupMock && toolArgs == null) {
         setUpStartupMock();
      }
      else if (toolArgs != null && toolArgs.length() > 0) {
         Startup.instrumentation().addTransformer(
            new ExternalMockTransformer(toolClassName, annotatedStartupMock, toolArgs));
      }
   }

   private void setUpStartupMock()
   {
      //noinspection ErrorNotRethrown
      try {
         Class<?> mockClass = Class.forName(toolClassName);
         Object mock = Utilities.newInstance(mockClass);
         new RedefinitionEngine(mock, mockClass, true).setUpStartupMock();
      }
      catch (ClassNotFoundException e) {
         throw new RuntimeException(e);
      }
      catch (NoClassDefFoundError e) {
         // Real class not in the classpath, so we ignore the startup mock.
         System.out.println(e);
      }
   }

   private void createAndInstallSpecifiedClassFileTransformer()
   {
      Class<ClassFileTransformer> transformerClass = Utilities.loadClass(toolClassName);
      ClassFileTransformer transformer =
         Utilities.newInstance(transformerClass, new Class<?>[] {String.class}, toolArgs);

      Startup.instrumentation().addTransformer(transformer);
   }
}
