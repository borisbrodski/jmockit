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

public final class Path implements Serializable
{
   private static final long serialVersionUID = 8895491272907955543L;
   private final transient ThreadLocal<Integer> activeNode = new ThreadLocal<Integer>();
   
   private final List<Node> nodes = new ArrayList<Node>(4);
   private transient int indexOfLastNonExitNode;
   int executionCount;

   Path(Label entryBlock)
   {
      addNode(new Node.Entry(entryBlock.line));
   }

   Path(Path sharedPrefix)
   {
      nodes.addAll(sharedPrefix.nodes);
      addNode(new Node.Fork());
   }

   void addNode(Node node)
   {
      nodes.add(node);
   }

   void addExitNode(Label exitBlock)
   {
      int lastIndex = nodes.size() - 1;
      Node lastNode = nodes.get(lastIndex);

      if (lastNode instanceof Node.Exit) {
         throw new IllegalStateException("Duplicate exit node");
      }

      addNode(new Node.Exit(exitBlock.line));
      indexOfLastNonExitNode = lastIndex;
   }

   void startExecution()
   {
      activeNode.set(0);
   }

   void markAsExecutedIfContainsNode(int line, int segment)
   {
      Integer activeNodeIndex = activeNode.get();

      if (activeNodeIndex == null) {
         return;
      }

      Node currentNode = nodes.get(activeNodeIndex);

      if (currentNode instanceof Node.Fork) {
         activeNodeIndex++;
         currentNode = nodes.get(activeNodeIndex);
      }

      if (currentNode.line == line) {
         if (currentNode instanceof Node.Exit || activeNodeIndex == indexOfLastNonExitNode) {
            executionCount++;
            activeNodeIndex = null;
         }
         else {
            activeNodeIndex++;
         }
      }
      else {
         activeNodeIndex = null;
      }

      activeNode.set(activeNodeIndex);
   }

   public int getExecutionCount()
   {
      return executionCount;
   }

   public List<Node> getNodes()
   {
      return nodes;
   }
}
