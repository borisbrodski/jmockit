/*
 * JMockit Expectations & Verifications
 * Copyright (c) 2006-2010 Rogério Liesenfeld
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