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
      Collaborator parent() { return null; }
      ClassLackingNoArgsConstructor someMethod() { return new ClassLackingNoArgsConstructor(123); }
      ClassWhoseConstructorFails willAlwaysFail() { return new ClassWhoseConstructorFails(); }
   }

   static final class ClassLackingNoArgsConstructor
   {
      ClassLackingNoArgsConstructor(int i) { assert i >= 0; }
   }

   public static final class ClassWhoseConstructorFails
   {
      public ClassWhoseConstructorFails() { throw new UnsupportedOperationException(); }
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

   @Test
   public void instantiateClassWhoseNoArgsConstructorIsNotPublic()
   {
      new Expectations() {
         @Input Collaborator parent;
      };

      assertNotNull(mock.parent());
   }

   @Test(expected = InstantiationException.class)
   public void attemptToInstantiateClassLackingANoArgsConstructor() throws Throwable
   {
      new Expectations() {
         @Input ClassLackingNoArgsConstructor fail;
      };

      try {
         mock.someMethod();
         fail();
      }
      catch (RuntimeException e) {
         throw e.getCause();
      }
   }

   @Test(expected = UnsupportedOperationException.class)
   public void attemptToInstantiateClassWhoseConstructorFails()
   {
      new Expectations() {
         @Input ClassWhoseConstructorFails fail;
      };

      mock.willAlwaysFail();
   }

   public static final class DependencyAbc
   {
      int intReturningMethod() { return -1; }

      @SuppressWarnings({"RedundantThrowsDeclaration"})
      String stringReturningMethod() throws SomeCheckedException { return ""; }
   }

   public static final class SomeCheckedException extends Exception {}

   public static class UnitUnderTest
   {
      private final DependencyAbc abc = new DependencyAbc();

      public void doSomething()
      {
         int n = abc.intReturningMethod();

         for (int i = 0; i < n; i++) {
            String s;

            try {
               s = abc.stringReturningMethod();
            }
            catch (SomeCheckedException e) {
               // somehow handle the exception
               s = "Abc";
            }

            // do some other stuff
            s.toCharArray();
         }
      }
   }

   @Test
   public void doSomethingHandlesSomeCheckedException()
   {
      new Expectations() {
         DependencyAbc abc;

         @Input int iterations = 3;
         @Input SomeCheckedException onFirstIteration;
      };

      new UnitUnderTest().doSomething();
   }
}
