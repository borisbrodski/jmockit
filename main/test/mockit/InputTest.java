/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import java.net.*;
import java.util.ArrayList;
import java.util.*;

import static java.util.Arrays.*;
import static org.junit.Assert.*;
import org.junit.*;

public final class InputTest
{
   static class Collaborator
   {
      int getInt() { return 1; }
      short getShort() { return 56; }
      int getAnotherInt(String s) { return s.hashCode(); }
      String getString() { return ""; }
      private String getAnotherString() { return ""; }
      List<Integer> getListOfIntegers() { return null; }
      List<Boolean> getListOfBooleans() { return null; }
      static Map<String, List<Long>> getMapFromStringToListOfLongs() { return null; }
      Socket getSocket() { return null; }
      void throwSocketException() throws SocketException, IllegalAccessException {}
      <E> List<E> genericMethod() { return null; }
   }

   @Mocked Collaborator mock;

   @Test
   public void specifyDefaultReturnValues()
   {
      new NonStrictExpectations()
      {
         @Input final int someIntValue = 123;
         @Input String uniqueId = "Abc5"; // fields not required to be final
         @Input final List<Integer> userIds = asList(4, 56, 278);
         @Input final List<Boolean> flags = asList(true, false, true);
         @Input final Map<String, List<Long>> complexData = new HashMap<String, List<Long>>() {{
            put("empty", new ArrayList<Long>());
            put("one", asList(1L));
            put("two", asList(10L, 20L));
         }};
         @Input Socket aSocket; // created with public no-args constructor and automatically assigned

         {
            mock.getAnotherInt("c"); result = 45;
            mock.getAnotherString(); result = null;
         }
      };

      assertEquals(123, mock.getInt());
      assertEquals(0, mock.getShort());
      assertEquals(123, mock.getAnotherInt("a"));
      assertEquals(45, mock.getAnotherInt("c"));
      assertEquals("Abc5", mock.getString());
      assertNull(mock.getAnotherString());
      assertEquals(asList(4, 56, 278), mock.getListOfIntegers());
      assertEquals(asList(true, false, true), mock.getListOfBooleans());
      assertNotNull(mock.getSocket());

      Map<String, List<Long>> data = Collaborator.getMapFromStringToListOfLongs();
      assertTrue(data.get("empty").isEmpty());
      assertTrue(data.get("one").contains(1L));
      assertEquals(2, data.get("two").size());
      assertEquals(10L, (long) data.get("two").get(0));
      assertEquals(20L, (long) data.get("two").get(1));

      assertEquals("Abc5", mock.getString());
      assertEquals(123, mock.getInt());
      assertEquals(123, mock.getAnotherInt("b"));
      assertNull(mock.getAnotherString());
   }

   @Test
   public void specifyUniqueReturnValueForMethodWithGenericReturnType()
   {
      new Expectations()
      {
         @Input final List<String> names = asList("a", "b");
      };

      List<String> names = mock.genericMethod();

      assertEquals(asList("a", "b"), names);
   }

   @Test(expected = SocketException.class)
   public void specifyDefaultExceptionToThrow() throws Exception
   {
      new Expectations()
      {
         @Input SocketException networkFailure;
      };

      mock.throwSocketException();
   }
}
