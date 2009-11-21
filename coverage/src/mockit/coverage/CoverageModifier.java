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

import org.objectweb.asm2.*;
import static org.objectweb.asm2.Opcodes.*;

final class CoverageModifier extends ClassWriter
{
   private final CoverageData coverageData;
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
      sourceFileName = p < 0 ? "" : name.substring(0, p + 1);

      cannotModify = name.startsWith("mockit/coverage/");

      super.visit(version, access, name, signature, superName, interfaces);
   }

   @Override
   public void visitSource(String file, String debug)
   {
      sourceFileName += file;
      fileData = coverageData.addFile(sourceFileName);

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

      return "<clinit>".equals(name) ?
         new StaticBlockModifier(mv) : new MethodModifier(mv, name + desc);
   }

   private class BaseMethodModifier extends MethodAdapter
   {
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
            INVOKESTATIC, "mockit/coverage/CoverageData", "lineExecuted", "(Ljava/lang/String;I)V");
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
            visitJumpInsnWithoutModifications(opcode, label);
            return;
         }

         jumpTargetsForCurrentLine.add(label);

         int branchIndex = lineData.addSegment(mw.currentBlock);
         pendingBranches.put(branchIndex, false);

         mw.visitJumpInsn(opcode, label);

         if (potentialAssertFalseFound && opcode == IFNE) {
            // TODO: what to do here, if anything?
         }

         generateCallToRegisterBranchTargetExecutionIfPending();

         if (assertFoundInCurrentLine) {
            BranchCoverageData branchData = lineData.getSegmentData(branchIndex);
            branchData.markAsUnreachable();
         }

         nextLabelAfterConditionalJump = opcode != GOTO && opcode != JSR;
      }

      final void visitJumpInsnWithoutModifications(int opcode, Label label)
      {
         assertFoundInCurrentLine = false;
         mw.visitJumpInsn(opcode, label);
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
            INVOKESTATIC, "mockit/coverage/CoverageData", methodName, "(Ljava/lang/String;II)V");
      }

      @Override
      public void visitInsn(int opcode)
      {
         generateCallToRegisterBranchTargetExecutionIfPending();
         mw.visitInsn(opcode);
      }

      @Override
      public final void visitIntInsn(int opcode, int operand)
      {
         generateCallToRegisterBranchTargetExecutionIfPending();
         mw.visitIntInsn(opcode, operand);
      }

      @Override
      public final void visitVarInsn(int opcode, int var)
      {
         generateCallToRegisterBranchTargetExecutionIfPending();
         mw.visitVarInsn(opcode, var);

         if (opcode == RET) {
            System.out.println("RET instruction found!");
         }
      }

      @Override
      public final void visitTypeInsn(int opcode, String desc)
      {
         generateCallToRegisterBranchTargetExecutionIfPending();
         mw.visitTypeInsn(opcode, desc);
      }

      @Override
      public final void visitFieldInsn(int opcode, String owner, String name, String desc)
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
      public final void visitLdcInsn(Object cst)
      {
         generateCallToRegisterBranchTargetExecutionIfPending();
         mw.visitLdcInsn(cst);
      }

      @Override
      public final void visitIincInsn(int var, int increment)
      {
         generateCallToRegisterBranchTargetExecutionIfPending();
         mw.visitIincInsn(var, increment);
      }

      @Override
      public final void visitTableSwitchInsn(int min, int max, Label dflt, Label[] labels)
      {
         generateCallToRegisterBranchTargetExecutionIfPending();
         mw.visitTableSwitchInsn(min, max, dflt, labels);
      }

      @Override
      public final void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels)
      {
         generateCallToRegisterBranchTargetExecutionIfPending();
         mw.visitLookupSwitchInsn(dflt, keys, labels);
      }

      @Override
      public final void visitMultiANewArrayInsn(String desc, int dims)
      {
         generateCallToRegisterBranchTargetExecutionIfPending();
         mw.visitMultiANewArrayInsn(desc, dims);
      }
   }

   private final class MethodModifier extends BaseMethodModifier
   {
      final MethodCoverageData methodData;
      boolean isTestMethod;

      MethodModifier(MethodVisitor mv, String methodNameAndDesc)
      {
         super(mv);
         methodData = new MethodCoverageData(methodNameAndDesc);
         fileData.methods.add(methodData);
      }

      @Override
      public AnnotationVisitor visitAnnotation(String desc, boolean visible)
      {
         isTestMethod = desc.startsWith("Lorg/junit/") || desc.startsWith("Lorg/testng/");
         return mw.visitAnnotation(desc, visible);
      }

      @Override
      public void visitLineNumber(int line, Label start)
      {
         if (isTestMethod) {
            mw.visitLineNumber(line, start);
            return;
         }

         super.visitLineNumber(line, start);
      }

      @Override
      public void visitLabel(Label label)
      {
         super.visitLabel(label);

         if (methodData.entryBlock == null) {
            methodData.entryBlock = mw.currentBlock;
         }
      }

      @Override
      public void visitJumpInsn(int opcode, Label label)
      {
         if (isTestMethod) {
            visitJumpInsnWithoutModifications(opcode, label);
            return;
         }

         super.visitJumpInsn(opcode, label);
      }

      @Override
      public void visitInsn(int opcode)
      {
         Label currentBlock = mw.currentBlock;

         super.visitInsn(opcode);

         if (mw.currentBlock == null) {
            methodData.exitBlocks.add(currentBlock);
         }
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
