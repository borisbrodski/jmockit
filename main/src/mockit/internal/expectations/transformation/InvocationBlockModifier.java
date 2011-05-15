/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations.transformation;

import mockit.external.asm.*;

import static mockit.external.asm.Opcodes.*;

final class InvocationBlockModifier extends MethodAdapter
{
   private static final String CLASS_DESC = ActiveInvocations.class.getName().replace('.', '/');

   private final int[] matcherStacks;
   private final MethodWriter mw;
   private final String fieldOwner;
   private final boolean callEndInvocations;
   private int matchers;

   InvocationBlockModifier(MethodWriter mw, String fieldOwner, boolean callEndInvocations)
   {
      super(mw);
      matcherStacks = new int[20];
      this.mw = mw;
      this.fieldOwner = fieldOwner;
      this.callEndInvocations = callEndInvocations;
   }

   @Override
   public void visitFieldInsn(int opcode, String owner, String name, String desc)
   {
      if ((opcode == GETSTATIC || opcode == PUTSTATIC) && isFieldDefinedByInvocationBlock(owner)) {
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
               mw.visitMethodInsn(INVOKESTATIC, CLASS_DESC, "setErrorMessage", "(Ljava/lang/CharSequence;)V");
               return;
            }
         }
         else if (name.startsWith("any")) {
            mw.visitFieldInsn(GETSTATIC, owner, name, desc);
            mw.visitMethodInsn(INVOKESTATIC, CLASS_DESC, "addArgMatcher", "()V");
            matcherStacks[matchers++] = mw.stackSize2;
            return;
         }
      }

      mw.visitFieldInsn(opcode, owner, name, desc);
   }

   private boolean isFieldDefinedByInvocationBlock(String owner)
   {
      return
         fieldOwner.equals(owner) ||
         ("mockit/Expectations mockit/NonStrictExpectations " +
          "mockit/Verifications mockit/VerificationsInOrder " +
          "mockit/FullVerifications mockit/FullVerificationsInOrder").contains(owner);
   }

   @Override
   public void visitMethodInsn(int opcode, String owner, String name, String desc)
   {
      if (opcode == INVOKEVIRTUAL && owner.equals(fieldOwner) && name.startsWith("with")) {
         mw.visitMethodInsn(INVOKEVIRTUAL, owner, name, desc);
         matcherStacks[matchers++] = mw.stackSize2;
         return;
      }

      if (matchers > 0) {
         Type[] argTypes = Type.getArgumentTypes(desc);
         int stackSize = mw.stackSize2;
         int stackAfter = stackSize - sumOfSizes(argTypes);

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
   public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index)
   {
      // In classes instrumented with EMMA some local variable information can be lost, so we discard it entirely to
      // avoid a ClassFormatError.
      if (end.position > 0) {
         super.visitLocalVariable(name, desc, signature, start, end, index);
      }
   }

   @Override
   public void visitInsn(int opcode)
   {
      if (opcode == RETURN && callEndInvocations) {
         mw.visitMethodInsn(INVOKESTATIC, CLASS_DESC, "endInvocations", "()V");
      }

      mw.visitInsn(opcode);
   }
}
