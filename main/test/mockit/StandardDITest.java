/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import javax.inject.*;

import org.junit.*;
import static org.junit.Assert.*;

public final class StandardDITest
{
   public static class TestedClass
   {
      @Inject static Runnable globalAction;

      private final Collaborator collaborator;
      @Inject private Collaborator collaborator1;
      Collaborator collaborator2;
      @Inject int someValue = 123; // will get assigned even if not null
      @Inject private int anotherValue;

      @Inject public TestedClass(Collaborator collaborator) { this.collaborator = collaborator; }

      @SuppressWarnings("UnusedParameters")
      public TestedClass(Collaborator collaborator, int anotherValue) { throw new RuntimeException("Must not occur"); }
   }

   interface Collaborator {}

   @Tested TestedClass tested1;
   @Injectable Collaborator collaborator; // for constructor injection

   static final class TestedClassWithNoAnnotatedConstructor
   {
      @Inject int value;
      @Inject String aText;
      String anotherText;
   }

   @Tested TestedClassWithNoAnnotatedConstructor tested2;

   public static class TestedClassWithInjectOnConstructorOnly
   {
      String name;
      @Inject public TestedClassWithInjectOnConstructorOnly() {}
   }

   @Tested TestedClassWithInjectOnConstructorOnly tested3;

   @Test
   public void invokeInjectAnnotatedConstructorOnly(@Injectable("45") int someValue)
   {
      assertSame(collaborator, tested1.collaborator);
      assertNull(tested1.collaborator1);
      assertNull(tested1.collaborator2);
      assertEquals(45, tested1.someValue);
      assertEquals(0, tested1.anotherValue);

      assertEquals(45, tested2.value);
   }

   @Test
   public void assignInjectAnnotatedFieldsWhileIgnoringNonAnnotatedOnes(
      @Injectable Collaborator collaborator2, @Injectable Collaborator collaborator1,
      @Injectable("45") int anotherValue, @Injectable("67") int notToBeUsed)
   {
      assertSame(collaborator, tested1.collaborator);
      assertSame(collaborator1, tested1.collaborator1);
      assertNull(tested1.collaborator2);
      assertEquals(123, tested1.someValue);
      assertEquals(45, tested1.anotherValue);

      assertEquals(45, tested2.value);
   }

   @Test
   public void assignAnnotatedFieldEvenIfTestedClassHasNoAnnotatedConstructor(@Injectable("123") int value)
   {
      assertEquals(123, tested2.value);
   }

   @Test
   public void assignAnnotatedStaticFieldDuringFieldInjection(@Injectable Runnable action)
   {
      assertSame(action, TestedClass.globalAction);
      assertEquals(0, tested2.value);
   }

   @Test
   public void onlyConsiderAnnotatedFieldsForInjection(@Injectable("Abc") String text1, @Injectable("XY") String text2)
   {
      assertEquals(text1, tested2.aText);
      assertNull(tested2.anotherText);
      assertNull(tested3.name);
   }
}
