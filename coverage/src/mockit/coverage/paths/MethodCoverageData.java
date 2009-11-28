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
   public final List<Path> paths = new ArrayList<Path>();
   private int firstLine;
   private int lastLine;

   // Helper fields used during path building:
   private transient boolean addNextBlockToActivePaths;
   private final transient Map<Label, List<Path>> jumpTargetToAlternatePaths =
      new LinkedHashMap<Label, List<Path>>();
   private final transient List<Path> activePaths = new ArrayList<Path>();
   private final transient List<Node> allNodes = new ArrayList<Node>();

   public MethodCoverageData(String methodName)
   {
      this.methodName = methodName;
   }

   public int handlePotentialNewBlock(int line)
   {
      if (paths.isEmpty()) {
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

      Node.Entry entryNode = new Node.Entry(line);
      allNodes.add(entryNode);

      Path path = new Path(entryNode);
      paths.add(path);

      activePaths.add(path);
   }

   public void markCurrentLineAsStartingNewBlock()
   {
      addNextBlockToActivePaths = true;
   }

   public int handleRegularInstruction(int line)
   {
      if (!addNextBlockToActivePaths) {
         return -1;
      }

      addNextBlockToActivePaths = false;
      assert !activePaths.isEmpty() : "No active paths for next block at line " + line;

      Node newNode = new Node.BasicBlock(line);

      return addNewNodeToActivePaths(newNode);
   }

   private int addNewNodeToActivePaths(Node newNode)
   {
      int newNodeIndex = allNodes.size();
      allNodes.add(newNode);

      for (Path path : activePaths) {
         path.addNode(newNode);
      }

      return newNodeIndex;
   }

   public int handleJump(Label targetBlock, int line, boolean conditional)
   {
      if (!conditional) {
         assert !addNextBlockToActivePaths;
         return -1;
      }

      Node newNode = new Node.Fork(line);
      int nodeIndex = addNewNodeToActivePaths(newNode);

      List<Path> alternatePathsForTarget = findOrCreateListOfAlternatePaths(targetBlock);
      createAlternatePathsForActivePaths(alternatePathsForTarget);

      addNextBlockToActivePaths = true;
      return nodeIndex;
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
      List<Path> alternatePaths = jumpTargetToAlternatePaths.get(basicBlock);

      if (alternatePaths == null) {
         return -1;
      }

      Node newNode = new Node.Join(line);
      int newNodeIndex = allNodes.size();
      allNodes.add(newNode);

      for (Path alternatePath : alternatePaths) {
         assert !activePaths.contains(alternatePath) : "Alternate path already active";
         alternatePath.addNode(newNode);
      }

      activePaths.addAll(alternatePaths);
      return newNodeIndex;
   }

   public int handleExit(int exitLine)
   {
      // TODO: try...catch...finally aren't handled yet
//      assert !activePaths.isEmpty() : "No active paths for exit at line " + exitLine;

      Node.Exit newNode = new Node.Exit(exitLine);
      int newNodeIndex = allNodes.size();
      allNodes.add(newNode);

      for (Path path : activePaths) {
         path.addExitNode(newNode);
      }

      for (List<Path> alternatePaths : jumpTargetToAlternatePaths.values()) {
         alternatePaths.removeAll(activePaths);
      }

      activePaths.clear();
      return newNodeIndex;
   }

   public void markNodeAsReached(int nodeIndex)
   {
      Node node = allNodes.get(nodeIndex);
      node.setReached(Boolean.TRUE);

      if (node instanceof Node.Exit) {
         for (Path path : paths) {
            if (path.countExecutionIfAllNodesWereReached()) {
               return;
            }
         }
      }
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
