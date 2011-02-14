/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
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
   public List<Path> paths;

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
      nodesReached.set(0);
   }

   public int getFirstLineInBody()
   {
      return firstLine;
   }

   public int getLastLineInBody()
   {
      return lastLine;
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

   public void addCountsFromPreviousTestRun(MethodCoverageData previousData)
   {
      for (int i = 0; i < paths.size(); i++) {
         Path path = paths.get(i);
         Path previousPath = previousData.paths.get(i);
         path.setExecutionCount(previousPath.getExecutionCount());
      }
   }
}
