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

import mockit.coverage.*;

import org.objectweb.asm2.*;

public final class PathCoverage
{
   private final Map<Label, BranchCoverageData> forkToSegmentData;
   private final List<Path> paths;

   public PathCoverage(FileCoverageData fileData)
   {
      forkToSegmentData = new LinkedHashMap<Label, BranchCoverageData>();
      paths = new LinkedList<Path>();

      buildMapFromInternalCFGNodesToSegmentData(fileData.getLineToLineData().entrySet());
   }

   private void buildMapFromInternalCFGNodesToSegmentData(
      Set<Map.Entry<Integer, LineCoverageData>> linesAndLineData)
   {
      for (Map.Entry<Integer, LineCoverageData> lineAndData : linesAndLineData) {
         LineCoverageData lineData = lineAndData.getValue();

         if (lineData.containsSegments()) {
            for (BranchCoverageData segmentData : lineData.getSegments()) {
               Label fork = segmentData.startLabel;
               forkToSegmentData.put(fork, segmentData);
            }
         }
      }
   }

   public List<Path> buildPaths(MethodCoverageData methodData)
   {
      Label entryBlock = methodData.entryBlock;
      Path path = new Path(entryBlock);

      paths.clear();
      paths.add(path);

      addNodesByWalkingBreadthFirst(path, methodData.exitBlocks, entryBlock.successors);

      return paths;
   }

   private void addNodesByWalkingBreadthFirst(Path path, List<Label> exitNodes, Edge firstSuccessor)
   {
      if (firstSuccessor == null) {
         return;
      }

      if (firstSuccessor.next != null) {
         Label siblingNode = firstSuccessor.next.successor;
         Path alternatePath = new Path(path, siblingNode);
         paths.add(alternatePath);
         addNodesByWalkingBreadthFirst(alternatePath, exitNodes, siblingNode.successors);
      }

      Label nextNode = firstSuccessor.successor;
      path.addNode(nextNode);

      if (!exitNodes.contains(nextNode)) {
         addNodesByWalkingBreadthFirst(path, exitNodes, nextNode.successors);
      }
   }
}
