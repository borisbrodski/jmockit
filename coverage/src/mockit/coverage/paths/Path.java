/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.coverage.paths;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.*;

public final class Path implements Serializable
{
   private static final long serialVersionUID = 8895491272907955543L;

   final List<Node> nodes = new ArrayList<Node>(4);
   private final AtomicInteger executionCount = new AtomicInteger();

   Path(Node.Entry entryNode)
   {
      addNode(entryNode);
   }

   Path(Path sharedSubPath)
   {
      nodes.addAll(sharedSubPath.nodes);
   }

   void addNode(Node node)
   {
      nodes.add(node);
   }

   boolean countExecutionIfAllNodesWereReached(int currentNodesReached)
   {
      if (currentNodesReached != nodes.size()) {
         return false;
      }

      for (Node node : nodes) {
         if (!node.wasReached()) {
            return false;
         }
      }

      executionCount.getAndIncrement();
      return true;
   }

   public List<Node> getNodes()
   {
      return nodes;
   }

   public int getExecutionCount()
   {
      return executionCount.get();
   }

   void setExecutionCount(int count)
   {
      executionCount.set(count);
   }
}
