/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package tourDeMock.simpler;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.junit.*;
import tourDeMock.simpler.service.*;

import mockit.*;

public final class EmailListServletTest
{
   @Mocked HttpServletRequest request;
   @Mocked EmailListService emailListService;

   @Test(expected = ServletException.class)
   public void doGetWithoutList() throws Exception
   {
      new NonStrictExpectations()
      {
         {
            emailListService.getListByName(null); result = new EmailListNotFound();
         }
      };

      new EmailListServlet().doGet(request, null);
   }

   @Test
   public void doGetWithList(@Cascading final HttpServletResponse response, @Mocked final PrintWriter writer)
      throws Exception
   {
      new NonStrictExpectations()
      {
         {
            emailListService.getListByName(anyString);
            returns("larry@stooge.com", "moe@stooge.com", "curley@stooge.com");
         }
      };

      new EmailListServlet().doGet(request, response);

      new VerificationsInOrder()
      {
         {
            writer.println("larry@stooge.com");
            writer.println("moe@stooge.com");
            writer.println("curley@stooge.com");
            response.flushBuffer();
         }
      };
   }
}