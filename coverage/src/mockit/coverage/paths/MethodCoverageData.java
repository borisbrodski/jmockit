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

import java.io.*;
import java.util.*;

import org.objectweb.asm2.*;

public final class MethodCoverageData implements Serializable
{
   private static final long serialVersionUID = -5073393714435522417L;

   public final String methodName;
   private final List<Node> allNodes = new ArrayList<Node>();
   public final List<Path> paths = new ArrayList<Path>();
   private int firstLine;
   private int lastLine;

   // Helper fields used during path building:
   private transient boolean addNextBlockToActivePaths;
   private final transient Map<Label, List<Path>> jumpTargetToAlternatePaths =
      new LinkedHashMap<Label, List<Path>>();
   private final transient List<Path> activePaths = new ArrayList<Path>();

   private transient Node.Entry entryNode;
   private transient Node.Fork currentFork;
   private final transient Map<Label, List<Node.Fork>> jumpTargetToForks =
      new LinkedHashMap<Label, List<Node.Fork>>();
   private transient Node.BasicBlock currentBasicBlock;
   private final transient Map<Label, List<Node.Successor>> gotoTargetToSuccessors =
      new LinkedHashMap<Label, List<Node.Successor>>();
   private transient Node.Join currentJoin;

   // Helper fields used during path execution:
   private final transient ThreadLocal<Integer> nodesReached = new ThreadLocal<Integer>();

   public MethodCoverageData(String methodName)
   {
      this.methodName = methodName;
   }

   public int handlePotentialNewBlock(int line)
   {
      if (entryNode == null /*paths.isEmpty()*/) {
         createEntryNode(line);
         return 0;
      }
      else {
         return handleRegularInstruction(line);
      }
   }

   private void createEntryNode(int line)
   {
      firstLine = line;

      entryNode = new Node.Entry(line);
      allNodes.add(entryNode);

//      Path path = new Path(entryNode);
//      paths.add(path);
//
//      activePaths.add(path);
   }

   public void markCurrentLineAsStartingNewBlock()
   {
      addNextBlockToActivePaths = true;
   }

