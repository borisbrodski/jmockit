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
