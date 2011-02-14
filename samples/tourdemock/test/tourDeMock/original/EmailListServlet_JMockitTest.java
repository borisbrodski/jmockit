/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package tourDeMock.original;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.junit.*;
import tourDeMock.original.service.*;

import mockit.*;

public final class EmailListServlet_JMockitTest
{
   EmailListServlet servlet;

   @NonStrict HttpServletRequest request;
   @Cascading HttpServletResponse response;
   @Mocked EmailListService emailListService;

   @Cascading ServletConfig servletConfig;

   @Before
   public void before() throws Exception
   {
      new Expectations()
      {
         {
            servletConfig.getServletContext().getAttribute(EmailListService.KEY);
            result = emailListService;
         }
      };

      servlet = new EmailListServlet();
      servlet.init(servletConfig);
   }

   @Test(expected = ServletException.class)
   public void doGetWithoutList() throws Exception
   {
      new Expectations()
      {{
         emailListService.getListByName(null); result = new ServletException();
      }};

      servlet.doGet(request, response);
   }

   @Test
   public void doGetWithList(final PrintWriter writer) throws Exception
   {
      new Expectations()
      {
         {
            emailListService.getListByName(anyString);
            returns("larry@stooge.com", "moe@stooge.com", "curley@stooge.com");
         }
      };

      servlet.doGet(request, response);

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