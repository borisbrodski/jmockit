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
import static javax.swing.SwingUtilities.*;

public final class Message
{
   private static final int CLIENT_PORT = 8000;

   private final Client[] to;
   private final String contents;
   private final StatusListener listener;

   public Message(Client[] to, String contents, StatusListener listener)
   {
      this.to = to;
      this.contents = contents;
      this.listener = listener;
   }

   /**
    * Sends the message contents to all clients, notifying the status listener about the
    * corresponding events as they occur.
    * <p/>
    * Network communication with clients occurs asynchronously, without ever blocking the caller.
    * Status notifications are executed on the Swing EDT (Event Dispatching Thread), so that the
    * UI can be safely updated.
    */
   public void dispatch()
   {
      for (Client client : to) {
         MessageDispatcher dispatcher = new MessageDispatcher(client);
         new Thread(dispatcher).start();
      }
   }

   private final class MessageDispatcher implements Runnable
   {
      private final Client client;

      MessageDispatcher(Client client) { this.client = client; }

      public void run()
      {
         try {
            communicateWithClient();
         }
         catch (IOException e) {
            throw new RuntimeException(e);
         }
      }

      private void communicateWithClient() throws IOException
      {
         Socket connection = new Socket(client.getAddress(), CLIENT_PORT);

         try {
            sendMessage(connection.getOutputStream());
            readRequiredReceipts(connection.getInputStream());
         }
         finally {
            connection.close();
         }
      }

      private void sendMessage(OutputStream output)
      {
         new PrintWriter(output, true).println(contents);

         invokeLater(new Runnable()
         {
            public void run() { listener.messageSent(client); }
         });
      }

      private void readRequiredReceipts(InputStream input) throws IOException
      {
         BufferedReader in = new BufferedReader(new InputStreamReader(input));

         // Wait for display receipt:
         in.readLine();
         invokeLater(new Runnable()
         {
            public void run() { listener.messageDisplayedByClient(client); }
         });

         // Wait for read receipt:
         in.readLine();
         invokeLater(new Runnable()
         {
            public void run() { listener.messageReadByClient(client); }
         });
      }
   }
}