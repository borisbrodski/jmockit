/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
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
   private final MockingConfiguration stubbingCfg;
   private String superClassName;

   public StubOutModifier(ClassReader cr, MockingConfiguration stubbingConfiguration)
   {
      super(cr);
      stubbingCfg = stubbingConfiguration;
   }

   @Override
   public void visit(int version, int access, String name, String signature, String superName, String[] interfaces)
   {
      superClassName = superName;
      super.visit(version, access, name, signature, superName, interfaces);
   }

   @Override
   public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions)
   {
      if (
         isAbstract(access) || (access & ACC_SYNTHETIC) != 0 || isNative(access) && !Startup.isJava6OrLater() ||
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

      String constructorDesc = SuperConstructorCollector.INSTANCE.findConstructor(superClassName);
      pushDefaultValuesForParameterTypes(constructorDesc);

      mw.visitMethodInsn(INVOKESPECIAL, superClassName, "<init>", constructorDesc);
   }
}
