/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations.transformation;

import java.util.*;

import mockit.external.asm4.*;

import static mockit.external.asm4.Opcodes.*;
import static mockit.internal.util.TypeConversion.*;

final class InvocationBlockModifier extends MethodVisitor
{
   private static final String CLASS_DESC = Type.getInternalName(ActiveInvocations.class);

   // Input data:
   private final String owner;
   private final boolean callEndInvocations;

   // Helper fields that allow argument matchers to be moved to the correct positions of their
   // corresponding parameters:
   private final int[] matcherStacks;
   private int matchersToMove;
   private Type[] argTypes;

   // Helper field used to prevent NPEs from calls to certain "with" methods, when the associated
   // parameter is of a primitive type:
   private boolean nullOnTopOfStack;

   // Helper fields used to deal with "withCapture()" matchers:
   private int matchersFound;
   private List<Capture> captures;
   private boolean parameterForCapture;

   private abstract class Capture
   {
      final int opcode;
      private int parameterIndex;
      private boolean parameterIndexFixed;

      Capture(int opcode)
      {
         this.opcode = opcode;
         parameterIndex = matchersFound - 1;
      }

      /**
       * Responsible for performing the following steps:
       * 1. Get the argument value (an Object) for the last matched invocation.
       * 2. Typecheck and unbox the Object value to a primitive type, as needed.
       * 3. Store the converted value in its local variable or field.
       */
      abstract void generateCodeToStoreCapturedValue();

      // Performs step 1 above.
      final void generateCallToObtainMatchedArgumentValue()
      {
         mv.visitIntInsn(SIPUSH, parameterIndex);
         mv.visitMethodInsn(INVOKESTATIC, CLASS_DESC, "matchedArgument", "(I)Ljava/lang/Object;");
      }

      final Type getParameterType() { return argTypes[parameterIndex]; }

      final boolean fixParameterIndex(int originalIndex, int newIndex)
      {
         if (!parameterIndexFixed && parameterIndex == originalIndex) {
            parameterIndex = newIndex;
            parameterIndexFixed = true;
            return true;
         }

         return false;
      }
   }

   private final class CaptureIntoVariable extends Capture
   {
      private final int var;

      private CaptureIntoVariable(int opcode, int var)
      {
         super(opcode);
         this.var = var;
      }

      @Override
      void generateCodeToStoreCapturedValue()
      {
         generateCallToObtainMatchedArgumentValue();
         generateUnboxing(mv, getParameterType(), opcode);
         mv.visitVarInsn(opcode, var);
      }
   }

   private final class CaptureIntoField extends Capture
   {
      private final String fieldOwner;
      private final String fieldName;
      private final String fieldDesc;

      CaptureIntoField(int opcode, String fieldOwner, String fieldName, String fieldDesc)
      {
         super(opcode);
         this.fieldOwner = fieldOwner;
         this.fieldName = fieldName;
         this.fieldDesc = fieldDesc;
      }

      @Override
      void generateCodeToStoreCapturedValue()
      {
         // 0. Pushes the instance field owner to the stack, if that's the case:
         if (opcode == PUTFIELD) {
            mv.visitVarInsn(ALOAD, 0);

            if (!owner.equals(fieldOwner)) {
               mv.visitFieldInsn(GETFIELD, owner, "this$0", 'L' + fieldOwner + ';');
            }
         }

         generateCallToObtainMatchedArgumentValue();
         generateUnboxing(mv, getParameterType(), fieldDesc);
         mv.visitFieldInsn(opcode, fieldOwner, fieldName, fieldDesc);
      }
   }

   private void addCapture(Capture capture)
   {
      if (captures == null) {
         captures = new ArrayList<Capture>();
      }

      captures.add(capture);
   }

   InvocationBlockModifier(MethodVisitor mw, String owner, boolean callEndInvocations)
   {
      super(mw);
      this.owner = owner;
      this.callEndInvocations = callEndInvocations;
      matcherStacks = new int[20];
   }

   @Override
   public void visitFieldInsn(int opcode, String owner, String name, String desc)
   {
      if ((opcode == PUTFIELD || opcode == PUTSTATIC) && parameterForCapture) {
         addCapture(new CaptureIntoField(opcode, owner, name, desc));
         parameterForCapture = false;
      }
      else if ((opcode == GETSTATIC || opcode == PUTSTATIC) && isFieldDefinedByInvocationBlock(owner)) {
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
            matcherStacks[matchersToMove++] = mv.stackSize2;
            matchersFound++;
            return;
         }
      }

