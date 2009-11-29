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

final class PathBuilder
{
   final List<Path> paths = new ArrayList<Path>();

   PathBuilder(List<Node> allNodes)
   {
      Node.Entry entryNode = (Node.Entry) allNodes.get(0);
      Path path = new Path(entryNode);
      paths.add(path);

      Node.Fork fork = entryNode.nextNode;

      if (fork == null) {
//         assert allNodes.size() == 2; // TODO: > 2 for switch and try
         Node.Exit exitNode = (Node.Exit) allNodes.get(1);
         addExitToPath(path, exitNode);
         return;
      }

      addForkToPath(path, fork);
   }

   private void addExitToPath(Path path, Node.Exit exitNode)
   {
      exitNode.paths.add(path);
      path.addNode(exitNode);
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

   private void addJoinToPath(Path path, Node.Join join)
   {
      path.addNode(join);
      addSuccessorToPath(path, join.nextNode);
   }

   private void addSuccessorToPath(Path path, Node.ConditionalSuccessor successor)
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
      else {
         addExitToPath(path, (Node.Exit) successor);
      }
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
}
