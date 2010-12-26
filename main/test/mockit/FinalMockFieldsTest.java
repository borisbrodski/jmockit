/*
 * JMockit Expectations
 * Copyright (c) 2006-2010 Rogério Liesenfeld
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
package mockit;

import java.io.*;

import org.junit.*;
import static org.testng.AssertJUnit.*;

public final class FinalMockFieldsTest
{
   static final class Collaborator
   {
      Collaborator() {}
      Collaborator(boolean b) { if (!b) throw new IllegalArgumentException(); }
      int getValue() { return -1; }
      void doSomething() {}
      static int doSomethingStatic() { return -2; }
   }

   static final class AnotherCollaborator
   {
      int getValue() { return -1; }
      void doSomething() {}
   }

   @Injectable final Collaborator mock = new Collaborator();
   @NonStrict final AnotherCollaborator mock2 = new AnotherCollaborator();

   @Test
   public void recordExpectationsOnInjectableFinalMockField()
   {
      new Expectations()
      {
         {
            mock.getValue(); result = 12;
            mock.doSomething(); times = 0;
         }
      };

      assertEquals(12, mock.getValue());
   }

   @Test
   public void recordExpectationsOnNonStrictFinalMockField()
   {
      AnotherCollaborator collaborator = new AnotherCollaborator();

      new Expectations()
      {
         {
            mock2.doSomething(); times = 1;
         }
      };

      collaborator.doSomething();
      assertEquals(0, collaborator.getValue());
   }

   @Test
   public void recordExpectationsOnInjectableFinalLocalMockField()
   {
      final Collaborator[] collaborators = new Collaborator[1];

      new NonStrictExpectations()
      {
         @Injectable final Collaborator mock3 = new Collaborator();

         {
            collaborators[0] = mock3;
            mock3.doSomething(); times = 1;
         }
      };

      collaborators[0].doSomething();
   }

   static final class YetAnotherCollaborator
   {
      int getValue() { return -1; }
      void doSomething() {}
   }

   @Test
   public void recordExpectationsOnNonStrictFinalLocalMockField()
   {
      YetAnotherCollaborator collaborator = new YetAnotherCollaborator();

      new Expectations()
      {
         @NonStrict final YetAnotherCollaborator mock3 = new YetAnotherCollaborator();

         {
            mock3.doSomething(); times = 1;
         }
      };

      collaborator.doSomething();
      assertEquals(0, collaborator.getValue());
   }

   @NonStrict final ProcessBuilder mockProcessBuilder = null;

   @Test
   public void recordExpectationsOnConstructorOfNonStrictFinalMockField() throws IOException
   {
      new Expectations()
      {
         {
            new ProcessBuilder("test"); times = 1;
         }
      };

      assertNull(new ProcessBuilder("test").start());
   }

   @Test
   public void recordExpectationsOnStaticMethodAndConstructorOfFinalLocalMockField()
   {
      new Expectations()
      {
         @NonStrict final Collaborator unused = null;

         {
            new Collaborator(true); result = new RuntimeException();
            Collaborator.doSomethingStatic(); result = 123;
         }
      };

      try {
         new Collaborator(true);
         fail();
      }
      catch (RuntimeException ignore) {}

      assertEquals(123, Collaborator.doSomethingStatic());
   }
}
