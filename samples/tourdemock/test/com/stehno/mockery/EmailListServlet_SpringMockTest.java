package com.stehno.mockery;

import java.io.*;
import java.util.*;
import javax.servlet.*;

import org.junit.*;

import com.stehno.mockery.service.*;
import static java.util.Arrays.*;
import static org.junit.Assert.*;
import org.springframework.mock.web.*;

public final class EmailListServlet_SpringMockTest
{
   static final String sep = System.getProperty("line.separator");

   EmailListServlet servlet;

   MockHttpServletRequest request;
   MockHttpServletResponse response;

   @Before
   public void before() throws ServletException
   {
      MockEmailListService emailListService = new MockEmailListService();

      MockServletConfig servletConfig = new MockServletConfig();
      servletConfig.getServletContext().setAttribute(EmailListService.KEY, emailListService);

      servlet = new EmailListServlet();
      servlet.init(servletConfig);

      request = new MockHttpServletRequest();
      response = new MockHttpServletResponse();
   }

   @Test(expected = IOException.class)
   public void doGetWithoutList() throws Exception
   {
      servlet.doGet(request, response);
   }

   @Test
   public void doGetWithList() throws Exception
   {
      request.setParameter("listName", "foo");

      servlet.doGet(request, response);

      assertTrue(response.isCommitted());
      assertEquals(
         "larry@stooge.com" + sep + "moe@stooge.com" + sep + "curley@stooge.com" + sep,
         response.getContentAsString());
   }

   private static final class MockEmailListService implements EmailListService
   {
      @Override
      public List<String> getListByName(String listName) throws IOException
      {
         if (listName == null) {
            throw new IOException();
         }

         return asList("larry@stooge.com", "moe@stooge.com", "curley@stooge.com");
      }
   }
}
