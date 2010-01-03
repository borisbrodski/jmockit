/*
 * JMockit
 * Copyright (c) 2006-2010 Rogério Liesenfeld
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
package mockit.internal;

import static java.lang.reflect.Modifier.*;

import mockit.external.asm.*;
import mockit.internal.filtering.*;
import mockit.internal.util.*;
import mockit.internal.startup.*;

import static mockit.external.asm.Opcodes.*;

public final class StubOutModifier extends BaseClassModifier
{
   private final SuperConstructorCollector superConstructors = new SuperConstructorCollector(1);
   private final MockingConfiguration stubbingCfg;
   private String superClassName;

   public StubOutModifier(ClassReader cr, MockingConfiguration stubbingConfiguration)
   {
      super(cr);
      stubbingCfg = stubbingConfiguration;
   }

   @Override
   public void visit(
      int version, int access, String name, String signature, String superName, String[] interfaces)
   {
      superClassName = superName;
      super.visit(version, access, name, signature, superName, interfaces);
   }

   @Override
   public MethodVisitor visitMethod(
      int access, String name, String desc, String signature, String[] exceptions)
   {
      if (
         isAbstract(access) || (access & ACC_SYNTHETIC) != 0 ||
         isNative(access) && !Startup.isJava6OrLater() ||
         stubbingCfg != null && !stubbingCfg.matchesFilters(name, desc)
      ) {
         return super.visitMethod(access, name, desc, signature, exceptions);
      }

      startModifiedMethodVersion(access, name, desc, signature, exceptions);

      if ("<init>".equals(name)) {
         generateCallToSuper();
      }

      generateEmptyImplementation(desc);
      return null;
   }

   private void generateCallToSuper()
   {
      mw.visitVarInsn(ALOAD, 0);

      String constructorDesc = superConstructors.findConstructor(superClassName);
      pushDefaultValuesForParameterTypes(constructorDesc);

      mw.visitMethodInsn(INVOKESPECIAL, superClassName, "<init>", constructorDesc);
   }
}
