/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.coverage;

import java.io.*;
import java.util.*;

import static mockit.external.asm.Opcodes.*;

import mockit.coverage.data.*;
import mockit.coverage.paths.*;
import mockit.external.asm.*;

final class CoverageModifier extends ClassWriter
{
   private static final Map<String, CoverageModifier> INNER_CLASS_MODIFIERS = new HashMap<String, CoverageModifier>();
   private static final int FIELD_MODIFIERS_TO_IGNORE = ACC_FINAL + ACC_SYNTHETIC;

   static byte[] recoverModifiedByteCodeIfAvailable(String innerClassName)
   {
      CoverageModifier modifier = INNER_CLASS_MODIFIERS.remove(innerClassName);
      return modifier == null ? null : modifier.toByteArray();
   }

   private String internalClassName;
   private String simpleClassName;
   private String sourceFileName;
   private FileCoverageData fileData;
   private boolean cannotModify;
   private final boolean forInnerClass;
   private boolean forEnumClass;

   CoverageModifier(ClassReader cr)
   {
      super(cr, true);
      forInnerClass = false;
   }

   private CoverageModifier(ClassReader cr, CoverageModifier other, String simpleClassName)
   {
      super(cr, true);
      sourceFileName = other.sourceFileName;
      fileData = other.fileData;
      internalClassName = other.internalClassName;
      this.simpleClassName = simpleClassName;
      forInnerClass = true;
   }

   @Override
   public void visit(int version, int access, String name, String signature, String superName, String[] interfaces)
   {
      if ((access & ACC_SYNTHETIC) != 0) {
         throw new VisitInterruptedException();
      }

      if (!forInnerClass) {
         internalClassName = name;
         int p = name.lastIndexOf('/');

         if (p < 0) {
            simpleClassName = name;
            sourceFileName = "";
         }
         else {
            simpleClassName = name.substring(p + 1);
            sourceFileName = name.substring(0, p + 1);
         }

         cannotModify = (access & ACC_ANNOTATION) != 0 || name.startsWith("mockit/coverage/");
      }

      forEnumClass = (access & ACC_ENUM) != 0;
      super.visit(version, access, name, signature, superName, interfaces);
   }

   @Override
   public void visitSource(String file, String debug)
   {
      if (!forInnerClass) {
         if (cannotModify) {
            throw VisitInterruptedException.INSTANCE;
         }

         sourceFileName += file;
         fileData = CoverageData.instance().addFile(sourceFileName);
      }

      super.visitSource(file, debug);
   }

   @Override
   public void visitInnerClass(String internalName, String outerName, String innerName, int access)
   {
      super.visitInnerClass(internalName, outerName, innerName, access);

      if (forInnerClass || isSyntheticOrEnumClass(access) || !isNestedInsideClassBeingModified(outerName)) {
         return;
      }

      String innerClassName = internalName.replace('/', '.');

      try {
         ClassReader innerCR = new ClassReader(innerClassName);
         CoverageModifier innerClassModifier = new CoverageModifier(innerCR, this, innerName);
         innerCR.accept(innerClassModifier, false);
         INNER_CLASS_MODIFIERS.put(innerClassName, innerClassModifier);
      }
      catch (IOException e) {
         e.printStackTrace();
      }
   }

   private boolean isSyntheticOrEnumClass(int access)
   {
      return (access & ACC_SYNTHETIC) != 0 || access == ACC_STATIC + ACC_ENUM;
   }

   private boolean isNestedInsideClassBeingModified(String outerName)
   {
      if (outerName == null) {
         return false;
      }

      int p = outerName.indexOf('$');
      String outerClassName = p < 0 ? outerName : outerName.substring(0, p);

      return outerClassName.equals(internalClassName);
   }

   @Override
   public FieldVisitor visitField(int access, String name, String desc, String signature, Object value)
   {
      if (fileData != null && (access & FIELD_MODIFIERS_TO_IGNORE) == 0) {
         fileData.dataCoverageInfo.addField(simpleClassName, name, (access & ACC_STATIC) != 0);
      }

      return super.visitField(access, name, desc, signature, value);
   }

