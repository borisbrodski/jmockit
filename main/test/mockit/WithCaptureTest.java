/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import java.util.ArrayList;
import java.util.*;
import static java.util.Arrays.*;

import org.junit.*;
import static org.junit.Assert.*;

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

   @SuppressWarnings("UnusedParameters")
   public static final class PersonDAO implements DAO<Person>
   {
      public void create(Person p) {}
      public Person create(String name, int age) { return new Person(name, age); }
      public void doSomething(Integer i) {}
      public void doSomething(boolean b) {}
      public void doSomething(Number n) {}
      public void doSomething(
         String s1, boolean b, String s2, double d, float f, long l, Object o, char c, byte bt, short sh) {}
      void doSomething(String[] names, int[] ages) {}
      void doSomething(Float f1, float f2, boolean... flags) {}
      void doSomething(String name, Short age) {}
   }

   @Mocked PersonDAO dao;

   @Test
   public void captureArgumentFromLastMatchingInvocationToLocalVariable()
   {
      dao.create("Mary Jane", 10);
      dao.create("John", 25);

      new FullVerifications() {{
         int age;
         dao.create(null, age = withCapture());
         assertTrue(age >= 18);
      }};
   }

   @Test
   public void captureArgumentWhenNoInvocationIsMatched()
   {
      dao.create("Mary Jane", 10);

      new Verifications() {{
         int age;
         dao.create("", age = withCapture()); minTimes = 0;
         assertEquals(0, age);
      }};
   }

   @Test
   public void captureArgumentOfWrapperTypeToLocalVariableOfPrimitiveType()
   {
      dao.doSomething(45);

      new Verifications() {{
         int i;
         dao.doSomething(i = withCapture());
         assertEquals(45, i);
      }};
   }

   @Test
   public void captureArgumentOfReferenceTypeToLocalVariableOfPrimitiveType()
   {
      dao.doSomething(123.0F);

      new Verifications() {{
         float f;
         dao.doSomething(f = withCapture());
         assertEquals(123.0F, f, 0);
      }};
   }

   @Ignore @Test
   public void captureArgumentOfReferenceTypeToVariableOfSpecificSubtypeForSeparateInvocations()
   {
      dao.doSomething(123.0F);
      dao.doSomething(123L);
      dao.doSomething(123.0);

      new Verifications() {{
         float f;
         dao.doSomething(f = withCapture());
         assertEquals(123.0F, f, 0);

         long l;
         dao.doSomething(l = withCapture());
         assertEquals(123L, l);

         Double d;
         dao.doSomething(d = withCapture());
         assertEquals(123.0, d, 0);
      }};
   }

   @Test
   public void captureArgumentOfPrimitiveTypeToLocalVariableOfPrimitiveType()
   {
      dao.doSomething(true);

      new Verifications() {{
         boolean b;
         dao.doSomething(b = withCapture());
         assertTrue(b);
      }};
   }

   @Test
   public void captureArgumentOfPrimitiveTypeToLocalVariableOfReferenceType()
   {
      dao.doSomething(true);

      new Verifications() {{
         Boolean b;
         dao.doSomething(b = withCapture());
         assertTrue(b);
      }};
   }

   @Test
   public void captureArgumentsToLocalVariables()
   {
      final Person p = new Person("John", 10);
      dao.create(p);
      dao.create("Mary Jane", 30);
      dao.doSomething("test", true, "Test", 4.5, -2.3F, 123, p, 'g', (byte) 127, (short) -32767);

      new Verifications() {{
         Person created;
         dao.create(created = withCapture());
         assertEquals("John", created.getName());
         assertEquals(10, created.getAge());

         String name;
         int age;
         dao.create(name = withCapture(), age = withCapture());
         assertEquals("Mary Jane", name);
         assertEquals(30, age);

         String s1;
         boolean b;
         double d;
         float f;
         long l;
         Object o;
         char c;
         byte bt;
         short sh;
         dao.doSomething(
            s1 = withCapture(), b = withCapture(), "Test", d = withCapture(),
            f = withCapture(), l = withCapture(), o = withCapture(),
            c = withCapture(), bt = withCapture(), sh = withCapture());
         assertEquals("test", s1);
         assertTrue(b);
         assertEquals(4.5, d, 0);
         assertEquals(-2.3, f, 0.001);
         assertEquals(123, l);
         assertSame(p, o);
         assertEquals('g', c);
         assertEquals(127, bt);
         assertEquals(-32767, sh);
      }};
   }

   boolean boolCapture;
   static int intCapture;

   @Test
   public void captureArgumentsIntoFields()
   {
      dao.doSomething(56);
      dao.doSomething(true);
      dao.create("", 123);
      dao.doSomething(123.5);

      new Verifications() {
         Integer i;
         final Number n;

         {
            dao.doSomething(i = withCapture());
            assertEquals(56, i.intValue());

            dao.doSomething(boolCapture = withCapture());
            assertTrue(boolCapture);

            dao.doSomething(n = withCapture()); times = 1;
            assertEquals(123.5, n.doubleValue(), 0);

            dao.create(anyString, intCapture = withCapture());
            assertEquals(123, intCapture);
         }
      };
   }

   @Ignore @Test
   public void captureFirstArgumentIntoLocalFieldInTwoParameterMethod()
   {
      final String name = "Ted";
      final Short age = 15;
      dao.doSomething(name, age);

      new Verifications() {
         String nameCapture;

         {
            dao.create(nameCapture = withCapture(), age);
            assertEquals(name, nameCapture);
         }
      };
   }

   @Test
   public void captureArgumentsFromConsecutiveMatchingInvocations()
   {
      dao.doSomething((byte) 56);
      dao.doSomething(123.4F);
      dao.doSomething((short) -78);
      dao.doSomething(91);
      dao.doSomething(92);

      final String[] names1 = {"Ted"};
      final int[] ages1 = {15, 46};
      dao.doSomething(names1, ages1);

      final String[] names2 = {"Ted"};
      final int[] ages2 = {15, 46};
      dao.doSomething(names2, ages2);

      new VerificationsInOrder() {
         private final short sh;
         final Number n;
         byte bt;
         int i1;
         Integer i2;
         String[] namesCapture;
         int[] agesCapture;

         {
            dao.doSomething(bt = withCapture());
            assertEquals(56, bt);

            dao.doSomething(n = withCapture());
            assertEquals(123.4, n.floatValue(), 0.001);

            dao.doSomething(sh = withCapture());
            assertEquals(-78, sh);

            dao.doSomething(i1 = withCapture());
            assertEquals(91, i1);

            dao.doSomething(i2 = withCapture());
            assertEquals(92, i2.intValue());

//            dao.doSomething(namesCapture = withCapture(), agesCapture = withCapture());
//            assertSame(names1, namesCapture);
//            assertSame(ages1, agesCapture);

//            dao.doSomething(namesCapture = withCapture(), agesCapture = withCapture());
//            assertSame(names2, namesCapture);
//            assertSame(ages2, agesCapture);
         }
      };
   }

   @Test
   public void captureArrayArguments()
   {
      final String[] names = {"Ted", "Lisa"};
      final int[] ages = {67, 19};
      dao.doSomething(names, ages);

      new Verifications() {{
         String[] capturedNames;
         int[] capturedAges;
         dao.doSomething(capturedNames = withCapture(), capturedAges = withCapture());

         assertArrayEquals(names, capturedNames);
         assertArrayEquals(ages, capturedAges);
      }};
   }

   @Test
   public void captureVarargsParameter()
   {
      dao.doSomething(1.2F, 1.0F, true, false, true);
      dao.doSomething(0.0F, 2.0F, false, true);
      dao.doSomething(-2.0F, 3.0F);

      new VerificationsInOrder() {{
         boolean[] flags;

         dao.doSomething(anyFloat, 1.0F, flags = withCapture());
         assertEquals(3, flags.length);
         assertTrue(flags[0]);
         assertFalse(flags[1]);
         assertTrue(flags[2]);

         dao.doSomething(null, 2.0F, flags = withCapture());
         assertEquals(2, flags.length);
         assertFalse(flags[0]);
         assertTrue(flags[1]);

         dao.doSomething(withAny(0.0F), 3.0F, flags = withCapture());
         assertEquals(0, flags.length);
      }};
   }

   @Test
   public void captureArgumentsWhileMixingAnyFieldsAndLiteralValuesAndCallsToOtherMethods()
   {
      final double d = 4.5;
      final long l = 123;
      dao.doSomething("Test", true, "data", d, 12.25F, l, "testing", '9', (byte) 11, (short) 5);

      new Verifications() {{
         float f;
         String s;
         byte b;

         //noinspection ConstantMathCall
         dao.doSomething(
            null, anyBoolean, getData(), Math.abs(-d), f = withCapture(), Long.valueOf("" + l),
            s = withCapture(), Character.forDigit(9, 10), b = withCapture(), anyShort);

         assertEquals(12.25F, f, 0);
         assertEquals("testing", s);
         assertEquals(11, b);
      }};
   }

   private String getData() { return "data"; }

   @Test
   public void captureArgumentsIntoListInExpectationBlock()
   {
      final List<Person> personsCreated = new ArrayList<Person>();
      final List<String> personNames = new LinkedList<String>();
      final List<Integer> personAges = new LinkedList<Integer>();

      new NonStrictExpectations() {{
         dao.create(withCapture(personsCreated));
         dao.create(withCapture(personNames), withCapture(personAges));
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
            dao.create("", withCapture(ages));
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
            dao.create(withSubstring(" "), withCapture(ages)); times = 2;
            assertEquals(asList(35, 6), ages);
         }
      };
   }
}
