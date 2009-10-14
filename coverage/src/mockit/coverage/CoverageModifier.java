/*
 * JMockit Coverage
 * Copyright (c) 2007-2009 Rogério Liesenfeld
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

      return new MethodModifier(mv, "<clinit>".equals(name));
   }

   private final class MethodModifier extends MethodAdapter
   {
      private boolean isTestMethod;
      private int currentLine;
      private LineCoverageData lineData;
      private final List<Label> startLabelsForVisitedLines = new ArrayList<Label>();
      private final List<Label> jumpTargetsForCurrentLine = new ArrayList<Label>();
      private final Map<Integer, Boolean> pendingBranches = new HashMap<Integer, Boolean>();
      private boolean assertFoundInCurrentLine;
      private final boolean clinit;

      private MethodModifier(MethodVisitor mv, boolean clinit)
      {
         super(mv);
         this.clinit = clinit;
      }

      @Override
      public AnnotationVisitor visitAnnotation(String desc, boolean visible)
      {
         isTestMethod = desc.startsWith("Lorg/junit/") || desc.startsWith("Lorg/testng/");
         return mv.visitAnnotation(desc, visible);
      }

      @Override
      public void visitLineNumber(int line, Label start)
      {
         if (isTestMethod) {
            mv.visitLineNumber(line, start);
            return;
         }

         lineData = fileData.addLine(line);
         assert lineData != null;

         currentLine = line;
         startLabelsForVisitedLines.add(start);
         jumpTargetsForCurrentLine.clear();
         pendingBranches.clear();

         generateCallToRegisterLineExecution();

         mv.visitLineNumber(line, start);
      }

      private void generateCallToRegisterLineExecution()
      {
         mv.visitLdcInsn(sourceFileName);
         pushCurrentLineOnTheStack();
         mv.visitMethodInsn(
            INVOKESTATIC, "mockit/coverage/CoverageData", "lineExecuted", "(Ljava/lang/String;I)V");
      }

      private void pushCurrentLineOnTheStack()
      {
         if (currentLine <= Short.MAX_VALUE) {
            mv.visitIntInsn(SIPUSH, currentLine);
         }
         else {
            mv.visitLdcInsn(currentLine);
         }
      }

      @Override
      public void visitJumpInsn(int opcode, Label label)
      {
         if (isTestMethod || startLabelsForVisitedLines.contains(label)) {
            assertFoundInCurrentLine = false;
            mv.visitJumpInsn(opcode, label);
            return;
         }

         int jumpInsnIndex = lineData.addSourceElement("if");

         String jumpInsnSource = sourceForJumpInsn(opcode);
         lineData.addSourceElement(jumpInsnSource);
         generateCallToRegisterBranchTargetExecutionIfPending(jumpInsnSource);

         int branchIndex = lineData.addBranch(jumpInsnIndex);
         jumpTargetsForCurrentLine.add(label);
         pendingBranches.put(branchIndex, false);

         if (assertFoundInCurrentLine) {
            BranchCoverageData branchData = lineData.getBranchData(branchIndex);
            branchData.markAsUnreachable();
         }

         mv.visitJumpInsn(opcode, label);

         // TODO: capture if this this point is executed?
      }

      private String sourceForJumpInsn(int opcode)
      {
         String source = "";

         if (opcode == IFEQ || opcode == IF_ICMPEQ || opcode == IFNULL) {
            source = "==";
         }
         else if (opcode == IFNE || opcode == IF_ICMPNE || opcode == IFNONNULL) {
            source = "!=";
         }
         else if (opcode == IFGE || opcode == IF_ICMPGE) {
            source = ">=";
         }
         else if (opcode == IFGT || opcode == IF_ICMPGT) {
            source = ">";
         }
         else if (opcode == IFLE || opcode == IF_ICMPLE) {
            source = "<=";
         }
         else if (opcode == IFLT || opcode == IF_ICMPLT) {
            source = "<";
         }
         if (opcode == IF_ACMPEQ) {
            source = "==";
         }
         else if (opcode == IF_ACMPNE) {
            source = "!=";
         }

         return source;
      }

      private void generateCallToRegisterBranchTargetExecutionIfPending(String targetInsnSource)
      {
         if (!pendingBranches.isEmpty()) {
            int targetInsnSourceIndex = lineData.addSourceElement(targetInsnSource);

            for (Integer branchIndex : pendingBranches.keySet()) {
               BranchCoverageData branchData = lineData.getBranchData(branchIndex);
               Boolean firstInsnAfterJump = pendingBranches.get(branchIndex);

               if (firstInsnAfterJump) {
                  branchData.setJumpTargetInsnIndex(targetInsnSourceIndex);
                  generateCallToRegisterBranchTargetExecution("jumpTargetExecuted", branchIndex);
               }
               else {
                  branchData.setNoJumpTargetInsnIndex(targetInsnSourceIndex);
                  generateCallToRegisterBranchTargetExecution("noJumpTargetExecuted", branchIndex);
               }
            }

            pendingBranches.clear();
         }
      }

      @Override
      public void visitLabel(Label label)
      {
         mv.visitLabel(label);

         int branchIndex = jumpTargetsForCurrentLine.indexOf(label);

         if (branchIndex >= 0) {
            pendingBranches.put(branchIndex, true);
            assertFoundInCurrentLine = false;
         }
      }

      private void generateCallToRegisterBranchTargetExecution(String methodName, int branchIndex)
      {
         mv.visitLdcInsn(sourceFileName);
         pushCurrentLineOnTheStack();
         mv.visitIntInsn(SIPUSH, branchIndex);
         mv.visitMethodInsn(
            INVOKESTATIC, "mockit/coverage/CoverageData", methodName, "(Ljava/lang/String;II)V");
      }

      @Override
      public void visitInsn(int opcode)
      {
         String source;

         if (opcode >= IRETURN && opcode <= RETURN) {
            source = "return";
         }
         else {
            source = "*";
         }

         if (opcode > DCONST_1) {
            generateCallToRegisterBranchTargetExecutionIfPending(source);
         }

         mv.visitInsn(opcode);
      }

      @Override
      public void visitIntInsn(int opcode, int operand)
      {
         generateCallToRegisterBranchTargetExecutionIfPending("*");
         mv.visitIntInsn(opcode, operand);
      }

      @Override
      public void visitVarInsn(int opcode, int var)
      {
         String source = "";

         if (opcode >= ILOAD && opcode <= ALOAD) {
//            source = "=v" + var;
         }
         else if (opcode >= ISTORE && opcode <= ASTORE) {
//            source = "v" + var + "=";
         }
         else {
            source = "return";
         }

         generateCallToRegisterBranchTargetExecutionIfPending(source);
         mv.visitVarInsn(opcode, var);
      }

      @Override
      public void visitTypeInsn(int opcode, String desc)
      {
         String source;

         if (opcode == NEW || opcode == ANEWARRAY) {
            source = "new";
         }
         else if (opcode == INSTANCEOF) {
            source = "instanceof";
         }
         else {
            source = "(" + desc + ")";
         }

         generateCallToRegisterBranchTargetExecutionIfPending(source);
         mv.visitTypeInsn(opcode, desc);
      }

      @Override
      public void visitFieldInsn(int opcode, String owner, String name, String desc)
      {
         assertFoundInCurrentLine = opcode == GETSTATIC && "$assertionsDisabled".equals(name);

         String source = owner + "." + name;

         if (opcode == GETSTATIC || opcode == GETFIELD) {
            source = "=" + source;
         }
         else {
            source += "=";
         }

         generateCallToRegisterBranchTargetExecutionIfPending(source);
         mv.visitFieldInsn(opcode, owner, name, desc);
      }

      @Override
      public void visitMethodInsn(int opcode, String owner, String name, String desc)
      {
         // This is to ignore bytecode belonging to a static initialization block inserted in a
         // regular line of code by the Java compiler when the class contains at least one "assert"
         // statement. Otherwise, that line of code would always appear as partially covered when
         // running with assertions enabled.
         assertFoundInCurrentLine =
            clinit && opcode == INVOKEVIRTUAL &&
            "java/lang/Class".equals(owner) && "desiredAssertionStatus".equals(name);

         generateCallToRegisterBranchTargetExecutionIfPending(owner + "." + name + "()");
         mv.visitMethodInsn(opcode, owner, name, desc);
      }

      @Override
      public void visitLdcInsn(Object cst)
      {
         generateCallToRegisterBranchTargetExecutionIfPending(cst.toString());
         mv.visitLdcInsn(cst);
      }

      @Override
      public void visitIincInsn(int var, int increment)
      {
         String source = "v" + var;

         if (increment > 0) {
            source += "+=" + increment;
         }
         else {
            source += "-=" + -increment;
         }

         generateCallToRegisterBranchTargetExecutionIfPending(source);
         mv.visitIincInsn(var, increment);
      }

      @Override
      public void visitTableSwitchInsn(int min, int max, Label dflt, Label[] labels)
      {
         generateCallToRegisterBranchTargetExecutionIfPending("case");
         mv.visitTableSwitchInsn(min, max, dflt, labels);
      }

      @Override
      public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels)
      {
         generateCallToRegisterBranchTargetExecutionIfPending("case2");
         mv.visitLookupSwitchInsn(dflt, keys, labels);
      }

      @Override
      public void visitMultiANewArrayInsn(String desc, int dims)
      {
         generateCallToRegisterBranchTargetExecutionIfPending(desc + "[]" + dims);
         mv.visitMultiANewArrayInsn(desc, dims);
      }
   }
}
