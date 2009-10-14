package mockit;

import org.junit.*;
import org.junit.runner.*;

import mockit.integration.junit4.*;

@RunWith(JMockit.class)
public final class ExceptionPropagationTest
{
   @Mocked private Context ctx;

   public static class ServiceException extends Exception
   {
      @SuppressWarnings({"UnusedDeclaration"})
      public ServiceException(String message, ContextException ce) {}
   }

   public static class ContextException extends Exception {}

   public interface Context
   {
      boolean hasNextIncoming() throws ContextException;
   }

   public static class Listener
   {
      public void service(Context c) throws ServiceException
      {
         try {
            while (c.hasNextIncoming()) {}
         }
         catch (ContextException ce) {
            throw new ServiceException(ce.getMessage(), ce);
         }
      }
   }

   @Test
   public void testService() throws Exception
   {
      new Expectations()
      {
         {
            ctx.hasNextIncoming(); returns(false);
         }
      };

      Listener l = new Listener();
      l.service(ctx);
   }

   @Test(expected = ServiceException.class)
   public void testServiceException() throws Exception
   {
      new Expectations()
      {
         {
            ctx.hasNextIncoming(); throwsException(new ContextException());
         }
      };

      Listener l = new Listener();
      l.service(ctx);
   }

   @Test(expected = ServiceException.class)
   public void testServiceExceptionMsg() throws Exception
   {
      new Expectations()
      {
         @Mocked("getMessage") // be careful to NOT mock everything in Exception and Throwable!
         ContextException ce;

         {
            ctx.hasNextIncoming(); throwsException(ce);

            // Note: this is done here only to prove it works; if the objective of the test were
            // to verify that the wrapper exception (ServiceException) contains as its message the
            // original message from the wrapped exception (ContextException), then the test should
            // be written differently (using a try...catch and assertEquals(...) instead of mocking
            // the "getMessage()" method, which belongs to the Throwable base class).
            ce.getMessage(); returns("foo");
         }
      };

      Listener l = new Listener();
      l.service(ctx);
   }
}
