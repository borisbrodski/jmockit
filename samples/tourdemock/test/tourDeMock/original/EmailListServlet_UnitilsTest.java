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
package tourDeMock.original;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import static java.util.Arrays.*;
import org.junit.*;
import org.junit.runner.*;
import org.unitils.*;
import org.unitils.mock.*;
import tourDeMock.original.service.*;

@RunWith(UnitilsJUnit4TestClassRunner.class)
public final class EmailListServlet_UnitilsTest
{
   EmailListServlet servlet;

   Mock<HttpServletRequest> request;
   Mock<HttpServletResponse> response;
   Mock<EmailListService> emailListService;

   Mock<ServletConfig> servletConfig;
   Mock<PrintWriter> writer;

   @Before
   public void before() throws Exception
   {
      servletConfig.returns(emailListService).getServletContext().getAttribute(EmailListService.KEY);

      servlet = new EmailListServlet();
      servlet.init(servletConfig.getMock());
   }

   @Test(expected = ServletException.class)
   public void doGetWithoutList() throws Exception
   {
      emailListService.raises(new EmailListNotFound()).getListByName(null);

      servlet.doGet(request.getMock(), response.getMock());
   }

   @Test
   public void doGetWithList() throws Exception
   {
      List<String> emails = asList("larry@stooge.com", "moe@stooge.com", "curley@stooge.com");
      emailListService.returns(emails).getListByName(null);

      response.returns(writer).getWriter();

      servlet.doGet(request.getMock(), response.getMock());

      writer.assertInvokedInSequence().println("larry@stooge.com");
      writer.assertInvokedInSequence().println("moe@stooge.com");
      writer.assertInvokedInSequence().println("curley@stooge.com");
      response.assertInvokedInSequence().flushBuffer();
   }
}