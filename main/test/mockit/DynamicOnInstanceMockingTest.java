/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import org.junit.*;

import static org.junit.Assert.*;

public final class DynamicOnInstanceMockingTest
{
   static class Collaborator
   {
      protected int value;

      Collaborator() { value = -1; }
      Collaborator(int value) { this.value = value; }

      int getValue() { return value; }
      void setValue(int value) { this.value = value; }
   }

   static final class AnotherDependency
   {
      private String name;

      public String getName() { return name; }
      public void setName(String name) { this.name = name; }
   }

   @Test
   public void mockingOneInstanceAndMatchingInvocationsOnAnyInstance()
   {
      Collaborator collaborator1 = new Collaborator();
      Collaborator collaborator2 = new Collaborator();
      final Collaborator collaborator3 = new Collaborator();

      new NonStrictExpectations(collaborator3)
      {
         {
            collaborator3.getValue(); result = 3;
         }
      };

      assertEquals(3, collaborator1.getValue());
      assertEquals(3, collaborator2.getValue());
      assertEquals(3, collaborator3.getValue());
      assertEquals(3, new Collaborator(2).getValue());
   }

   @Test
   public void mockingTwoInstancesAndMatchingInvocationsOnEachOne()
   {
      final Collaborator collaborator1 = new Collaborator();
      final Collaborator collaborator2 = new Collaborator();

      new NonStrictExpectations(collaborator1, collaborator2)
      {
         {
            collaborator1.getValue(); result = 1;
         }
      };

      collaborator2.setValue(2);
      assertEquals(2, collaborator2.getValue());
      assertEquals(1, collaborator1.getValue());
      assertEquals(3, new Collaborator(3).getValue());
   }

   @Test
   public void mockingAClassAndMatchingInvocationsOnAnyInstance()
   {
      final Collaborator collaborator = new Collaborator();

      new NonStrictExpectations(Collaborator.class)
      {
         {
            collaborator.getValue(); result = 1;
         }
      };

      collaborator.setValue(2);
      assertEquals(1, collaborator.getValue());
      assertEquals(1, new Collaborator(2).getValue());
   }

   @Test
   public void mockingOneInstanceButRecordingOnAnother()
   {
      final Collaborator collaborator1 = new Collaborator();
      final Collaborator collaborator2 = new Collaborator();
      Collaborator collaborator3 = new Collaborator();

      new NonStrictExpectations(collaborator1)
      {
         {
            collaborator2.getValue(); result = -2;
         }
      };

      collaborator1.setValue(1);
      collaborator2.setValue(2);
      collaborator3.setValue(3);
      assertEquals(-2, collaborator1.getValue());
      assertEquals(-2, collaborator2.getValue());
      assertEquals(-2, collaborator3.getValue());
   }

   @Test
   public void mockingTwoInstancesButRecordingOnAnother()
   {
      final Collaborator collaborator1 = new Collaborator();
      final Collaborator collaborator2 = new Collaborator();
      final Collaborator collaborator3 = new Collaborator();

      new NonStrictExpectations(collaborator1, collaborator2)
      {
         {
            // Recording expectations on a mock instance other than the ones
            // passed in the constructor should be avoided, but it is valid:
            collaborator3.getValue(); result = 3;
         }
      };

      collaborator1.setValue(1);
      collaborator2.setValue(2);
      assertEquals(1, collaborator1.getValue());
      assertEquals(2, collaborator2.getValue());
      assertEquals(3, collaborator3.getValue());
   }

   @Test
   public void mockingOneInstanceAndOneClass()
   {
      final Collaborator collaborator1 = new Collaborator();
      final Collaborator collaborator2 = new Collaborator();
      Collaborator collaborator3 = new Collaborator();
      final AnotherDependency dependency = new AnotherDependency();

      new NonStrictExpectations(collaborator1, AnotherDependency.class)
      {
         {
            collaborator2.getValue(); result = -2;
            dependency.getName(); result = "name1";
         }
      };

      collaborator1.setValue(1);
      collaborator2.setValue(2);
      collaborator3.setValue(3);
      assertEquals(-2, collaborator2.getValue());
      assertEquals(-2, collaborator1.getValue());
      assertEquals(-2, collaborator3.getValue());

      dependency.setName("modified");
      assertEquals("name1", dependency.getName());

      AnotherDependency dep2 = new AnotherDependency();
      dep2.setName("another");
      assertEquals("name1", dep2.getName());
   }
}