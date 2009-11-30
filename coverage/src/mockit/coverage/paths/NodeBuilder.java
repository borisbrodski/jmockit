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
package mockit.coverage.paths;

import java.util.*;

import org.objectweb.asm2.*;

public final class NodeBuilder
{
   public int firstLine;
   final List<Node> nodes = new ArrayList<Node>();

   private Node.Entry entryNode;
   private Node.SimpleFork currentSimpleFork;
   private Node.BasicBlock currentBasicBlock;
   private Node.Join currentJoin;
   private final Map<Label, List<Node.Fork>> jumpTargetToForks =
      new LinkedHashMap<Label, List<Node.Fork>>();
   private final Map<Label, List<Node.GotoSuccessor>> gotoTargetToSuccessors =
      new LinkedHashMap<Label, List<Node.GotoSuccessor>>();

   public int handlePotentialNewBlock(int line)
   {
      if (entryNode == null) {
         firstLine = line;
         entryNode = new Node.Entry(line);
         return addNewNode(entryNode);
      }

      return handleRegularInstruction(line);
   }

   private int addNewNode(Node newNode)
   {
      int newNodeIndex = nodes.size();
      nodes.add(newNode);
      return newNodeIndex;
   }

   public int handleRegularInstruction(int line)
   {
      if (currentSimpleFork == null && currentJoin == null) {
         return -1;
      }

      if (currentBasicBlock == null) {
         Node.BasicBlock newNode = new Node.BasicBlock(line);
         connectNodes(newNode);
         return addNewNode(newNode);
      }
      else {
         connectNodes(currentBasicBlock);
         return -1;
      }
   }

   public int handleJump(Label targetBlock, int line, boolean conditional)
   {
      if (conditional) {
         Node.SimpleFork newFork = new Node.SimpleFork(line);
         currentSimpleFork = newFork;
         connectNodes(targetBlock, newFork);
         return addNewNode(newFork);
      }
      else {
         setUpMappingFromGotoTargetToCurrentGotoSuccessor(targetBlock);
         return -1;
      }
   }

   public int handleJumpTarget(Label basicBlock, int line)
   {
      // Ignore for visitLabel calls preceding visitLineNumber:
      if (isNewLineTarget(basicBlock)) {
         return -1;
      }

      Node.Join newNode = new Node.Join(line);
      connectNodes(basicBlock, newNode);

      return addNewNode(newNode);
   }

   private boolean isNewLineTarget(Label basicBlock)
   {
      return
         !jumpTargetToForks.containsKey(basicBlock) &&
         !gotoTargetToSuccessors.containsKey(basicBlock);
   }

   private void connectNodes(Node.BasicBlock newBasicBlock)
   {
      if (currentSimpleFork != null) {
         currentSimpleFork.nextConsecutiveNode = newBasicBlock;
         currentSimpleFork = null;
      }
      else {
         currentJoin.nextNode = newBasicBlock;
         currentJoin = null;
      }

      currentBasicBlock = newBasicBlock;
   }

   private void connectNodes(Label targetBlock, Node.Fork newFork)
   {
      if (entryNode.nextNode == null) {
         entryNode.nextNode = newFork;
      }

      setUpMappingFromConditionalTargetToFork(targetBlock, newFork);
      connectNodes(newFork);
   }

   private void setUpMappingFromConditionalTargetToFork(Label targetBlock, Node.Fork newFork)
   {
      List<Node.Fork> forksWithSameTarget = jumpTargetToForks.get(targetBlock);

      if (forksWithSameTarget == null) {
         forksWithSameTarget = new LinkedList<Node.Fork>();
         jumpTargetToForks.put(targetBlock, forksWithSameTarget);
      }

      forksWithSameTarget.add(newFork);
   }

   private void setUpMappingFromGotoTargetToCurrentGotoSuccessor(Label targetBlock)
   {
      if (currentBasicBlock == null && currentJoin == null) {
         return;
      }

      List<Node.GotoSuccessor> successors = gotoTargetToSuccessors.get(targetBlock);

      if (successors == null) {
         successors = new LinkedList<Node.GotoSuccessor>();
         gotoTargetToSuccessors.put(targetBlock, successors);
      }

      // TODO: they both can be non-null here; what to do?
      if (currentBasicBlock != null) {
         successors.add(currentBasicBlock);
         currentBasicBlock = null;
      }
      else {
         successors.add(currentJoin);
         currentJoin = null;
      }
   }

   private void connectNodes(Label basicBlock, Node.Join newJoin)
   {
      connectNodes(newJoin);
      connectSourceForksToTargetedJoin(basicBlock, newJoin);
      connectGotoSuccessorsToNewJoin(basicBlock, newJoin);
      currentJoin = newJoin;
   }

   public int handleExit(int exitLine)
   {
      Node.Exit newNode = new Node.Exit(exitLine);
      connectNodes(newNode);

      return addNewNode(newNode);
   }

   private void connectNodes(Node.ConditionalSuccessor newNode)
   {
      if (currentJoin != null) {
         currentJoin.nextNode = newNode;
         currentJoin = null;
      }

      if (currentBasicBlock != null) {
         currentBasicBlock.nextConsecutiveNode = newNode;
         currentBasicBlock = null;
      }
   }

   private void connectSourceForksToTargetedJoin(Label targetBlock, Node.Join newJoin)
   {
      List<Node.Fork> forks = jumpTargetToForks.get(targetBlock);

      if (forks != null) {
         for (Node.Fork fork : forks) {
            fork.addNextNode(newJoin);
         }

         jumpTargetToForks.remove(targetBlock);
      }
   }

   private void connectGotoSuccessorsToNewJoin(Label targetBlock, Node.Join newJoin)
   {
      List<Node.GotoSuccessor> successors = gotoTargetToSuccessors.get(targetBlock);

      if (successors != null) {
         for (Node.GotoSuccessor successorToGoto : successors) {
            successorToGoto.setNextNodeAfterGoto(newJoin);
         }

         gotoTargetToSuccessors.remove(targetBlock);
      }
   }

   public int handleForwardJumpsToNewTargets(Label defaultBlock, Label[] caseBlocks, int line)
   {
      Node.Fork newJoin = new Node.MultiFork(line);

      for (Label targetBlock : caseBlocks) {
         if (targetBlock != defaultBlock) {
            connectNodes(targetBlock, newJoin);
         }
      }

      connectNodes(defaultBlock, newJoin);

      return addNewNode(newJoin);
   }
}
