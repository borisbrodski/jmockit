package unitils.tutorial;

import java.util.*;

import org.junit.*;

import org.unitils.*;
import static org.unitils.mock.ArgumentMatchers.*;
import org.unitils.mock.*;

/**
 * Extracted from the <a href="http://unitils.org/tutorial.html">Unitils Tutorial</a>.
 */
public final class AlertServiceTest extends UnitilsJUnit4
{
   private AlertService alertService;
   private Message alert1;
   private Message alert2;
   private List<Message> alerts;

   Mock<SchedulerService> mockSchedulerService;
   Mock<MessageService> mockMessageService;

   @Before
   public void init()
   {
      alertService = new AlertService(mockSchedulerService.getMock(), mockMessageService.getMock());
      alert1 = new Alert();
      alert2 = new Alert();
      alerts = Arrays.asList(alert1, alert2);
   }

   @Test
   public void testSendScheduledAlerts()
   {
      mockSchedulerService.returns(alerts).getScheduledAlerts(null, 1, anyBoolean());

      alertService.sendScheduledAlerts();

      mockMessageService.assertInvoked().sendMessage(alert1);
      mockMessageService.assertInvoked().sendMessage(alert2);
   }
}
