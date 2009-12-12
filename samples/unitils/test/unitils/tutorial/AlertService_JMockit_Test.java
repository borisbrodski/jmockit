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

import static org.junit.Assert.assertEquals;

public final class AlertService_JMockit_Test
{
   private AlertService alertService;
   private Message alert1;
   private Message alert2;
   private List<Message> alerts;

   @Mocked SchedulerService mockSchedulerService;
   @Mocked MessageService mockMessageService;

   @Before
   public void init()
   {
      alertService = new AlertService(mockSchedulerService, mockMessageService);
      alert1 = new Alert();
      alert2 = new Alert();
      alerts = Arrays.asList(alert1, alert2);
   }

   @Test
   public void sendScheduledAlerts()
   {
      new NonStrictExpectations()
      {{
         mockSchedulerService.getScheduledAlerts(null, 1, anyBoolean); result = alerts;
      }};

      alertService.sendScheduledAlerts();

      new Verifications()
      {{
         mockMessageService.sendMessage(alert2);
         mockMessageService.sendMessage(alert1);
      }};
   }

   @Test
   public void sendScheduledAlertsInProperSequence()
   {
      new NonStrictExpectations()
      {{
         mockSchedulerService.getScheduledAlerts(null, 1, anyBoolean); result = alerts;
      }};

      alertService.sendScheduledAlerts();

      new VerificationsInOrder()
      {{
         mockMessageService.sendMessage(alert1);
         mockMessageService.sendMessage(alert2);
      }};
   }

   @Test
   public void sendNothingWhenNoAlertsAvailable()
   {
      alertService.sendScheduledAlerts();

      new Verifications()
      {
         {
            mockMessageService.sendMessage((Message) any); times = 0;
         }
      };
   }

   @Test
   public void sendNothingWhenNoAlertsAvailable_usingFullVerifications()
   {
      alertService.sendScheduledAlerts();

      new FullVerifications()
      {
         {
            mockSchedulerService.getScheduledAlerts(null, anyInt, anyBoolean);
         }
      };
   }

   @Test(expected = IllegalArgumentException.class)
   public void attemptToGetScheduledAlertsWithInvalidArguments()
   {
      new Expectations()
      {
         {
            mockSchedulerService.getScheduledAlerts("123", 1, true);
            result = new IllegalArgumentException();
         }
      };

      alertService.sendScheduledAlerts();
   }

   @Test(expected = Exception.class)
   public void recordConsecutiveInvocationsToSameMethodWithSameArguments()
   {
      new Expectations()
      {
         {
            mockSchedulerService.getScheduledAlerts(null, 0, true); result = alerts;
            mockSchedulerService.getScheduledAlerts(null, 0, true); result = new Exception();
         }
      };

      assertEquals(alerts, mockSchedulerService.getScheduledAlerts(null, 0, true));
      mockSchedulerService.getScheduledAlerts(null, 0, true);
   }

   @Test
   public void specifyingCustomMockBehavior()
   {
      new NonStrictExpectations()
      {
         {
            mockSchedulerService.getScheduledAlerts("123", 1, true);
            result = new Delegate()
            {
               List<Message> getScheduledAlerts(Object arg0, int arg1, boolean arg2)
               {
                  assert arg0 == "123";
                  assert arg1 == 1;
                  assert arg2;

                  return Arrays.asList(alert2);
               }
            };
         }
      };

      alertService.sendScheduledAlerts();

      new Verifications()
      {
         {
            mockMessageService.sendMessage(alert1); times = 0;
            mockMessageService.sendMessage(alert2);
         }
      };
   }
}
