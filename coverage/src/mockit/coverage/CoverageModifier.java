/*
 * JMockit Coverage
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
package mockit.coverage;

import java.util.*;

import mockit.coverage.paths.*;

import org.objectweb.asm2.*;
import static org.objectweb.asm2.Opcodes.*;

final class CoverageModifier extends ClassWriter
{
   private final CoverageData coverageData;
   private String simpleClassName;
   private String sourceFileName;
   private FileCoverageData fileData;
   private boolean cannotModify;

   CoverageModifier(ClassReader cr)
   {
      super(cr, true);
      coverageData = CoverageData.instance();
   }

   @Override
   public void visit(
      int version, int access, String name, String signature, String superName, String[] interfaces)
   {
      int p = name.lastIndexOf('/');

      if (p < 0) {
         simpleClassName = name;
         sourceFileName = "";
      }
      else {
         simpleClassName = name.substring(p + 1);
         sourceFileName = name.substring(0, p + 1);
      }

      cannotModify = (access & ACC_INTERFACE) != 0 || name.startsWith("mockit/coverage/");

      super.visit(version, access, name, signature, superName, interfaces);
   }

   @Override
   public void visitSource(String file, String debug)
   {
      sourceFileName += file;
      fileData = coverageData.addFile(sourceFileName);

      if (cannotModify) {
         throw CodeCoverage.CLASS_IGNORED;
      }

      super.visitSource(file, debug);
   }

   @Override
   public MethodVisitor visitMethod(
      int access, String name, String desc, String signature, String[] exceptions)
   {
      MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);

      if (name.charAt(0) == '<') {
         if (name.charAt(1) == 'c') {
            return new StaticBlockModifier(mv);
         }
         
         return new ConstructorModifier(mv);
      }

      return new MethodModifier(mv, name);
   }

   private class BaseMethodModifier extends MethodAdapter
   {
      static final String DATA_RECORDING_CLASS = "mockit/coverage/TestRun";

      final MethodWriter mw;
      int currentLine;
      LineCoverageData lineData;
      final List<Label> visitedLabels = new ArrayList<Label>();
      final List<Label> jumpTargetsForCurrentLine = new ArrayList<Label>();
      final Map<Integer, Boolean> pendingBranches = new HashMap<Integer, Boolean>();
      boolean assertFoundInCurrentLine;
      boolean nextLabelAfterConditionalJump;
      boolean potentialAssertFalseFound;

      BaseMethodModifier(MethodVisitor mv)
      {
         super(mv);
         mw = (MethodWriter) mv;
      }

      @Override
      public void visitLineNumber(int line, Label start)
      {
         if (!pendingBranches.isEmpty()) {
            pendingBranches.clear();
         }

         lineData = fileData.addLine(line);
         currentLine = line;

         jumpTargetsForCurrentLine.clear();

         generateCallToRegisterLineExecution();

         mw.visitLineNumber(line, start);
      }

      private void generateCallToRegisterLineExecution()
      {
         mw.visitLdcInsn(sourceFileName);
         pushCurrentLineOnTheStack();
         mw.visitMethodInsn(
            INVOKESTATIC, DATA_RECORDING_CLASS, "lineExecuted", "(Ljava/lang/String;I)V");
      }

      private void pushCurrentLineOnTheStack()
      {
         if (currentLine <= Short.MAX_VALUE) {
            mw.visitIntInsn(SIPUSH, currentLine);
         }
         else {
            mw.visitLdcInsn(currentLine);
         }
      }

      @Override
      public void visitJumpInsn(int opcode, Label label)
      {
         if (visitedLabels.contains(label)) {
            assertFoundInCurrentLine = false;
            mw.visitJumpInsn(opcode, label);
            return;
         }

         jumpTargetsForCurrentLine.add(label);

         nextLabelAfterConditionalJump = isConditionalJump(opcode);

         if (nextLabelAfterConditionalJump) {
            int branchIndex = lineData.addSegment(mw.currentBlock);
            pendingBranches.put(branchIndex, false);

            if (assertFoundInCurrentLine) {
               BranchCoverageData branchData = lineData.getSegmentData(branchIndex);
               branchData.markAsUnreachable();
            }
         }

         mw.visitJumpInsn(opcode, label);

         if (potentialAssertFalseFound && opcode == IFNE) {
            // TODO: what to do here, if anything?
         }

         generateCallToRegisterBranchTargetExecutionIfPending();
      }

      protected final boolean isConditionalJump(int opcode)
      {
         return opcode != GOTO && opcode != JSR;
      }

      private void generateCallToRegisterBranchTargetExecutionIfPending()
      {
         potentialAssertFalseFound = false;

         if (pendingBranches.isEmpty()) {
            return;
         }

         for (Integer branchIndex : pendingBranches.keySet()) {
            BranchCoverageData branchData = lineData.getSegmentData(branchIndex);
            Boolean firstInsnAfterJump = pendingBranches.get(branchIndex);

            if (firstInsnAfterJump) {
               branchData.setHasJumpTarget();
               generateCallToRegisterBranchTargetExecution("jumpTargetExecuted", branchIndex);
            }
            else {
               branchData.setHasNoJumpTarget();
               generateCallToRegisterBranchTargetExecution("noJumpTargetExecuted", branchIndex);
            }
         }

         pendingBranches.clear();
      }

      @Override
      public void visitLabel(Label label)
      {
         visitedLabels.add(label);
         mw.visitLabel(label);

         if (nextLabelAfterConditionalJump) {
            int branchIndex = jumpTargetsForCurrentLine.indexOf(label);

            if (branchIndex >= 0) {
               pendingBranches.put(branchIndex, true);
               assertFoundInCurrentLine = false;
            }

            nextLabelAfterConditionalJump = false;
         }
      }

      private void generateCallToRegisterBranchTargetExecution(String methodName, int branchIndex)
      {
         mw.visitLdcInsn(sourceFileName);
         pushCurrentLineOnTheStack();
         mw.visitIntInsn(SIPUSH, branchIndex);
         mw.visitMethodInsn(
            INVOKESTATIC, DATA_RECORDING_CLASS, methodName, "(Ljava/lang/String;II)V");
      }

      @Override
      public void visitInsn(int opcode)
      {
         generateCallToRegisterBranchTargetExecutionIfPending();
         mw.visitInsn(opcode);
      }

      @Override
      public void visitIntInsn(int opcode, int operand)
      {
         generateCallToRegisterBranchTargetExecutionIfPending();
         mw.visitIntInsn(opcode, operand);
      }

      @Override
      public void visitVarInsn(int opcode, int var)
      {
         generateCallToRegisterBranchTargetExecutionIfPending();
         mw.visitVarInsn(opcode, var);

         if (opcode == RET) {
            System.out.println("RET instruction found!");
         }
      }

      @Override
      public void visitTypeInsn(int opcode, String desc)
      {
         generateCallToRegisterBranchTargetExecutionIfPending();
         mw.visitTypeInsn(opcode, desc);
      }

      @Override
      public void visitFieldInsn(int opcode, String owner, String name, String desc)
      {
         generateCallToRegisterBranchTargetExecutionIfPending();
         mw.visitFieldInsn(opcode, owner, name, desc);

         assertFoundInCurrentLine = opcode == GETSTATIC && "$assertionsDisabled".equals(name);
         potentialAssertFalseFound = true;
      }

      @Override
      public void visitMethodInsn(int opcode, String owner, String name, String desc)
      {
         generateCallToRegisterBranchTargetExecutionIfPending();
         mw.visitMethodInsn(opcode, owner, name, desc);
      }

      @Override
      public void visitLdcInsn(Object cst)
      {
         generateCallToRegisterBranchTargetExecutionIfPending();
         mw.visitLdcInsn(cst);
      }

      @Override
      public void visitIincInsn(int var, int increment)
      {
         generateCallToRegisterBranchTargetExecutionIfPending();
         mw.visitIincInsn(var, increment);
      }

      @Override
      public void visitTryCatchBlock(Label start, Label end, Label handler, String type)
      {
         generateCallToRegisterBranchTargetExecutionIfPending();
         mw.visitTryCatchBlock(start, end, handler, type);
      }

      @Override
      public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels)
      {
         generateCallToRegisterBranchTargetExecutionIfPending();
         mw.visitLookupSwitchInsn(dflt, keys, labels);
      }

      @Override
      public void visitTableSwitchInsn(int min, int max, Label dflt, Label[] labels)
      {
         generateCallToRegisterBranchTargetExecutionIfPending();
         mw.visitTableSwitchInsn(min, max, dflt, labels);
      }

      @Override
      public void visitMultiANewArrayInsn(String desc, int dims)
      {
         generateCallToRegisterBranchTargetExecutionIfPending();
         mw.visitMultiANewArrayInsn(desc, dims);
      }
   }

   private class MethodOrConstructorModifier extends BaseMethodModifier
   {
      final MethodCoverageData methodData;
      final NodeBuilder nodeBuilder;

      MethodOrConstructorModifier(MethodVisitor mv, String methodOrConstructorName)
      {
         super(mv);
         methodData = new MethodCoverageData(methodOrConstructorName);
         nodeBuilder = new NodeBuilder();
      }

      @Override
      public final void visitLineNumber(int line, Label start)
      {
         super.visitLineNumber(line, start);

         int newNodeIndex = nodeBuilder.handlePotentialNewBlock(line);
         generateCallToRegisterNodeReached(newNodeIndex);
      }

      private void generateCallToRegisterNodeReached(int nodeIndex)
      {
         if (nodeIndex >= 0) {
            mw.visitLdcInsn(sourceFileName);
            mw.visitLdcInsn(nodeBuilder.firstLine);
            mw.visitIntInsn(SIPUSH, nodeIndex);
            mw.visitMethodInsn(
               INVOKESTATIC, DATA_RECORDING_CLASS, "nodeReached", "(Ljava/lang/String;II)V");
         }
      }

      @Override
      public final void visitLabel(Label label)
      {
         super.visitLabel(label);

         int line = label.line;
         int newNodeIndex = nodeBuilder.handleJumpTarget(label, line > 0 ? line : currentLine);
         generateCallToRegisterNodeReached(newNodeIndex);
      }

      @Override
      public final void visitJumpInsn(int opcode, Label label)
      {
         if (visitedLabels.contains(label)) {
            super.visitJumpInsn(opcode, label);
            return;
         }

         boolean conditional = isConditionalJump(opcode);
         int nodeIndex = nodeBuilder.handleJump(label, currentLine, conditional);
         generateCallToRegisterNodeReached(nodeIndex);

         super.visitJumpInsn(opcode, label);
      }

      @Override
      public final void visitInsn(int opcode)
      {
         if (opcode >= IRETURN && opcode <= RETURN || opcode == ATHROW) {
            handleRegularInstruction();
            int newNodeIndex = nodeBuilder.handleExit(currentLine);
            generateCallToRegisterNodeReached(newNodeIndex);
         }
         else {
            handleRegularInstruction();
         }

         super.visitInsn(opcode);
      }

      private void handleRegularInstruction()
      {
         int nodeIndex = nodeBuilder.handleRegularInstruction(currentLine);
         generateCallToRegisterNodeReached(nodeIndex);
      }

      @Override
      public final void visitIntInsn(int opcode, int operand)
      {
         super.visitIntInsn(opcode, operand);
         handleRegularInstruction();
      }

      @Override
      public final void visitIincInsn(int var, int increment)
      {
         super.visitIincInsn(var, increment);
         handleRegularInstruction();
      }

      @Override
      public final void visitLdcInsn(Object cst)
      {
         super.visitLdcInsn(cst);
         handleRegularInstruction();
      }

      @Override
      public final void visitTypeInsn(int opcode, String desc)
      {
         super.visitTypeInsn(opcode, desc);
         handleRegularInstruction();
      }

      @Override
      public final void visitVarInsn(int opcode, int var)
      {
         super.visitVarInsn(opcode, var);
         handleRegularInstruction();
      }

      @Override
      public final void visitFieldInsn(int opcode, String owner, String name, String desc)
      {
         super.visitFieldInsn(opcode, owner, name, desc);
         handleRegularInstruction();
      }

      @Override
      public final void visitMethodInsn(int opcode, String owner, String name, String desc)
      {
         super.visitMethodInsn(opcode, owner, name, desc);
         handleRegularInstruction();
      }

      @Override
      public final void visitTryCatchBlock(Label start, Label end, Label handler, String type)
      {
         super.visitTryCatchBlock(start, end, handler, type);
         handleRegularInstruction();
      }

      @Override
      public final void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels)
      {
         super.visitLookupSwitchInsn(dflt, keys, labels);

         handleRegularInstruction();
         nodeBuilder.handleForwardJumpsToNewTargets(dflt, labels);
      }

      @Override
      public final void visitTableSwitchInsn(int min, int max, Label dflt, Label[] labels)
      {
         super.visitTableSwitchInsn(min, max, dflt, labels);

         handleRegularInstruction();
         nodeBuilder.handleForwardJumpsToNewTargets(dflt, labels);
      }

      @Override
      public final void visitMultiANewArrayInsn(String desc, int dims)
      {
         super.visitMultiANewArrayInsn(desc, dims);
         handleRegularInstruction();
      }

      @Override
      public final void visitEnd()
      {
         if (currentLine > 0) {
            methodData.buildPaths(currentLine, nodeBuilder);
            fileData.addMethod(methodData);
         }
      }
   }

   private final class MethodModifier extends MethodOrConstructorModifier
   {
      MethodModifier(MethodVisitor mv, String methodName) { super(mv, methodName); }

      @Override
      public AnnotationVisitor visitAnnotation(String desc, boolean visible)
      {
         boolean isTestMethod = desc.startsWith("Lorg/junit/") || desc.startsWith("Lorg/testng/");

         if (isTestMethod) {
            throw CodeCoverage.CLASS_IGNORED;
         }

         return mw.visitAnnotation(desc, visible);
      }
   }

   private final class ConstructorModifier extends MethodOrConstructorModifier
   {
      ConstructorModifier(MethodVisitor mv) { super(mv, simpleClassName); }
   }

   private final class StaticBlockModifier extends BaseMethodModifier
   {
      StaticBlockModifier(MethodVisitor mv) { super(mv); }

      @Override
      public void visitMethodInsn(int opcode, String owner, String name, String desc)
      {
         // This is to ignore bytecode belonging to a static initialization block inserted in a
         // regular line of code by the Java compiler when the class contains at least one "assert"
         // statement. Otherwise, that line of code would always appear as partially covered when
         // running with assertions enabled.
         assertFoundInCurrentLine =
            opcode == INVOKEVIRTUAL &&
            "java/lang/Class".equals(owner) && "desiredAssertionStatus".equals(name);

         super.visitMethodInsn(opcode, owner, name, desc);
      }
   }
}