      mv.visitFieldInsn(opcode, owner, name, desc);
   }

   private boolean isFieldDefinedByInvocationBlock(String owner)
   {
      return
         this.owner.equals(owner) ||
         ("mockit/Expectations mockit/NonStrictExpectations " +
          "mockit/Verifications mockit/VerificationsInOrder " +
          "mockit/FullVerifications mockit/FullVerificationsInOrder").contains(owner);
   }

   @Override
   public void visitMethodInsn(int opcode, String owner, String name, String desc)
   {
      if (opcode == INVOKESTATIC && (isBoxing(owner, name, desc) || isAccessMethod(owner, name))) {
         // It's an invocation to a primitive boxing method or to a synthetic method for private access, just ignore it.
         mv.visitMethodInsn(INVOKESTATIC, owner, name, desc);
         return;
      }
      else if (opcode == INVOKEVIRTUAL && owner.equals(this.owner) && name.startsWith("with")) {
         mv.visitMethodInsn(INVOKEVIRTUAL, owner, name, desc);
         matcherStacks[matchersToMove++] = mv.stackSize2;
         nullOnTopOfStack = createPendingCaptureIfNeeded(name, desc);
         matchersFound++;
         return;
      }
      else if (isUnboxing(opcode, owner, desc)) {
         if (nullOnTopOfStack) {
            generateCodeToReplaceNullWithZeroOnTopOfStack(desc.charAt(2));
            nullOnTopOfStack = false;
         }
         else {
            mv.visitMethodInsn(opcode, owner, name, desc);
         }

         return;
      }
      else if (matchersToMove > 0) {
         argTypes = Type.getArgumentTypes(desc);
         int stackSize = mv.stackSize2;
         int stackAfter = stackSize - sumOfParameterSizes();

         if (stackAfter < matcherStacks[0]) {
            generateCallsToMoveArgMatchers(stackAfter);
            matchersToMove = 0;
         }
      }

      mv.visitMethodInsn(opcode, owner, name, desc);
      generateCallsToCaptureMatchedArgumentsIfPending();
      nullOnTopOfStack = false;
   }

   private boolean isAccessMethod(String owner, String name)
   {
      return !owner.equals(this.owner) && name.startsWith("access$");
   }

   private boolean createPendingCaptureIfNeeded(String name, String desc)
   {
      boolean withCapture = "withCapture".equals(name);
      parameterForCapture = withCapture && !desc.contains("List");
      return withCapture;
   }

   private void generateCodeToReplaceNullWithZeroOnTopOfStack(char primitiveTypeCode)
   {
      mv.visitInsn(POP);

      int zeroOpcode;
      switch (primitiveTypeCode) {
         case 'J': zeroOpcode = LCONST_0; break;
         case 'F': zeroOpcode = FCONST_0; break;
         case 'D': zeroOpcode = DCONST_0; break;
         default: zeroOpcode = ICONST_0;
      }
      mv.visitInsn(zeroOpcode);
   }

   private int sumOfParameterSizes()
   {
      int sum = 0;

      for (Type argType : argTypes) {
         sum += argType.getSize();
      }

      return sum;
   }

   private void generateCallsToMoveArgMatchers(int initialStack)
   {
      int stack = initialStack;
      int nextMatcher = 0;
      int matcherStack = matcherStacks[0];

      for (int i = 0; i < argTypes.length && nextMatcher < matchersToMove; i++) {
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
         for (int i = captures.size() - 1; i >= 0; i--) {
            Capture capture = captures.get(i);

            if (capture.fixParameterIndex(originalIndex, newIndex)) {
               break;
            }
         }
      }
   }

   private void generateCallsToCaptureMatchedArgumentsIfPending()
   {
      if (matchersToMove == 0) {
         if (captures != null) {
            for (Capture capture : captures) {
               capture.generateCodeToStoreCapturedValue();
            }

            captures = null;
         }

         matchersFound = 0;
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
      if (opcode >= ISTORE && opcode <= ASTORE && parameterForCapture) {
         addCapture(new CaptureIntoVariable(opcode, var));
         parameterForCapture = false;
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
