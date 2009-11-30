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
   List<Path> buildPaths(List<Node> nodes)
   {
      Node.Entry entryNode = (Node.Entry) nodes.get(0);
      Path path = new Path(entryNode);

      Node.ConditionalSuccessor nextNode = entryNode.nextNode;

      if (nextNode == null) {
         nextNode = (Node.ConditionalSuccessor) nodes.get(1);
      }

      nextNode.addToPath(path);

      return getAllPathsFromExitNodes(nodes);
   }

   private List<Path> getAllPathsFromExitNodes(List<Node> nodes)
   {
      List<Path> paths = new ArrayList<Path>();

      for (Node node : nodes) {
         if (node instanceof Node.Exit) {
            paths.addAll(((Node.Exit) node).paths);
         }
      }

      return paths;
   }
}
