/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations.transformation;

import java.util.*;

import mockit.external.asm4.*;
import mockit.internal.util.*;

import static mockit.external.asm4.Opcodes.*;

final class InvocationBlockModifier extends MethodVisitor
{
   private static final String CLASS_DESC = Type.getInternalName(ActiveInvocations.class);

   private final int[] matcherStacks;
   private final String fieldOwner;
   private final boolean callEndInvocations;
   private int matchers;

   private static final class Capture
   {
      int opcode;
      int var;
      int parameter;
   }

   private List<Capture> captures;
   private Capture pendingCapture;

   InvocationBlockModifier(MethodVisitor mw, String fieldOwner, boolean callEndInvocations)
   {
      super(mw);
      matcherStacks = new int[20];
      this.fieldOwner = fieldOwner;
      this.callEndInvocations = callEndInvocations;
   }

   @Override
   public void visitFieldInsn(int opcode, String owner, String name, String desc)
   {
      if ((opcode == GETSTATIC || opcode == PUTSTATIC) && isFieldDefinedByInvocationBlock(owner)) {
         if (opcode == PUTSTATIC) {
            if ("result".equals(name)) {
               mv.visitMethodInsn(INVOKESTATIC, CLASS_DESC, "addResult", "(Ljava/lang/Object;)V");
               return;
            }
            else if ("forEachInvocation".equals(name)) {
               mv.visitMethodInsn(INVOKESTATIC, CLASS_DESC, "setHandler", "(Ljava/lang/Object;)V");
               return;
            }
            else if ("times".equals(name) || "minTimes".equals(name) || "maxTimes".equals(name)) {
               mv.visitMethodInsn(INVOKESTATIC, CLASS_DESC, name, "(I)V");
               return;
            }
            else if ("$".equals(name)) {
               mv.visitMethodInsn(INVOKESTATIC, CLASS_DESC, "setErrorMessage", "(Ljava/lang/CharSequence;)V");
               return;
            }
         }
         else if (name.startsWith("any")) {
            mv.visitFieldInsn(GETSTATIC, owner, name, desc);
            mv.visitMethodInsn(INVOKESTATIC, CLASS_DESC, "addArgMatcher", "()V");
            matcherStacks[matchers++] = mv.stackSize2;
            return;
         }
      }

      mv.visitFieldInsn(opcode, owner, name, desc);
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
      if (opcode == INVOKESTATIC && !owner.equals(fieldOwner) && name.startsWith("access$")) {
         // It's a synthetic method for private access, just ignore it.
         mv.visitMethodInsn(opcode, owner, name, desc);
         return;
      }
      else if (opcode == INVOKEVIRTUAL && owner.equals(fieldOwner) && name.startsWith("with")) {
         mv.visitMethodInsn(INVOKEVIRTUAL, owner, name, desc);
         matcherStacks[matchers++] = mv.stackSize2;

         if ("withCapture".equals(name) && !desc.contains("List")) {
            pendingCapture = new Capture();
         }

         return;
      }
      else if (matchers > 0) {
         Type[] argTypes = Type.getArgumentTypes(desc);
         int stackSize = mv.stackSize2;
         int stackAfter = stackSize - sumOfSizes(argTypes);

         if (stackAfter < matcherStacks[0]) {
            generateCallsToMoveArgMatchers(argTypes, stackAfter);
            matchers = 0;
         }
      }

      mv.visitMethodInsn(opcode, owner, name, desc);

      if (matchers == 0 && captures != null) {
         generateCallsToCaptureMatchedArguments(desc);
         captures = null;
      }
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
               updateCaptureIfAny(nextMatcher, i);
            }

            matcherStack = matcherStacks[++nextMatcher];
         }
      }
   }

   private void generateCallToMoveArgMatcher(int originalMatcherIndex, int toIndex)
   {
      mv.visitIntInsn(SIPUSH, originalMatcherIndex);
      mv.visitIntInsn(SIPUSH, toIndex);
      mv.visitMethodInsn(INVOKESTATIC, CLASS_DESC, "moveArgMatcher", "(II)V");
   }

   private void updateCaptureIfAny(int originalIndex, int newIndex)
   {
      if (captures != null) {
         Capture capture = captures.get(originalIndex);
         capture.parameter = newIndex;
      }
   }

   private void generateCallsToCaptureMatchedArguments(String expectationDesc)
   {
      Type[] argTypes = Type.getArgumentTypes(expectationDesc);

      for (Capture capture : captures) {
         mv.visitIntInsn(SIPUSH, capture.parameter);
         mv.visitMethodInsn(INVOKESTATIC, CLASS_DESC, "matchedArgument", "(I)Ljava/lang/Object;");

         Type argType = argTypes[capture.parameter];
         TypeConversion.generateCastFromObject(mv, argType);

         mv.visitVarInsn(capture.opcode, capture.var);
      }
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
   public void visitVarInsn(int opcode, int var)
   {
      if (pendingCapture != null && opcode >= ISTORE && opcode <= ASTORE) {
         if (captures == null) captures = new ArrayList<Capture>();
         pendingCapture.opcode = opcode;
         pendingCapture.var = var;
         pendingCapture.parameter = captures.size();
         captures.add(pendingCapture);
         pendingCapture = null;
      }

      mv.visitVarInsn(opcode, var);
   }

   @Override
   public void visitInsn(int opcode)
   {
      if (opcode == RETURN && callEndInvocations) {
         mv.visitMethodInsn(INVOKESTATIC, CLASS_DESC, "endInvocations", "()V");
      }

      mv.visitInsn(opcode);
   }
}
