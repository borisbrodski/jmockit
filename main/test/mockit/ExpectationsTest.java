/*
 * JMockit Expectations
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

import java.io.*;

import org.junit.*;

import static org.junit.Assert.*;

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

   @Test(expected = AssertionError.class)
   public void expectOnlyOneInvocationOnLocalMockedTypeButExerciseOthersDuringReplay()
   {
      Collaborator collaborator = new Collaborator();

      new Expectations()
      {
         Collaborator mock;

         {
            mock.provideSomeService();
         }
      };

      collaborator.provideSomeService();
      collaborator.setValue(1);
   }

   @Test(expected = AssertionError.class)
   public void expectOnlyOneInvocationOnTestScopedMockedTypeButExerciseOthersDuringReplay(
      final Collaborator mock)
   {
      new Expectations()
      {
         {
            mock.provideSomeService();
         }
      };

      mock.provideSomeService();
      mock.setValue(1);
   }

   @Test
   public void recordNothingOnLocalMockedTypeButExerciseItDuringReplay()
   {
      Collaborator collaborator = new Collaborator();

      new Expectations()
      {
         Collaborator mock;
      };

      collaborator.provideSomeService();
   }

   @Test
   public void recordNothingOnTestScopedMockedTypeButExerciseItDuringReplay(Collaborator mock)
   {
      new Expectations() {};

      mock.provideSomeService();
   }

   @Test(expected = AssertionError.class)
   public void expectNothingOnLocalMockedTypeButExerciseItDuringReplay()
   {
      Collaborator collaborator = new Collaborator();

      new Expectations()
      {
         Collaborator mock;

         {
            mock.provideSomeService(); times = 0;
         }
      };

      collaborator.setValue(2);
   }

   @Test(expected = AssertionError.class)
   public void expectNothingOnTestScopedMockedTypeButExerciseItDuringReplay(final Collaborator mock)
   {
      new Expectations()
      {
         {
            mock.setValue(anyInt); times = 0;
         }
      };

      mock.setValue(2);
   }

   @Test
   public void expectNothingWithExplicitEndRecording()
   {
      new Expectations()
      {
         Collaborator mock;

         {
            endRecording();
         }
      };
   }

   @Test(expected = IllegalStateException.class)
   public void expectNothingWithNoMockedTypesInScope()
   {
      new Expectations()
      {
      };
   }

   @Test
   public void restoreFieldTypeRedefinitions(final Collaborator mock)
   {
      new Expectations()
      {
         {
            mock.getValue(); result = 2;
         }
      };

      assertEquals(2, mock.getValue());
      Mockit.tearDownMocks();
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

   static final class SubCollaborator extends Collaborator
   {
      @Override
      int getValue()
      {
         return 1 + super.getValue();
      }

      int getValue(int i)
      {
         return i + super.getValue();
      }
   }

   @Test
   public void mockSubclass()
   {
      new Expectations()
      {
         final SubCollaborator mock = new SubCollaborator();

         {
            mock.provideSomeService();
            mock.getValue(); result = 1;
         }
      };

      SubCollaborator collaborator = new SubCollaborator();
      collaborator.provideSomeService();
      assertEquals(1, collaborator.getValue());
   }

   @Test
   public void mockSuperClassUsingLocalMockField()
   {
      new Expectations()
      {
         Collaborator mock;

         {
            mock.getValue(); result = 1;
            mock.getValue(); result = 2;
         }
      };

      SubCollaborator collaborator = new SubCollaborator();
      assertEquals(2, collaborator.getValue());
      assertEquals(3, collaborator.getValue(1));
   }

   @Test
   public void mockSuperClassUsingMockParameter(@NonStrict final Collaborator mock)
   {
      new Expectations()
      {
         {
            mock.getValue(); times = 2; returns(1, 2);
         }
      };

      SubCollaborator collaborator = new SubCollaborator();
      assertEquals(2, collaborator.getValue());
      assertEquals(3, collaborator.getValue(1));
   }

   @Test(expected = IllegalStateException.class)
   public void attemptToRecordExpectedReturnValueForNoCurrentInvocation()
   {
      new Expectations()
      {
         Collaborator mock;

         {
            result = 42;
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
            mock.setValue(withNotEqual(5));
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

   @Test
   public void defineTwoConsecutiveReturnValues(final Collaborator mock)
   {
      new Expectations()
      {
         {
            mock.getValue(); result = 1; result = 2;
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
         final System system = null;

         {
            System.nanoTime(); result = 0L;
         }
      };

      assertEquals(0, System.nanoTime());
   }

   @Test
   public void mockSystemGetenvMethod()
   {
      new Expectations()
      {
         System mockedSystem;

         {
            System.getenv("envVar"); result = ".";
         }
      };

      assertEquals(".", System.getenv("envVar"));
   }

   @Test
   public void mockConstructorsInJREClassHierarchies() throws Exception
   {
      new Expectations()
      {
         final FileWriter fileWriter;
         PrintWriter printWriter;

         {
            fileWriter = new FileWriter("no.file");
         }
      };

      new FileWriter("no.file");
   }

   @Test(expected = AssertionError.class)
   public void failureFromUnexpectedInvocationInAnotherThread() throws Exception
   {
      final Collaborator collaborator = new Collaborator();
      Thread t = new Thread(new Runnable()
      {
         public void run() { collaborator.provideSomeService(); }
      });

      new Expectations()
      {
         Collaborator mock;

         {
            mock.getValue();
         }
      };

      collaborator.getValue();
      t.start();
      t.join();
   }
}