   @Override
   public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions)
   {
      MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);

      if (fileData == null || (access & ACC_SYNTHETIC) != 0) {
         return mv;
      }

      if (name.charAt(0) == '<') {
         if (name.charAt(1) == 'c') {
            return forEnumClass ? mv : new StaticBlockModifier(mv);
         }

         if (Metrics.PATH_COVERAGE || Metrics.DATA_COVERAGE) { // TODO: fully separate these
            return new ConstructorModifier(mv);
         }
      }

      if (Metrics.PATH_COVERAGE || Metrics.DATA_COVERAGE) {
         return new MethodModifier(mv, name);
      }
      else {
         return new BaseMethodModifier(mv);
      }
   }

   private class BaseMethodModifier extends MethodAdapter
   {
      static final String DATA_RECORDING_CLASS = "mockit/coverage/TestRun";

      final MethodWriter mw;
      int currentLine;
      LineCoverageData lineData;
      final List<Label> visitedLabels = new ArrayList<Label>();
      final List<Label> jumpTargetsForCurrentLine = new ArrayList<Label>(4);
      private final Map<Label, Label> unconditionalJumps = new HashMap<Label, Label>(2);
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
         nextLabelAfterConditionalJump = false;
         unconditionalJumps.clear();

         generateCallToRegisterLineExecution();

         mw.visitLineNumber(line, start);
      }

      private void generateCallToRegisterLineExecution()
      {
         mw.visitLdcInsn(sourceFileName);
         pushCurrentLineOnTheStack();
         mw.visitMethodInsn(INVOKESTATIC, DATA_RECORDING_CLASS, "lineExecuted", "(Ljava/lang/String;I)V");
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
            int branchIndex = lineData.addBranch(mw.currentBlock, label);
            pendingBranches.put(branchIndex, false);

            if (assertFoundInCurrentLine) {
               BranchCoverageData branchData = lineData.getBranchData(branchIndex);
               branchData.markAsUnreachable();
            }
         }
         else {
            unconditionalJumps.put(label, mw.currentBlock);
         }

         mw.visitJumpInsn(opcode, label);

         if (nextLabelAfterConditionalJump) {
            generateCallToRegisterBranchTargetExecutionIfPending();
         }
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
            BranchCoverageData branchData = lineData.getBranchData(branchIndex);
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

         Label unconditionalJumpSource = unconditionalJumps.get(label);

         if (unconditionalJumpSource != null) {
            int branchIndex = lineData.addBranch(unconditionalJumpSource, label);
            BranchCoverageData branchData = lineData.getBranchData(branchIndex);
            branchData.setHasJumpTarget();
            generateCallToRegisterBranchTargetExecution("jumpTargetExecuted", branchIndex);
         }
      }

      private void generateCallToRegisterBranchTargetExecution(String methodName, int branchIndex)
      {
         mw.visitLdcInsn(sourceFileName);
         pushCurrentLineOnTheStack();
         mw.visitIntInsn(SIPUSH, branchIndex);
         mw.visitMethodInsn(INVOKESTATIC, DATA_RECORDING_CLASS, methodName, "(Ljava/lang/String;II)V");
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

   // TODO: implement max limit for number of paths
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
            mw.visitMethodInsn(INVOKESTATIC, DATA_RECORDING_CLASS, "nodeReached", "(Ljava/lang/String;II)V");
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
         boolean getField = opcode == GETSTATIC || opcode == GETFIELD;
         boolean isStatic = opcode == PUTSTATIC || opcode == GETSTATIC;
         char fieldType = desc.charAt(0);
         boolean size2 = fieldType == 'J' || fieldType == 'D';
         String classAndFieldNames = null;
         boolean fieldHasData = false;

         if (!owner.startsWith("java/")) {
            classAndFieldNames = owner.substring(owner.lastIndexOf('/') + 1) + '.' + name;
            fieldHasData = fileData.dataCoverageInfo.isFieldWithCoverageData(classAndFieldNames);

            if (fieldHasData && !isStatic) {
               generateCodeToSaveInstanceReferenceOnTheStack(getField, size2);
            }
         }

         super.visitFieldInsn(opcode, owner, name, desc);

         if (fieldHasData) {
            generateCallToRegisterFieldCoverage(getField, isStatic, size2, classAndFieldNames);
         }

         handleRegularInstruction();
      }

      private void generateCodeToSaveInstanceReferenceOnTheStack(boolean getField, boolean size2)
      {
         if (getField) {
            mw.visitInsn(DUP);
         }
         else if (size2) {
            mw.visitInsn(DUP2_X1);
            mw.visitInsn(POP2);
            mw.visitInsn(DUP_X2);
            mw.visitInsn(DUP_X2);
            mw.visitInsn(POP);
         }
         else {
            mw.visitInsn(DUP_X1);
            mw.visitInsn(POP);
            mw.visitInsn(DUP_X1);
            mw.visitInsn(DUP_X1);
            mw.visitInsn(POP);
         }
      }

      private void generateCallToRegisterFieldCoverage(
         boolean getField, boolean isStatic, boolean size2, String classAndFieldNames)
      {
         if (!isStatic && getField) {
            if (size2) {
               mw.visitInsn(DUP2_X1);
               mw.visitInsn(POP2);
            }
            else {
               mw.visitInsn(DUP_X1);
               mw.visitInsn(POP);
            }
         }

         mw.visitLdcInsn(sourceFileName);
         mw.visitLdcInsn(classAndFieldNames);

         String methodToCall = getField ? "fieldRead" : "fieldAssigned";
         String methodDesc =
            isStatic ?
               "(Ljava/lang/String;Ljava/lang/String;)V" : "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;)V";

         mw.visitMethodInsn(INVOKESTATIC, DATA_RECORDING_CLASS, methodToCall, methodDesc);
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
         int nodeIndex = nodeBuilder.handleForwardJumpsToNewTargets(dflt, labels, currentLine);
         generateCallToRegisterNodeReached(nodeIndex);

         super.visitLookupSwitchInsn(dflt, keys, labels);
      }

      @Override
      public final void visitTableSwitchInsn(int min, int max, Label dflt, Label[] labels)
      {
         int nodeIndex = nodeBuilder.handleForwardJumpsToNewTargets(dflt, labels, currentLine);
         generateCallToRegisterNodeReached(nodeIndex);

         super.visitTableSwitchInsn(min, max, dflt, labels);
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
            throw VisitInterruptedException.INSTANCE;
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
         // This is to ignore bytecode belonging to a static initialization block inserted in a regular line of code by
         // the Java compiler when the class contains at least one "assert" statement. Otherwise, that line of code
         // would always appear as partially covered when running with assertions enabled.
         assertFoundInCurrentLine =
            opcode == INVOKEVIRTUAL && "java/lang/Class".equals(owner) && "desiredAssertionStatus".equals(name);

         super.visitMethodInsn(opcode, owner, name, desc);
      }
   }
}
