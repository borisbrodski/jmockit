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
package org.mockitousage.examples.use;

import org.junit.*;

import mockit.*;

import static java.util.Arrays.*;

public final class ArticleManager_JMockit_Test
{
   @NonStrict ArticleCalculator mockCalculator;
   @NonStrict ArticleDatabase mockDatabase;
   ArticleManager articleManager;

   @Before
   public void setup()
   {
      articleManager = new ArticleManager(mockCalculator, mockDatabase);
   }

   @Test
   public void managerCountsArticlesAndSavesThemInTheDatabase()
   {
      new Expectations()
      {
         {
            mockCalculator.countArticles("Guardian"); result = 12;
            mockCalculator.countArticlesInPolish(anyString); result = 5;
         }
      };

      articleManager.updateArticleCounters("Guardian");

      new Verifications()
      {
         {
            mockDatabase.updateNumberOfArticles("Guardian", 12);
            mockDatabase.updateNumberOfPolishArticles("Guardian", 5);
            mockDatabase.updateNumberOfEnglishArticles("Guardian", 7);
         }
      };
   }

   @Test
   public void managerCountsArticlesUsingCalculator()
   {
      articleManager.updateArticleCounters("Guardian");

      new Verifications()
      {
         {
            mockCalculator.countArticles("Guardian");
            mockCalculator.countArticlesInPolish("Guardian");
         }
      };
   }

   @Test
   public void managerSavesArticlesInTheDatabase()
   {
      articleManager.updateArticleCounters("Guardian");

      new Verifications()
      {
         {
            mockDatabase.updateNumberOfArticles("Guardian", 0);
            mockDatabase.updateNumberOfPolishArticles("Guardian", 0);
            mockDatabase.updateNumberOfEnglishArticles("Guardian", 0);
         }
      };
   }

   @Test
   public void managerUpdatesNumberOfRelatedArticles()
   {
      final Article articleOne = new Article();
      final Article articleTwo = new Article();
      final Article articleThree = new Article();

      new Expectations()
      {
         {
            mockCalculator.countNumberOfRelatedArticles(articleOne); result = 1;
            mockCalculator.countNumberOfRelatedArticles(articleTwo); result = 12;
            mockCalculator.countNumberOfRelatedArticles(articleThree); result = 0;

            mockDatabase.getArticlesFor("Guardian");
            result = asList(articleOne, articleTwo, articleThree);
         }
      };

      articleManager.updateRelatedArticlesCounters("Guardian");

      new Verifications()
      {
         {
            mockDatabase.save(articleOne);
            mockDatabase.save(articleTwo);
            mockDatabase.save(articleThree);
         }
      };
   }

   @Test
   public void shouldPersistRecalculatedArticle()
   {
      final Article articleOne = new Article();
      final Article articleTwo = new Article();

      new Expectations()
      {
         {
            mockCalculator.countNumberOfRelatedArticles(articleOne); result = 1;
            mockCalculator.countNumberOfRelatedArticles(articleTwo); result = 12;
            mockDatabase.getArticlesFor("Guardian"); result = asList(articleOne, articleTwo);
         }
      };

      articleManager.updateRelatedArticlesCounters("Guardian");

      new VerificationsInOrder(2)
      {
         {
            mockCalculator.countNumberOfRelatedArticles((Article) withNotNull());
            mockDatabase.save(withInstanceOf(Article.class));
         }
      };
   }
}
