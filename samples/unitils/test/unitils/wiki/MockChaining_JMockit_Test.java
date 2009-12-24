package unitils.wiki;

import org.junit.*;

import mockit.*;

import static org.junit.Assert.*;
import org.unitils.*;
import org.unitils.inject.annotation.*;
import org.unitils.mock.*;
import org.unitils.mock.Mock;
import org.unitils.mock.core.*;

public final class MockChaining_JMockit_Test
{
   final MyService myService = new MyService();

   @Test
   public void withoutChaining(final User user)
   {
      new NonStrictExpectations()
      {
         UserService userService;

         {
            userService.getUser(); result = user; // returns the user mock
            user.getName(); result = "my name";   // define behavior of user mock
         }
      };

      assertEquals("my name", myService.outputUserName());
   }

   @Test
   public void sameTestButWithChaining()
   {
      new NonStrictExpectations()
      {
         @Cascading UserService userService;

         {
            userService.getUser().getName(); result = "my name"; // automatically returns user mock
         }
      };

      assertEquals("my name", myService.outputUserName());
   }
}