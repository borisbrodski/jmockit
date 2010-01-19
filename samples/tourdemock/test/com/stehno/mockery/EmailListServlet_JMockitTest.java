/*
 * JMockit Samples
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
package com.stehno.mockery;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.junit.*;

import mockit.*;

import com.stehno.mockery.service.*;

public final class EmailListServlet_JMockitTest
{
   EmailListServlet servlet;

   @NonStrict HttpServletRequest request;
   @Cascading HttpServletResponse response;
   @Mocked EmailListService emailListService;

   @Cascading ServletConfig servletConfig;

   @Before // TODO: add support for mock parameters in setup methods
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

   @Test(expected = IOException.class)
   public void doGetWithoutList() throws Exception
   {
      new Expectations()
      {
         {
            emailListService.getListByName(null); result = new IOException();
         }
      };

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