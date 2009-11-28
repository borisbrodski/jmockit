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

public class Node implements Serializable
{
   private static final long serialVersionUID = 7521062699264845946L;

   private final transient ThreadLocal<Boolean> reached = new ThreadLocal<Boolean>();
   public final int line;

   private Node(int line)
   {
      this.line = line;
   }

   final void setReached(Boolean reached)
   {
      this.reached.set(reached);
   }

   final boolean wasReached()
   {
      return reached.get() != null;
   }

   @Override
   public String toString()
   {
      return getClass().getSimpleName() + ':' + line;
   }

   static final class Entry extends Node
   {
      private static final long serialVersionUID = -3065417917872259568L;

      Entry(int entryLine) { super(entryLine); }
   }

   static final class Exit extends Node
   {
      private static final long serialVersionUID = -4801498566218642509L;

      Exit(int exitLine) { super(exitLine); }
   }

   static final class BasicBlock extends Node
   {
      private static final long serialVersionUID = 2637678937923952603L;

      BasicBlock(int startingLine) { super(startingLine); }
   }

   static final class Fork extends Node
   {
      private static final long serialVersionUID = -8266367107063017773L;

      Fork(int line) { super(line); }
   }

   static final class Join extends Node
   {
      private static final long serialVersionUID = -1983522899831071765L;

      Join(int joiningLine) { super(joiningLine); }
   }
}
