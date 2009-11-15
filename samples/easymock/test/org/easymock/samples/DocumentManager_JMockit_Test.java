/*
 * JMockit Samples
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
package org.easymock.samples;

import java.util.*;

import org.junit.*;

import mockit.*;

import static org.junit.Assert.*;

public final class DocumentManager_JMockit_Test
{
   private DocumentManager classUnderTest;
   @Mocked private Collaborator mock; // A mock field which will be automatically set.

   @Before
   public void setup()
   {
      classUnderTest = new DocumentManager();
      classUnderTest.addListener(mock);
   }

   @Test
   public void removeNonExistingDocument()
   {
      assertTrue(classUnderTest.removeDocument("Does not exist"));

      // Verify there were no uses of the collaborator.
      new FullVerifications() {};
   }

   @Test
   public void addDocument()
   {
      new Expectations()
      {
         {
            mock.documentAdded("New Document");
         }
      };

      classUnderTest.addDocument("New Document", new byte[0]);
   }

   @Test
   public void addAndChangeDocument()
   {
      new Expectations()
      {
         {
            mock.documentAdded("Document");
            mock.documentChanged("Document"); repeats(3);
         }
      };

      classUnderTest.addDocument("Document", new byte[0]);
      classUnderTest.addDocument("Document", new byte[0]);
      classUnderTest.addDocument("Document", new byte[0]);
      classUnderTest.addDocument("Document", new byte[0]);
   }

   @Test
   public void voteForRemoval()
   {
      new Expectations()
      {
         {
            // Expect document addition.
            mock.documentAdded("Document");
            // Expect to be asked to vote, and vote for it.
            mock.voteForRemoval("Document"); returns(42);
            // Expect document removal.
            mock.documentRemoved("Document");
         }
      };

      classUnderTest.addDocument("Document", new byte[0]);
      assertTrue(classUnderTest.removeDocument("Document"));
   }

   @Test
   public void voteAgainstRemoval()
   {
      new Expectations()
      {
         {
            // Expect document addition.
            mock.documentAdded("Document");
            // Expect to be asked to vote, and vote against it.
            mock.voteForRemoval("Document"); returns(-42);
            // Document removal is *not* expected.
         }
      };

      classUnderTest.addDocument("Document", new byte[0]);
      assertFalse(classUnderTest.removeDocument("Document"));
   }

   @Test
   public void voteForRemovals()
   {
      new Expectations()
      {
         {
            mock.documentAdded("Document 1");
            mock.documentAdded("Document 2");
            String[] documents = {"Document 1", "Document 2"};
            mock.voteForRemovals(withEqual(documents)); returns(42);
            mock.documentRemoved("Document 1");
            mock.documentRemoved("Document 2");
         }
      };

      classUnderTest.addDocument("Document 1", new byte[0]);
      classUnderTest.addDocument("Document 2", new byte[0]);
      assertTrue(classUnderTest.removeDocuments("Document 1", "Document 2"));
   }

   @Test
   public void voteAgainstRemovals()
   {
      new Expectations()
      {
         {
            mock.documentAdded("Document 1");
            mock.documentAdded("Document 2");
            String[] documents = {"Document 1", "Document 2"};
            mock.voteForRemovals(withEqual(documents)); returns(-42);
         }
      };

      classUnderTest.addDocument("Document 1", new byte[0]);
      classUnderTest.addDocument("Document 2", new byte[0]);
      assertFalse(classUnderTest.removeDocuments("Document 1", "Document 2"));
   }

   @Test
   public void delegateMethodWhichProducesResultBasedOnCustomLogic(final List<String> l)
   {
      new Expectations()
      {
         {
            l.remove(10);
            returns(new Delegate()
            {
               String remove(int index) { return String.valueOf(index); }
            });
         }
      };

      assertEquals("10", l.remove(10));
   }
}
