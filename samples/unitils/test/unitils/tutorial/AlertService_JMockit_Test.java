/*
 * JMockit Samples
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
package unitils.tutorial;

import java.util.*;

import org.junit.*;

import mockit.*;
import mockit.integration.junit4.*;

public final class AlertService_JMockit_Test extends JMockitTest
{
   private AlertService alertService;
   private Message alert1;
   private Message alert2;
   private List<Message> alerts;

   @Mocked private SchedulerService mockSchedulerService;
   @Mocked private MessageService mockMessageService;

   @Before
   public void init()
   {
      alertService = new AlertService(mockSchedulerService, mockMessageService);
      alert1 = new Alert();
      alert2 = new Alert();
      alerts = Arrays.asList(alert1, alert2);
   }

   @Test
   public void testSendScheduledAlerts()
   {
      new NonStrictExpectations()
      {{
         mockSchedulerService.getScheduledAlerts(withAny(), withAny(0), withAny(false));
         returns(alerts);
      }};

      alertService.sendScheduledAlerts();

      new Verifications()
      {{
         mockMessageService.sendMessage(alert1);
         mockMessageService.sendMessage(alert2);
      }};
   }
}
