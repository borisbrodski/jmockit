/*
 * Copyright (c) 2007 Mockito contributors
 * This program is made available under the terms of the MIT License.
 */
package org.mockitousage.examples.use;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import static java.util.Arrays.asList;

import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ArticleManagerTest
{
   @Mock private ArticleCalculator mockCalculator;
   @Mock private ArticleDatabase mockDatabase;
   private ArticleManager articleManager;

   @Before
   public void setup()
   {
      articleManager = new ArticleManager(mockCalculator, mockDatabase);
   }

   @Test
   public void managerCountsArticlesAndSavesThemInTheDatabase()
   {
      when(mockCalculator.countArticles("Guardian")).thenReturn(12);
      when(mockCalculator.countArticlesInPolish(anyString())).thenReturn(5);

      articleManager.updateArticleCounters("Guardian");

      verify(mockDatabase).updateNumberOfArticles("Guardian", 12);
      verify(mockDatabase).updateNumberOfPolishArticles("Guardian", 5);
      verify(mockDatabase).updateNumberOfEnglishArticles("Guardian", 7);
   }

   @Test
   public void managerCountsArticlesUsingCalculator()
   {
      articleManager.updateArticleCounters("Guardian");

      verify(mockCalculator).countArticles("Guardian");
      verify(mockCalculator).countArticlesInPolish("Guardian");
   }

   @Test
   public void managerSavesArticlesInTheDatabase()
   {
      articleManager.updateArticleCounters("Guardian");

      verify(mockDatabase).updateNumberOfArticles("Guardian", 0);
      verify(mockDatabase).updateNumberOfPolishArticles("Guardian", 0);
      verify(mockDatabase).updateNumberOfEnglishArticles("Guardian", 0);
   }

   @Test
   public void managerUpdatesNumberOfRelatedArticles()
   {
      Article articleOne = new Article();
      Article articleTwo = new Article();
      Article articleThree = new Article();

      when(mockCalculator.countNumberOfRelatedArticles(articleOne)).thenReturn(1);
      when(mockCalculator.countNumberOfRelatedArticles(articleTwo)).thenReturn(12);
      when(mockCalculator.countNumberOfRelatedArticles(articleThree)).thenReturn(0);

      when(mockDatabase.getArticlesFor("Guardian"))
         .thenReturn(asList(articleOne, articleTwo, articleThree));

      articleManager.updateRelatedArticlesCounters("Guardian");

      verify(mockDatabase).save(articleOne);
      verify(mockDatabase).save(articleTwo);
      verify(mockDatabase).save(articleThree);
   }

   @Test
   public void shouldPersistRecalculatedArticle()
   {
      Article articleOne = new Article();
      Article articleTwo = new Article();

      when(mockCalculator.countNumberOfRelatedArticles(articleOne)).thenReturn(1);
      when(mockCalculator.countNumberOfRelatedArticles(articleTwo)).thenReturn(12);
      when(mockDatabase.getArticlesFor("Guardian")).thenReturn(asList(articleOne, articleTwo));

      articleManager.updateRelatedArticlesCounters("Guardian");

      InOrder inOrder = inOrder(mockDatabase, mockCalculator);
      inOrder.verify(mockCalculator).countNumberOfRelatedArticles(isA(Article.class));
      inOrder.verify(mockDatabase, times(2)).save((Article) notNull());
   }
}
