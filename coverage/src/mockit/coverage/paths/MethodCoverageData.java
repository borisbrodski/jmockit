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
      new HashMap<Label, List<Path>>();
   private final transient List<Path> activePaths = new ArrayList<Path>();

   public void handlePotentialNewBlock(Label basicBlock)
   {
      if (paths.isEmpty()) {
         Path path = new Path(basicBlock);
         paths.add(path);
         activePaths.add(path);
      }
      else {
         handleRegularInstruction(basicBlock.line);
      }
   }

   public void handleRegularInstruction(int line)
   {
      if (addNextBlockToActivePaths) {
         for (Path path : activePaths) {
            path.addNode(new Node.BasicBlock(line));
         }

         addNextBlockToActivePaths = false;
      }
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

   public void handleJumpTarget(Label basicBlock)
   {
      // Will be null for visitLabel calls preceding visitLineNumber:
      List<Path> alternatePaths = jumpTargetToAlternatePaths.get(basicBlock);

      if (alternatePaths == null) {
         return;
      }

      for (Path alternatePath : alternatePaths) {
         alternatePath.addNode(new Node.Join(basicBlock.line));

         if (activePaths.contains(alternatePath)) {
            throw new IllegalStateException("Alternate path already active");
         }
      }

      activePaths.addAll(alternatePaths);
   }

   public void handleExit(Label exitBlock)
   {
      if (activePaths.isEmpty()) {
         throw new IllegalStateException("No active paths");
      }

      for (Path path : activePaths) {
         path.addExitNode(exitBlock);
      }

      for (List<Path> alternatePaths : jumpTargetToAlternatePaths.values()) {
         alternatePaths.removeAll(activePaths);
      }

      activePaths.clear();
   }

   public void startNewExecution()
   {
      for (Path path : paths) {
         path.startExecution();
      }
   }

   public void markSubPathsAsExecuted(int line, int segment)
   {
      for (Path path : paths) {
         path.markAsExecutedIfContainsNode(line, segment);
      }
   }

   public int getExecutionCount()
   {
      int totalCount = 0;

      for (Path path : paths) {
         totalCount += path.executionCount;
      }

      return totalCount;
   }
}
