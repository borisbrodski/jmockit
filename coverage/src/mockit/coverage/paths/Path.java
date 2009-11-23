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

import org.objectweb.asm2.*;

public final class Path
{
   final List<Label> basicBlocks = new LinkedList<Label>();

   Path(Label entryBlock)
   {
      addNode(entryBlock);
   }

   Path(Path sharedPrefix, Label siblingNode)
   {
      basicBlocks.addAll(sharedPrefix.basicBlocks);
      addNode(siblingNode);
   }

   void addNode(Label basicBlock)
   {
      basicBlocks.add(basicBlock);
   }

   public String getListOfSourceLocations()
   {
      StringBuilder sourceLocations = new StringBuilder();
      Label previousBlock = null;

      for (Label nextBlock : basicBlocks) {
         int line = nextBlock.line;

         if (previousBlock != null) {
            sourceLocations.append(' ');

            if (nextBlock != previousBlock.successors.successor) {
               sourceLocations.append("jump ");

            }

            if (line == 0) {
               line = previousBlock.line;
            }
         }

         sourceLocations.append(line);
         previousBlock = nextBlock;
      }

      return sourceLocations.toString();
   }
}
