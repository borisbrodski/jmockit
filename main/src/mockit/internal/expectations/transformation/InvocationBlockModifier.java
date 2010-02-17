/*
 * JMockit Expectations & Verifications
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
package mockit.internal.expectations.transformation;

import mockit.external.asm.*;

import static mockit.external.asm.Opcodes.*;

final class InvocationBlockModifier extends MethodAdapter
{
   private static final String CLASS_DESC = ActiveInvocations.class.getName().replace('.', '/');

   private final int[] matcherStacks = new int[20];
   private final MethodWriter mw;
   private final String fieldOwner;
   private int matchers;

   InvocationBlockModifier(MethodWriter mw, String fieldOwner)
   {
      super(mw);
      this.mw = mw;
      this.fieldOwner = fieldOwner;
   }

   @Override
   public void visitFieldInsn(int opcode, String owner, String name, String desc)
   {
      if ((opcode == GETSTATIC || opcode == PUTSTATIC) && fieldOwner.equals(owner)) {
         if (opcode == PUTSTATIC) {
            if ("result".equals(name)) {
               mw.visitMethodInsn(INVOKESTATIC, CLASS_DESC, "addResult", "(Ljava/lang/Object;)V");
               return;
            }
            else if ("forEachInvocation".equals(name)) {
               mw.visitMethodInsn(INVOKESTATIC, CLASS_DESC, "setHandler", "(Ljava/lang/Object;)V");
               return;
            }
            else if ("times".equals(name) || "minTimes".equals(name) || "maxTimes".equals(name)) {
               mw.visitMethodInsn(INVOKESTATIC, CLASS_DESC, name, "(I)V");
               return;
            }
            else if ("$".equals(name)) {
               mw.visitMethodInsn(
                  INVOKESTATIC, CLASS_DESC, "setErrorMessage", "(Ljava/lang/CharSequence;)V");
               return;
            }
         }
         else if (name.startsWith("any")) {
            mw.visitFieldInsn(GETSTATIC, owner, name, desc);
            mw.visitMethodInsn(INVOKESTATIC, CLASS_DESC, "addArgMatcher", "()V");
            matcherStacks[matchers++] = mw.stackSize;
            return;
         }
      }

      mw.visitFieldInsn(opcode, owner, name, desc);
   }

   @Override
   public void visitMethodInsn(int opcode, String owner, String name, String desc)
   {
      if (opcode == INVOKEVIRTUAL && owner.equals(fieldOwner) && name.startsWith("with")) {
         mw.visitMethodInsn(INVOKEVIRTUAL, owner, name, desc);
         matcherStacks[matchers++] = mw.stackSize;
         return;
      }

      if (matchers > 0) {
         Type[] argTypes = Type.getArgumentTypes(desc);
         int stackAfter = mw.stackSize - sumOfSizes(argTypes);

         if (stackAfter < matcherStacks[0]) {
            generateCallsToMoveArgMatchers(argTypes, stackAfter);
            matchers = 0;
         }
      }

      mw.visitMethodInsn(opcode, owner, name, desc);
   }

   private int sumOfSizes(Type[] argTypes)
   {
      int sum = 0;

      for (Type argType : argTypes) {
         sum += argType.getSize();
      }

      return sum;
   }

   private void generateCallsToMoveArgMatchers(Type[] argTypes, int initialStack)
   {
      int stack = initialStack;
      int nextMatcher = 0;
      int matcherStack = matcherStacks[0];

      for (int i = 0; i < argTypes.length && nextMatcher < matchers; i++) {
         stack += argTypes[i].getSize();

         if (stack == matcherStack || stack == matcherStack + 1) {
            if (nextMatcher < i) {
               generateCallToMoveArgMatcher(nextMatcher, i);
            }

            matcherStack = matcherStacks[++nextMatcher];
         }
      }
   }

   private void generateCallToMoveArgMatcher(int originalMatcherIndex, int toIndex)
   {
      mw.visitIntInsn(SIPUSH, originalMatcherIndex);
      mw.visitIntInsn(SIPUSH, toIndex);
      mw.visitMethodInsn(INVOKESTATIC, CLASS_DESC, "moveArgMatcher", "(II)V");
   }

   @Override
   public void visitInsn(int opcode)
   {
      if (opcode == RETURN) {
         mw.visitMethodInsn(INVOKESTATIC, CLASS_DESC, "endInvocations", "()V");
      }

      mw.visitInsn(opcode);
   }
}
