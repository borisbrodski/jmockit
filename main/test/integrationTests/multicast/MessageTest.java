/*
 * JMockit
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
package integrationTests.multicast;

import java.io.*;
import java.net.*;

import org.junit.*;

import mockit.*;
import mockit.integration.junit4.*;

public final class MessageTest extends JMockitTest
{
   static final String testContents = "hello there";

   @Test
   public void sendMessageToSingleClient() throws Exception
   {
      final Client theClient = new Client("client1");
      Client[] testClient = {theClient};

      new MockUp<Socket>()
      {
         @Mock(invocations = 1)
         void $init(String host, int port)
         {
            assertEquals(theClient.getAddress(), host);
            assertTrue(port > 0);
         }

         @Mock(invocations = 1)
         public OutputStream getOutputStream()
         {
            return new ByteArrayOutputStream();
         }

         @Mock(invocations = 1)
         public InputStream getInputStream()
         {
            return new ByteArrayInputStream("reply1\nreply2\n".getBytes());
         }

         @Mock(minInvocations = 1)
         void close() {}
      };

      StatusListener listener = new MockUp<StatusListener>()
      {
         int eventIndex;

         @Mock(invocations = 1)
         void messageSent(Client toClient)
         {
            assertSame(theClient, toClient);
            assertEquals(0, eventIndex++);
         }

         @Mock(invocations = 1)
         void messageDisplayedByClient(Client client)
         {
            assertSame(theClient, client);
            assertEquals(1, eventIndex++);
         }

         @Mock(invocations = 1)
         void messageReadByClient(Client client)
         {
            assertSame(theClient, client);
            assertEquals(2, eventIndex++);
         }
      }.getMockInstance();

      new Message(testClient, testContents, listener).dispatch();

      allowSomeTimeForAllEventsToBeReceived();
   }

   // Waits a fixed time for all threads to finish - not elegant, but easy.
   private void allowSomeTimeForAllEventsToBeReceived() throws InterruptedException
   {
      Thread.sleep(100);
   }

   @Test
   public void sendMessageToTwoClients() throws Exception
   {
      Client[] testClients = {new Client("client1"), new Client("client2")};

      new MockUp<Socket>()
      {
         @Mock(invocations = 2)
         void $init(String host, int port)
         {
            assertTrue(host.startsWith("client"));
            assertTrue(port > 0);
         }

         @Mock(invocations = 2)
         public OutputStream getOutputStream()
         {
            return new ByteArrayOutputStream();
         }

         @Mock(invocations = 2)
         public InputStream getInputStream()
         {
            return new ByteArrayInputStream("reply1\nreply2\n".getBytes());
         }

         @Mock(minInvocations = 2)
         void close() {}
      };

      StatusListener listener = new MockUp<StatusListener>()
      {
         @Mock(invocations = 2)
         void messageSent(Client toClient)
         {
            assertNotNull(toClient);
         }

         @Mock(invocations = 2)
         void messageDisplayedByClient(Client client)
         {
            assertNotNull(client);
         }

         @Mock(invocations = 2)
         void messageReadByClient(Client client)
         {
            assertNotNull(client);
         }
      }.getMockInstance();

      new Message(testClients, testContents, listener).dispatch();

      allowSomeTimeForAllEventsToBeReceived();
   }

   @Test
   public void sendMessageToMultipleClients(final StatusListener listener) throws Exception
   {
      final Client[] testClients = {
         new Client("client1"), new Client("client2"), new Client("client3")
      };

      new NonStrictExpectations(testClients.length)
      {
         final Socket con = new Socket(withPrefix("client"), withAny(1));

         {
            con.getOutputStream(); returns(new ByteArrayOutputStream());
            con.getInputStream(); returns(new ByteArrayInputStream("reply1\nreply2\n".getBytes()));
            con.close();
         }
      };

      new Message(testClients, testContents, listener).dispatch();

      allowSomeTimeForAllEventsToBeReceived();

      for (final Client client : testClients) {
         new VerificationsInOrder()
         {
            {
               // TODO: try creating a "setMaxDelay(msBeforeTimeout)" method that allows a given
               // max time for the verification block to be fully satisfied.
               listener.messageSent(client);
               listener.messageDisplayedByClient(client);
               listener.messageReadByClient(client);
            }
         };
      }
   }

   @Test
   public void sendMessageToMultipleClients_minimal(final StatusListener listener) throws Exception
   {
      final Client[] testClients = {new Client("client1"), new Client("client2")};

      new NonStrictExpectations(testClients.length)
      {
         Socket con;

         {
            con.getOutputStream(); returns(new ByteArrayOutputStream());
            con.getInputStream(); returns(new ByteArrayInputStream("reply1\nreply2\n".getBytes()));
         }
      };

      new Message(testClients, testContents, listener).dispatch();

      allowSomeTimeForAllEventsToBeReceived();

      for (final Client client : testClients) {
         new VerificationsInOrder()
         {
            {
               listener.messageSent(client);
               listener.messageDisplayedByClient(client);
               listener.messageReadByClient(client);
            }
         };
      }
   }
}
