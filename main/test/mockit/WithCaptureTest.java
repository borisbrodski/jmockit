/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import java.util.*;

import static java.util.Arrays.asList;

import static org.junit.Assert.*;
import org.junit.*;

public final class WithCaptureTest
{
   public static class Person
   {
      private String name;
      private int age;
      public Person() {}
      public Person(String name, int age) { this.name = name; this.age = age; }
      public String getName() { return name; }
      public int getAge() { return age; }
   }

   public interface DAO<T> { void create(T t); }

   public static final class PersonDAO implements DAO<Person>
   {
      public void create(Person p) {}
      public Person create(String name, int age) { return new Person(name, age); }
      @SuppressWarnings("UnusedParameters")
      public void doSomething(String s1, boolean b, String s2, double d, float f, long l, Object o) {}
   }

   @Mocked PersonDAO dao;

   @Test
   public void captureArgumentToLocalVariable()
   {
      dao.create("Mary Jane", 10);
      dao.create("John", 25);

      new FullVerifications() {{
         int age;
         dao.create(null, age = withCapture(0));
         assertTrue(age >= 18);
      }};
   }

   @Test
   public void captureArgumentsToLocalVariables()
   {
      final Person p = new Person("John", 10);
      dao.create(p);
      dao.create("Mary Jane", 30);
      dao.doSomething("test", true, "Test", 4.5, -2.3F, 123, p);

      new Verifications() {{
         Person created;
         dao.create(created = withCapture());
         assertEquals("John", created.getName());
         assertEquals(10, created.getAge());

         String name;
         int age;
         dao.create(name = withCapture(), age = withCapture(0));
         assertEquals("Mary Jane", name);
         assertEquals(30, age);

         String s1;
         boolean b;
         double d;
         float f;
         long l;
         Object o;
         dao.doSomething(
            s1 = withCapture(), b = withCapture(false), "Test",
            d = withCapture(0.0), f = withCapture(0.0F), l = withCapture(0L), o = withCapture());
         assertEquals("test", s1);
         assertTrue(b);
         assertEquals(4.5, d, 0);
         assertEquals(-2.3, f, 0.001);
         assertEquals(123, l);
         assertSame(p, o);
      }};
   }

   @Test
   public void captureArgumentsIntoListInExpectationBlock()
   {
      final List<Person> personsCreated = new ArrayList<Person>();
      final List<String> personNames = new LinkedList<String>();
      final List<Integer> personAges = new LinkedList<Integer>();

      new NonStrictExpectations() {{
         dao.create(withCapture(personsCreated));
         dao.create(withCapture(personNames), withCapture(0, personAges));
      }};

      dao.create(new Person("John", 10));
      assertEquals(1, personsCreated.size());
      Person first = personsCreated.get(0);
      assertEquals("John", first.getName());
      assertEquals(10, first.getAge());

      dao.create(new Person("Jane", 20));
      assertEquals(2, personsCreated.size());
      Person second = personsCreated.get(1);
      assertEquals("Jane", second.getName());
      assertEquals(20, second.getAge());

      dao.create("Mary Jane", 35);
      assertEquals(1, personNames.size());
      assertEquals("Mary Jane", personNames.get(0));
      assertEquals(1, personAges.size());
      assertEquals(35, personAges.get(0).intValue());
   }

   @Test
   public void captureArgumentsIntoListInVerificationBlock()
   {
      dao.create(new Person("John", 10));
      dao.create("Mary Jane", 35);
      dao.create("", 56);
      dao.create(new Person("Jane", 20));
      dao.create("Daisy Jones", 6);

      new Verifications() {
         final List<Person> created = new ArrayList<Person>();
         final List<Integer> ages = new ArrayList<Integer>();

         {
            dao.create("", withCapture(0, ages));
            assertEquals(asList(56), ages);

            dao.create(withCapture(created));
            assertEquals(2, created.size());

            Person first = created.get(0);
            assertEquals("John", first.getName());
            assertEquals(10, first.getAge());

            Person second = created.get(1);
            assertEquals("Jane", second.getName());
            assertEquals(20, second.getAge());

            ages.clear();
            dao.create(withSubstring(" "), withCapture(0, ages)); times = 2;
            assertEquals(asList(35, 6), ages);
         }
      };
   }
}
