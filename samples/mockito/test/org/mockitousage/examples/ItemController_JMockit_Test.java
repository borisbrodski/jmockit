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
package org.mockitousage.examples;

import java.util.*;

import mockit.*;
import mockit.integration.junit4.*;
import static org.junit.Assert.*;
import org.junit.*;
import org.junit.runner.*;

@RunWith(JMockit.class)
public class ItemController_JMockit_Test
{
   private ItemController itemController;
   @NonStrict private ItemService itemService;
   private Map<String, Object> modelMap;

   @Before
   public void setUp()
   {
      itemController = new ItemController(itemService);
      modelMap = new HashMap<String, Object>();
   }

   @Test
   public void testViewItem() throws Exception
   {
      final Item item = new Item(1, "Item 1");

      new Expectations()
      {
         {
            itemService.getItem(item.getId()); returns(item);
         }
      };

      String view = itemController.viewItem(item.getId(), modelMap);

      assertEquals(item, modelMap.get("item"));
      assertEquals("viewItem", view);
   }

   @Test
   public void testViewItemWithItemNotFoundException() throws Exception
   {
      final ItemNotFoundException exception = new ItemNotFoundException(5);

      new Expectations()
      {
         {
            itemService.getItem(5); throwsException(exception);
         }
      };

      String view = itemController.viewItem(5, modelMap);

      assertEquals("redirect:/errorView", view);
      assertSame(exception, modelMap.get("exception"));
   }

   @Test
   public void testDeleteItem() throws Exception
   {
      String view = itemController.deleteItem(5);

      new Verifications()
      {
         {
            itemService.deleteItem(5);
         }
      };

      assertEquals("redirect:/itemList", view);
   }
}
