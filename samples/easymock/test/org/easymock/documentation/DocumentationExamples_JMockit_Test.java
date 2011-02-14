/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
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
            mock1.a();
            mock2.a();

            mock1.c(); notStrict();
            mock2.c(); notStrict();

            mock2.b();
            mock1.b();
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

      new VerificationsInOrder()
      {
         {
            mock1.a();
            mock2.a();

            mock2.b();
            mock1.b();
         }
      };

      new Verifications()
      {
         {
            mock1.c();
            mock2.c();
         }
      };
   }
}