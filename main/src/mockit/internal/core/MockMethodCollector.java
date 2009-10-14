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
package mockit.internal.core;

import java.lang.reflect.*;

import org.objectweb.asm2.*;
import static org.objectweb.asm2.Opcodes.*;
import mockit.internal.*;

/**
 * Responsible for collecting the signatures of all public methods and constructors (except the
 * default one) defined in a given mock class.
 */
public final class MockMethodCollector extends BaseMockCollector
{
   private static final int INVALID_ACCESSES =
      ACC_BRIDGE + ACC_SYNTHETIC + ACC_ABSTRACT + ACC_NATIVE;

   /**
    * Indicates whether a public default constructor in the mock class is allowed as a mock for the
    * default constructor in the real class.
    */
   private final boolean allowDefaultConstructor;

   public MockMethodCollector(MockMethods mockMethods, boolean allowDefaultConstructor)
   {
      super(mockMethods);
      this.allowDefaultConstructor = allowDefaultConstructor;
   }

   /**
    * Adds the method or constructor specified to the set of mock methods, representing it as
    * <code>name + desc</code>, as long as it's appropriate for such method or constructor to be a
    * mock, as indicated by its access modifiers.
    *
    * @param access access modifiers, indicating "public", "static", and so on; if "public" is not
    * indicated, or one or more of the {@link #INVALID_ACCESSES invalid access modifiers} is
    * indicated, the method or constructor is ignored (not added to the set)
    * @param name the method or constructor name
    * @param desc internal JVM description of parameters and return type
    * @param signature generic signature for a Java 5 generic method, ignored since redefinition
    * only needs to consider the "erased" signature
    * @param exceptions zero or more thrown exceptions in the method "throws" clause, also ignored
    *
    * @return always null, since we are not interested in visiting the code of the method
    */
   @Override
   public MethodVisitor visitMethod(
      int access, String name, String desc, String signature, String[] exceptions)
   {
      super.visitMethod(access, name, desc, signature, exceptions);

      if (!Modifier.isPublic(access) || (access & INVALID_ACCESSES) != 0) {
         // not public or with invalid access, so ignore the method
      }
      else if (allowDefaultConstructor || !isDefaultConstructor(name, desc)) {
         mockMethods.addMethod(name, desc, Modifier.isStatic(access));
      }

      return null;
   }

   private boolean isDefaultConstructor(String name, String desc)
   {
      return "<init>".equals(name) && "()V".equals(desc);
   }
}
