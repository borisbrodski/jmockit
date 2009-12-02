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

public final class MethodCoverageData implements Serializable
{
   private static final long serialVersionUID = -5073393714435522417L;

   public final String methodName;
   private List<Node> nodes;
   private int firstLine;
   private int lastLine;

   // Helper fields used during node building and path execution:
   private transient ThreadLocal<Integer> nodesReached;

   // TODO: eliminate this list in favor of a list of exit nodes?
   public transient List<Path> paths;

   public MethodCoverageData(String methodName)
   {
      this.methodName = methodName;
   }

   public void buildPaths(int lastLine, NodeBuilder nodeBuilder)
   {
      firstLine = nodeBuilder.firstLine;
      this.lastLine = lastLine;

      nodes = nodeBuilder.nodes;
      paths = new PathBuilder().buildPaths(nodes);

      nodesReached = new ThreadLocal<Integer>();
   }

   public int getFirstLineInBody()
   {
      return firstLine;
   }

   public int getLastLineInBody()
   {
      return lastLine;
   }

   public boolean isInitialDeclarationLine(int lineNo, String line)
   {
      int p = line.indexOf(methodName);

      if (p < 0) {
         return false;
      }

      int q = p + methodName.length();

      return
         line.length() > q && line.charAt(q) == '(' || lineNo == firstLine && firstLine == lastLine;
   }

   public void markNodeAsReached(int nodeIndex)
   {
      if (nodeIndex == 0) {
         clearNodes();
      }

      Node node = nodes.get(nodeIndex);
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
      for (Node node : nodes) {
         node.setReached(null);
      }

      nodesReached.set(0);
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
