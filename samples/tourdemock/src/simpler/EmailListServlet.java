package simpler;

import java.io.*;
import java.util.*;
import javax.servlet.http.*;

import simpler.service.*;

public final class EmailListServlet extends HttpServlet
{
   @Override
   protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException
   {
      String listName = request.getParameter("listName");
      List<String> emails = new EmailListService().getListByName(listName);

      writeListOfEmailsToClient(response.getWriter(), emails);
      response.flushBuffer();
   }

   private void writeListOfEmailsToClient(PrintWriter writer, List<String> emails)
   {
      for (String email : emails) {
         writer.println(email);
      }
   }
}