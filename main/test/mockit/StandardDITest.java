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
      private final Collaborator collaborator;
      @Inject private Collaborator collaborator1;
      Collaborator collaborator2;
      @Inject int someValue = 123; // will get assigned even if not null
      @SuppressWarnings("UnusedDeclaration") private int anotherValue;

      @Inject public TestedClass(Collaborator collaborator) { this.collaborator = collaborator; }

      @SuppressWarnings("UnusedParameters")
      public TestedClass(Collaborator collaborator, int anotherValue) { throw new RuntimeException("Must not occur"); }
   }

   interface Collaborator {}

   @Tested TestedClass tested;

   @Test
   public void invokeInjectAnnotatedConstructorOnly(@Injectable Collaborator mock, @Injectable("45") int someValue)
   {
      assertSame(mock, tested.collaborator);
      assertNull(tested.collaborator1);
      assertNull(tested.collaborator2);
      assertEquals(45, tested.someValue);
      assertEquals(0, tested.anotherValue);
   }

   @Test
   public void assignInjectAnnotatedFieldsAsWellNonAnnotatedOnes(
      @Injectable Collaborator collaborator, // for constructor injection
      @Injectable Collaborator collaborator2, @Injectable Collaborator collaborator1,
      @Injectable("45") int anotherValue, @Injectable("67") int notToBeUsed)
   {
      assertSame(collaborator, tested.collaborator);
      assertSame(collaborator1, tested.collaborator1);
      assertSame(collaborator2, tested.collaborator2);
      assertEquals(123, tested.someValue);
      assertEquals(45, tested.anotherValue);
   }
}
