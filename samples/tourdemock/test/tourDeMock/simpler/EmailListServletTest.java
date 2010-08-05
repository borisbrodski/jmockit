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
   public void doGetWithList(
      @Cascading final HttpServletResponse response, @Mocked final PrintWriter writer)
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