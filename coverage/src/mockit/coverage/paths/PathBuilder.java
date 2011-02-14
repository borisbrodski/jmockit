/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
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
