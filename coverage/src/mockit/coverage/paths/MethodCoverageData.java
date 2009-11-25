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

   public final List<Path> paths = new ArrayList<Path>();

   // Helper fields used during path building:
   private transient boolean addNextBlockToActivePaths;
   private final transient Map<Label, List<Path>> jumpTargetToAlternatePaths =
      new LinkedHashMap<Label, List<Path>>();
   private final transient List<Path> activePaths = new ArrayList<Path>();
   private final transient List<Node> allNodes = new ArrayList<Node>();

   public int handlePotentialNewBlock(int line)
   {
      if (paths.isEmpty()) {
         Node.Entry entryNode = new Node.Entry(line);
         Path path = new Path(entryNode);
         paths.add(path);
         activePaths.add(path);
         allNodes.add(entryNode);
         return 0;
      }
      else {
         return handleRegularInstruction(line);
      }
   }

   public int handleRegularInstruction(int line)
   {
      if (!addNextBlockToActivePaths) {
         return -1;
      }

      assert !activePaths.isEmpty() : "No active paths for next block at line " + line;

      Node.BasicBlock newNode = new Node.BasicBlock(line);
      int newNodeIndex = allNodes.size();
      allNodes.add(newNode);

      for (Path path : activePaths) {
         path.addNode(newNode);
      }

      addNextBlockToActivePaths = false;
      return newNodeIndex;
   }

   public void handleJump(Label targetBlock, boolean conditional)
   {
      if (conditional) {
         List<Path> alternatePathsForTarget = getAlternatePathsForTarget(targetBlock);

         for (Path path : activePaths) {
            addNewAlternatePath(alternatePathsForTarget, path);
         }
      }

      addNextBlockToActivePaths = conditional;
   }

   private List<Path> getAlternatePathsForTarget(Label targetBlock)
   {
      List<Path> alternatePaths = jumpTargetToAlternatePaths.get(targetBlock);

      if (alternatePaths == null) {
         alternatePaths = new LinkedList<Path>();
         jumpTargetToAlternatePaths.put(targetBlock, alternatePaths);
      }

      return alternatePaths;
   }

   private void addNewAlternatePath(List<Path> alternatePathsForThisTarget, Path regularPath)
   {
      if (paths.size() < 20) {
         Path alternatePath = new Path(regularPath);
         paths.add(alternatePath);
         alternatePathsForThisTarget.add(alternatePath);
      }
   }

   public int handleJumpTarget(Label basicBlock, int line)
   {
      // Will be null for visitLabel calls preceding visitLineNumber:
      List<Path> alternatePaths = jumpTargetToAlternatePaths.get(basicBlock);

      if (alternatePaths == null) {
         return -1;
      }

      Node.Join newNode = new Node.Join(line);
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

   public int getExecutionCount()
   {
      int totalCount = 0;

      for (Path path : paths) {
         totalCount += path.getExecutionCount();
      }

      return totalCount;
   }
}
