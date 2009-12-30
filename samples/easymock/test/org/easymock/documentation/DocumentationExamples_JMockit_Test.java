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
package org.easymock.documentation;

import org.junit.*;

import mockit.*;

import static org.junit.Assert.*;
import org.easymock.samples.*;

public final class DocumentationExamples_JMockit_Test
{
   @Test
   public void changingBehaviorForTheSameMethodCall(final Collaborator mock)
   {
      final String title = "Document";

      new Expectations()
      {
         {
            mock.voteForRemoval(title);
            returns(42, 42, 42); times = 3;
            // TODO: change repeats(n) so that it stores a repeatCount in the current
            // InvocationResult? may break existing tests, so I am not sure, but seems worth a try
            result = new RuntimeException();
            result = -42;
         }
      };

      assertEquals(42, mock.voteForRemoval(title));
      assertEquals(42, mock.voteForRemoval(title));
      assertEquals(42, mock.voteForRemoval(title));

      try {
         mock.voteForRemoval(title);
      }
      catch (RuntimeException e) {
         // OK
      }

      assertEquals(-42, mock.voteForRemoval(title));
   }

   @Test
   public void checkingMethodCallOrderBetweenMocksUsingExpectations(
      final MyInterface mock1, final MyInterface mock2)
   {
      new Expectations()
      {
         {
            // TODO: if two or more mock fields/parameters of the same type are declared, shouldn't
            // onInstance be assumed by default in all recorded instance method invocations?
            onInstance(mock1).a();
            onInstance(mock2).a();

            // By default, the mock instance doesn't matter, so record only for one of them:
            mock1.c(); notStrict();

            onInstance(mock2).b();
            onInstance(mock1).b();
         }
      };

      // Ordered:
      mock1.a();
      mock2.a();

      // Unordered:
      mock2.c();
      mock1.c();
      mock2.c();

      // Ordered:
      mock2.b();
      mock1.b();
   }

   @Test
   public void checkingMethodCallOrderBetweenMocksUsingVerifications(
      final MyInterface mock1, final MyInterface mock2)
   {
      // Ordered:
      mock1.a();
      mock2.a();

      // Unordered:
      mock2.c();
      mock1.c();
      mock2.c();

      // Ordered:
      mock2.b();
      mock1.b();

      new FullVerificationsInOrder()
      {
         {
            // TODO: verification doesn't consider the instance, so onInstance(mock) must be added
            mock1.a();
            mock2.a();

            mock1.c();

            mock2.b();
            mock1.b();
         }
      };
   }
}