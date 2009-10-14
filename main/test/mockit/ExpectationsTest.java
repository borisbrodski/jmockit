/*
 * JMockit Expectations
 * Copyright (c) 2006-2009 Rogério Liesenfeld
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

import mockit.integration.junit4.*;
import static org.junit.Assert.*;
import org.junit.*;
import org.junit.runner.*;

@RunWith(JMockit.class)
public final class ExpectationsTest
{
   static class Collaborator
   {
      private int value;

      Collaborator() {}
      Collaborator(int value) { this.value = value; }

      private static String doInternal() { return "123"; }

      void provideSomeService() {}

      int getValue() { return value; }
      void setValue(int value) { this.value = value; }
   }

   @Test
   public void expectNothingWithExplicitEndRecording()
   {
      new Expectations()
      {
      }.endRecording();
   }

   @Test
   public void restoreFieldTypeRedefinitions(final Collaborator mock)
   {
      new Expectations()
      {
         {
            mock.getValue(); returns(2);
         }
      };

      assertEquals(2, mock.getValue());
      Mockit.restoreAllOriginalDefinitions();
      assertEquals(0, mock.getValue());
   }

   @Test
   public void mockInterface(final Runnable mock)
   {
      new Expectations() {{ mock.run(); }};

      mock.run();
   }

   public abstract static class AbstractCollaborator
   {
      String doSomethingConcrete() { return "test"; }
      protected abstract void doSomethingAbstract();
   }

   @Test
   public void mockAbstractClass(final AbstractCollaborator mock)
   {
      new Expectations()
      {
         {
            mock.doSomethingConcrete();
            mock.doSomethingAbstract();
         }
      };

      mock.doSomethingConcrete();
      mock.doSomethingAbstract();
   }

   @Test
   public void mockFinalField()
   {
      new Expectations()
      {
         final Collaborator mock = new Collaborator();

         {
            mock.getValue();
         }
      };

      new Collaborator().getValue();
   }

   @Test
   public void mockClassWithoutDefaultConstructor()
   {
      new Expectations()
      {
         Dummy mock;
      };
   }

   static class Dummy
   {
      @SuppressWarnings({"UnusedDeclaration"})
      Dummy(int i) {}
   }

   @Test
   public void mockSubclass()
   {
      new Expectations()
      {
         SubCollaborator mock = new SubCollaborator();
      };

      new SubCollaborator();
   }

   static final class SubCollaborator extends Collaborator
   {
   }

   @Test(expected = IllegalStateException.class)
   public void attemptToRecordExpectedReturnValueForNoCurrentInvocation()
   {
      new Expectations()
      {
         Collaborator mock;

         {
            returns(42);
         }
      };
   }

   @Test(expected = IllegalStateException.class)
   public void attemptToAddArgumentMatcherWhenNotRecording()
   {
      new Expectations()
      {
         Collaborator mock;

         {
            endRecording();
            mock.setValue(withAny(5));
         }
      };
   }

   @Test
   public void mockClassWithMethodsOfAllReturnTypesReturningDefaultValues()
   {
      ClassWithMethodsOfEveryReturnType realObject = new ClassWithMethodsOfEveryReturnType();

      new Expectations()
      {
         ClassWithMethodsOfEveryReturnType mock;

         {
            mock.getBoolean();
            mock.getChar();
            mock.getByte();
            mock.getShort();
            mock.getInt();
            mock.getLong();
            mock.getFloat();
            mock.getDouble();
            mock.getObject();
         }
      };

      assertFalse(realObject.getBoolean());
      assertEquals('\0', realObject.getChar());
      assertEquals(0, realObject.getByte());
      assertEquals(0, realObject.getShort());
      assertEquals(0, realObject.getInt());
      assertEquals(0L, realObject.getLong());
      assertEquals(0.0, realObject.getFloat(), 0.0);
      assertEquals(0.0, realObject.getDouble(), 0.0);
      assertNull(realObject.getObject());
   }

   static class ClassWithMethodsOfEveryReturnType
   {
      boolean getBoolean() { return true; }
      char getChar() { return 'A' ; }
      byte getByte() { return 1; }
      short getShort() { return 1; }
      int getInt() { return 1; }
      long getLong() { return 1; }
      float getFloat() { return 1.0F; }
      double getDouble() { return 1.0; }
      Object getObject() { return new Object(); }
   }

   @Test(expected = AssertionError.class)
   public void replayWithUnexpectedMethodInvocation(final Collaborator mock)
   {
      new Expectations()
      {
         {
            mock.getValue();
         }
      };

      mock.provideSomeService();
   }

   @Test(expected = AssertionError.class)
   public void replayWithUnexpectedStaticMethodInvocation()
   {
      new Expectations()
      {
         Collaborator mock;

         {
            mock.getValue();
         }
      };

      Collaborator.doInternal();
   }

   @Test(expected = AssertionError.class)
   public void replayWithMissingExpectedMethodInvocation()
   {
      new Expectations()
      {
         Collaborator mock;

         {
            mock.setValue(123);
         }
      };
   }

   @Test(expected = AssertionError.class)
   public void assertSatisfiedWithMissingExpectedMethodInvocation()
   {
      new Expectations()
      {
         Collaborator mock;

         {
            mock.setValue(123);
         }
      };

      Expectations.assertSatisfied();
   }

   @Test
   public void defineTwoConsecutiveReturnValues(final Collaborator mock)
   {
      new Expectations()
      {
         {
            mock.getValue(); returns(1); returns(2);
         }
      };

      assertEquals(1, mock.getValue());
      assertEquals(2, mock.getValue());
   }

   @Test // Note: this test only works under JDK 1.6+; JDK 1.5 does not support redefining natives.
   public void mockNativeMethod()
   {
      new Expectations()
      {
         @Mocked("nanoTime") final System system = null;

         {
            System.nanoTime(); returns(0L);
         }
      };

      assertEquals(0, System.nanoTime());
      Expectations.assertSatisfied();

      assertTrue(System.nanoTime() > 0);
   }
}
