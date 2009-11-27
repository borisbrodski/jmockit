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
   private String methodName;
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

      cannotModify = name.startsWith("mockit/coverage/");

      super.visit(version, access, name, signature, superName, interfaces);
   }

   @Override
   public void visitSource(String file, String debug)
   {
      sourceFileName += file;
      fileData = coverageData.addFile(sourceFileName);

//      System.out.println("Modifying: " + sourceFileName);

      super.visitSource(file, debug);
   }

   @Override
   public MethodVisitor visitMethod(
      int access, String name, String desc, String signature, String[] exceptions)
   {
      MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);

      if (cannotModify) {
         return mv;
      }

      methodName = name;

      return "<clinit>".equals(name) ? new StaticBlockModifier(mv) : new MethodModifier(mv);
   }

   private class BaseMethodModifier extends MethodAdapter
   {
      static final String DATA_RECORDING_CLASS = "mockit/coverage/TestRun";

      final MethodWriter mw;
      int currentLine;
      LineCoverageData lineData;
      final List<Label> startLabelsForVisitedLines = new ArrayList<Label>();
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

         startLabelsForVisitedLines.add(start);
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
         if (startLabelsForVisitedLines.contains(label)) {
            assertFoundInCurrentLine = false;
            mw.visitJumpInsn(opcode, label);
            return;
         }

         jumpTargetsForCurrentLine.add(label);

         if (opcode != GOTO && opcode != JSR) {
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

         nextLabelAfterConditionalJump = opcode != GOTO && opcode != JSR;
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
         mw.visitLabel(label);

         if (nextLabelAfterConditionalJump) {
            int branchIndex = jumpTargetsForCurrentLine.indexOf(label);

            if (branchIndex >= 0) {
               pendingBranches.put(branchIndex, true);
               assertFoundInCurrentLine = false;
            }
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
      public void visitTableSwitchInsn(int min, int max, Label dflt, Label[] labels)
      {
         generateCallToRegisterBranchTargetExecutionIfPending();
         mw.visitTableSwitchInsn(min, max, dflt, labels);
      }

      @Override
      public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels)
      {
         generateCallToRegisterBranchTargetExecutionIfPending();
         mw.visitLookupSwitchInsn(dflt, keys, labels);
      }

      @Override
      public void visitMultiANewArrayInsn(String desc, int dims)
      {
         generateCallToRegisterBranchTargetExecutionIfPending();
         mw.visitMultiANewArrayInsn(desc, dims);
      }
   }

   private final class MethodModifier extends BaseMethodModifier
   {
      final MethodCoverageData methodData;

      MethodModifier(MethodVisitor mv)
      {
         super(mv);

         if (methodName.charAt(0) == '<') {
            methodName = simpleClassName;
         }

         methodData = new MethodCoverageData(methodName);
      }

      @Override
      public AnnotationVisitor visitAnnotation(String desc, boolean visible)
      {
         boolean isTestMethod = desc.startsWith("Lorg/junit/") || desc.startsWith("Lorg/testng/");

         if (isTestMethod) {
            throw CodeCoverage.CLASS_IGNORED;
         }

         return mw.visitAnnotation(desc, visible);
      }

      @Override
      public void visitLineNumber(int line, Label start)
      {
         super.visitLineNumber(line, start);

         int newNodeIndex = methodData.handlePotentialNewBlock(line);
         generateCallToRegisterNodeReached(newNodeIndex);
      }

      private void generateCallToRegisterNodeReached(int nodeIndex)
      {
         if (nodeIndex >= 0) {
            mw.visitLdcInsn(sourceFileName);
            mw.visitLdcInsn(methodData.getFirstLineOfImplementationBody());
            mw.visitIntInsn(SIPUSH, nodeIndex);
            mw.visitMethodInsn(
               INVOKESTATIC, DATA_RECORDING_CLASS, "nodeReached", "(Ljava/lang/String;II)V");
         }
      }

      @Override
      public void visitLabel(Label label)
      {
         super.visitLabel(label);
         int newNodeIndex = methodData.handleJumpTarget(label, label.line);
         generateCallToRegisterNodeReached(newNodeIndex);
      }

      @Override
      public void visitJumpInsn(int opcode, Label label)
      {
         super.visitJumpInsn(opcode, label);
         methodData.handleJump(label, opcode != GOTO && opcode != JSR);
      }

      @Override
      public void visitInsn(int opcode)
      {
         handleRegularInstruction();

         if (opcode >= IRETURN && opcode <= RETURN || opcode == ATHROW) {
            int newNodeIndex = methodData.handleExit(currentLine);
            generateCallToRegisterNodeReached(newNodeIndex);
         }

         super.visitInsn(opcode);
      }

      private void handleRegularInstruction()
      {
         int nodeIndex = methodData.handleRegularInstruction(currentLine);
         generateCallToRegisterNodeReached(nodeIndex);
      }

      @Override
      public void visitIntInsn(int opcode, int operand)
      {
         super.visitIntInsn(opcode, operand);
         handleRegularInstruction();
      }

      @Override
      public void visitIincInsn(int var, int increment)
      {
         super.visitIincInsn(var, increment);
         handleRegularInstruction();
      }

      @Override
      public void visitLdcInsn(Object cst)
      {
         super.visitLdcInsn(cst);
         handleRegularInstruction();
      }

      @Override
      public void visitTypeInsn(int opcode, String desc)
      {
         super.visitTypeInsn(opcode, desc);
         handleRegularInstruction();
      }

      @Override
      public void visitVarInsn(int opcode, int var)
      {
         super.visitVarInsn(opcode, var);
         handleRegularInstruction();
      }

      @Override
      public void visitFieldInsn(int opcode, String owner, String name, String desc)
      {
         super.visitFieldInsn(opcode, owner, name, desc);
         handleRegularInstruction();
      }

      @Override
      public void visitMethodInsn(int opcode, String owner, String name, String desc)
      {
         super.visitMethodInsn(opcode, owner, name, desc);
         handleRegularInstruction();
      }

      @Override
      public void visitTryCatchBlock(Label start, Label end, Label handler, String type)
      {
         super.visitTryCatchBlock(start, end, handler, type);
         handleRegularInstruction();
      }

      @Override
      public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels)
      {
         super.visitLookupSwitchInsn(dflt, keys, labels);
         handleRegularInstruction();
      }

      @Override
      public void visitTableSwitchInsn(int min, int max, Label dflt, Label[] labels)
      {
         super.visitTableSwitchInsn(min, max, dflt, labels);
         handleRegularInstruction();
      }

      @Override
      public void visitMultiANewArrayInsn(String desc, int dims)
      {
         super.visitMultiANewArrayInsn(desc, dims);
         handleRegularInstruction();
      }

      @Override
      public void visitEnd()
      {
         methodData.setLastLine(currentLine);
         fileData.addMethod(methodData);
      }
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
