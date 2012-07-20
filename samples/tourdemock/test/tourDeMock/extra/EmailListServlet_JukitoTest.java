/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package tourDeMock.extra;

import java.io.*;
import java.util.*;
import javax.inject.*;
import javax.servlet.*;
import javax.servlet.http.*;
import static java.util.Arrays.*;

import org.jukito.*;
import org.mockito.*;
import tourDeMock.original.*;
import tourDeMock.original.service.*;

import org.junit.*;
import org.junit.runner.*;

import static org.mockito.Mockito.*;

@RunWith(JukitoRunner.class)
public final class EmailListServlet_JukitoTest
{
   @Inject EmailListServlet servlet;

   @Inject HttpServletRequest request;
   @Inject HttpServletResponse response;
   @Inject EmailListService service;

   @Before
   public void before(ServletConfig config, ServletContext context) throws Exception
   {
      when(config.getServletContext()).thenReturn(context);
      when(context.getAttribute(EmailListService.KEY)).thenReturn(service);

      servlet.init(config);
   }

   @Test(expected = ServletException.class)
   public void doGetWithoutList() throws Exception
   {
      when(service.getListByName(null)).thenThrow(new EmailListNotFound());

      servlet.doGet(request, response);
   }

   @Test
   public void doGetWithList() throws Exception
   {
      List<String> emails = asList("larry@stooge.com", "moe@stooge.com", "curley@stooge.com");
      when(service.getListByName(anyString())).thenReturn(emails);

      PrintWriter writer = mock(PrintWriter.class);
      when(response.getWriter()).thenReturn(writer);

      servlet.doGet(request, response);

      InOrder order = inOrder(writer, response);
      order.verify(writer).println("larry@stooge.com");
      order.verify(writer).println("moe@stooge.com");
      order.verify(writer).println("curley@stooge.com");
      order.verify(response).flushBuffer();
   }
}