   public int handleRegularInstruction(int line)
   {
//      if (!addNextBlockToActivePaths) {
//         return -1;
//      }

//      addNextBlockToActivePaths = false;
//      assert !activePaths.isEmpty() : "No active paths for next block at line " + line;

      if (currentFork == null && currentJoin == null) {
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

   private void connectNodes(Node.BasicBlock newBasicBlock)
   {
      if (currentFork != null) {
         currentFork.nextConsecutiveNode = newBasicBlock;
         currentFork = null;
      }
      else {
         currentJoin.nextNode = newBasicBlock;
         currentJoin = null;
      }

      currentBasicBlock = newBasicBlock;
   }

   private int addNewNode(Node newNode)
   {
      int newNodeIndex = allNodes.size();
      allNodes.add(newNode);

//      for (Path path : activePaths) {
//         path.addNode(newNode);
//      }

      return newNodeIndex;
   }

   public int handleJump(Label targetBlock, int line, boolean conditional)
   {
      if (!conditional) {
//         assert !addNextBlockToActivePaths;

         if (currentBasicBlock != null || currentJoin != null) {
            List<Node.Successor> successors = gotoTargetToSuccessors.get(targetBlock);

            if (successors == null) {
               successors = new LinkedList<Node.Successor>();
               gotoTargetToSuccessors.put(targetBlock, successors);
            }

            if (currentBasicBlock != null) {
               successors.add(currentBasicBlock);
               currentBasicBlock = null;
            }
            else {
               successors.add(currentJoin);
               currentJoin = null;
            }
         }

         return -1;
      }

      Node.Fork newNode = new Node.Fork(line);
      connectNodes(targetBlock, newNode);

      int newNodeIndex = addNewNode(newNode);
//
//      List<Path> alternatePathsForTarget = findOrCreateListOfAlternatePaths(targetBlock);
//      createAlternatePathsForActivePaths(alternatePathsForTarget);
//
//      addNextBlockToActivePaths = true;
      return newNodeIndex;
   }

   private void connectNodes(Label targetBlock, Node.Fork newFork)
   {
      if (entryNode.nextNode == null) {
         entryNode.nextNode = newFork;
      }

      if (currentJoin != null) {
         currentJoin.nextNode = newFork;
         currentJoin = null;
      }

      currentFork = newFork;
      List<Node.Fork> forksWithSameTarget = jumpTargetToForks.get(targetBlock);

      if (forksWithSameTarget == null) {
         forksWithSameTarget = new LinkedList<Node.Fork>();
         jumpTargetToForks.put(targetBlock, forksWithSameTarget);
      }

      forksWithSameTarget.add(newFork);

      if (currentBasicBlock != null) {
         currentBasicBlock.nextConsecutiveNode = newFork;
         currentBasicBlock = null;
      }
   }

   private void createAlternatePathsForActivePaths(List<Path> alternatePaths)
   {
      for (Path path : activePaths) {
         addNewAlternatePath(alternatePaths, path);
      }
   }

   private List<Path> findOrCreateListOfAlternatePaths(Label targetBlock)
   {
      List<Path> alternatePaths = jumpTargetToAlternatePaths.get(targetBlock);

      if (alternatePaths == null) {
         alternatePaths = new LinkedList<Path>();
         jumpTargetToAlternatePaths.put(targetBlock, alternatePaths);
      }

      return alternatePaths;
   }

   private void addNewAlternatePath(List<Path> alternatePaths, Path sharedSubPath)
   {
      if (paths.size() < 40) {
         Path alternatePath = new Path(sharedSubPath);
         paths.add(alternatePath);
         alternatePaths.add(alternatePath);
      }
   }

   public void handleForwardJumpsToNewTargets(Label defaultBlock, Label[] caseBlocks)
   {
      for (Label targetBlock : caseBlocks) {
         if (targetBlock != defaultBlock) {
            handleForwardJumpToNewTarget(targetBlock);
         }
      }

      handleForwardJumpToNewTarget(defaultBlock);
   }

   private void handleForwardJumpToNewTarget(Label targetBlock)
   {
      List<Path> alternatePathsForTarget = new LinkedList<Path>();
      jumpTargetToAlternatePaths.put(targetBlock, alternatePathsForTarget);
      createAlternatePathsForActivePaths(alternatePathsForTarget);
   }

   public int handleJumpTarget(Label basicBlock, int line)
   {
      // Will be null for visitLabel calls preceding visitLineNumber:
//      List<Path> alternatePaths = jumpTargetToAlternatePaths.get(basicBlock);
//
//      if (alternatePaths == null) {
//         return -1;
//      }

      if (
         !jumpTargetToForks.containsKey(basicBlock) &&
         !gotoTargetToSuccessors.containsKey(basicBlock)
      ) {
         return -1;
      }

      Node.Join newNode = new Node.Join(line);
      int newNodeIndex = allNodes.size();
      allNodes.add(newNode);

      connectNodes(basicBlock, newNode);

//      for (Path alternatePath : alternatePaths) {
//         assert !activePaths.contains(alternatePath) : "Alternate path already active";
//         alternatePath.addNode(newNode);
//      }
//
//      activePaths.addAll(alternatePaths);
      return newNodeIndex;
   }

   private void connectNodes(Label basicBlock, Node.Join newJoin)
   {
      connectNodes(newJoin);

      List<Node.Fork> forks = jumpTargetToForks.get(basicBlock);

      if (forks != null) {
         for (Node.Fork fork : forks) {
            fork.nextNodesAfterJump.add(newJoin);
         }

         jumpTargetToForks.remove(basicBlock);
      }

      List<Node.Successor> successors = gotoTargetToSuccessors.get(basicBlock);

      if (successors != null) {
         for (Node.Successor successorToGoto : successors) {
            if (successorToGoto instanceof Node.BasicBlock) {
               ((Node.BasicBlock) successorToGoto).nextNodeAfterGoto = newJoin;
            }
            else {
               ((Node.Join) successorToGoto).nextNode = newJoin;
            }
         }

         gotoTargetToSuccessors.remove(basicBlock);
      }

      currentJoin = newJoin;
   }

   public int handleExit(int exitLine)
   {
      // TODO: try...catch...finally aren't handled yet
//      assert !activePaths.isEmpty() : "No active paths for exit at line " + exitLine;

      Node.Exit newNode = new Node.Exit(exitLine);
      int newNodeIndex = allNodes.size();
      allNodes.add(newNode);

      connectNodes(newNode);

//      for (Path path : activePaths) {
//         path.addExitNode(newNode);
//      }
//
//      for (List<Path> alternatePaths : jumpTargetToAlternatePaths.values()) {
//         alternatePaths.removeAll(activePaths);
//      }
//
//      activePaths.clear();
      return newNodeIndex;
   }

   private void connectNodes(Node.Successor newNode)
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

   public void markNodeAsReached(int nodeIndex)
   {
      if (nodeIndex == 0) {
         clearNodes();
      }

      Node node = allNodes.get(nodeIndex);
      Integer currentNodesReached = nodesReached.get();

      if (!node.wasReached()) {
         node.setReached(Boolean.TRUE);
         currentNodesReached++;
         nodesReached.set(currentNodesReached);
      }

      if (node instanceof Node.Exit) {
         Node.Exit exitNode = (Node.Exit) node;

         for (Path path : exitNode.paths) {
            if (path.countExecutionIfAllNodesWereReached(currentNodesReached)) {
               return;
            }
         }
      }
   }

   private void clearNodes()
   {
      for (Node node : allNodes) {
         node.setReached(null);
      }

      nodesReached.set(0);
   }

   public int getFirstLineOfImplementationBody()
   {
      return firstLine;
   }

   public int getLastLineOfImplementationBody()
   {
      return lastLine;
   }

   public void setLastLine(int lastLine)
   {
      this.lastLine = lastLine;

//      paths.clear();

      Path path = new Path(entryNode);
      paths.add(path);

      Node.Fork fork = entryNode.nextNode;

      if (fork == null) {
         Node.Exit exitNode = (Node.Exit) allNodes.get(1);
         exitNode.paths.add(path);
         path.addNode(exitNode);
         return;
      }

      addForkToPath(path, fork);
   }

   private void addForkToPath(Path path, Node.Fork fork)
   {
      path.addNode(fork);

      for (Node.Join join : fork.nextNodesAfterJump) {
         Path alternatePath = new Path(path);
         paths.add(alternatePath);
         addJoinToPath(alternatePath, join);
      }

      Node.BasicBlock basicBlock = fork.nextConsecutiveNode;
      addBasicBlockToPath(path, basicBlock);
   }

   private void addBasicBlockToPath(Path path, Node.BasicBlock basicBlock)
   {
      path.addNode(basicBlock);

      Node.Join nextNodeAfterGoto = basicBlock.nextNodeAfterGoto;

      if (nextNodeAfterGoto != null) {
         addJoinToPath(path, nextNodeAfterGoto);
         return;
      }

      addSuccessorToPath(path, basicBlock.nextConsecutiveNode);
   }

   private void addSuccessorToPath(Path path, Node.Successor successor)
   {
      if (successor instanceof Node.BasicBlock) {
         addBasicBlockToPath(path, (Node.BasicBlock) successor);
      }
      else if (successor instanceof Node.Fork) {
         addForkToPath(path, (Node.Fork) successor);
      }
      else if (successor instanceof Node.Join) {
         addJoinToPath(path, (Node.Join) successor);
      }
      else if (successor instanceof Node.Exit) {
         Node.Exit exitNode = (Node.Exit) successor;
         exitNode.paths.add(path);
         path.addNode(exitNode);
      }
   }

   private void addJoinToPath(Path path, Node.Join join)
   {
      path.addNode(join);
      addSuccessorToPath(path, join.nextNode);
   }

   public int getExecutionCount()
   {
      int totalCount = 0;

      for (Path path : paths) {
         totalCount += path.getExecutionCount();
      }

      return totalCount;
   }

   public int getCoveredPaths()
   {
      int coveredCount = 0;

      for (Path path : paths) {
         if (path.getExecutionCount() > 0) {
            coveredCount++;
         }
      }

      return coveredCount;
   }
}
