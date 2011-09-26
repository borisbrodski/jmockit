/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.*;

import static org.junit.Assert.*;
import org.junit.*;

@SuppressWarnings({"deprecation"})
public final class DynamicPartialMockingTest
{
   @SuppressWarnings({"UnusedDeclaration"})
   @Deprecated
   static class Collaborator
   {
      @Deprecated
      protected final int value;

      Collaborator() { value = -1; }
      @Deprecated Collaborator(@Deprecated int value) { this.value = value; }

      final int getValue() { return value; }
      final boolean simpleOperation(int a, String b, Date c) { return true; }
      static void doSomething(boolean b, String s) { throw new IllegalStateException(); }

      @Ignore("test")
      boolean methodWhichCallsAnotherInTheSameClass()
      {
         return simpleOperation(1, "internal", null);
      }
      
      String overridableMethod() { return "base"; }
      @Deprecated native void nativeMethod();
   }

   interface Dependency
   {
      boolean doSomething();
      List<?> doSomethingElse(int n);
   }

   @Test
   public void dynamicallyMockAClass()
   {
      final Collaborator toBeMocked = new Collaborator();

      new Expectations(Collaborator.class) {{
         toBeMocked.getValue(); result = 123;
      }};

      // Not mocked:
      Collaborator collaborator = new Collaborator();
      assertEquals(-1, collaborator.value);
      assertTrue(collaborator.simpleOperation(1, "b", null));
      assertEquals(45, new Collaborator(45).value);

      // Mocked:
      assertEquals(123, collaborator.getValue());
   }

   @Test
   public void dynamicallyMockJREClass() throws Exception
   {
      new Expectations(ByteArrayOutputStream.class) {{
         new ByteArrayOutputStream().size(); result = 123;
      }};

      // Mocked:
      ByteArrayOutputStream collaborator = new ByteArrayOutputStream();
      assertNull(Deencapsulation.getField(collaborator, "buf"));
      assertEquals(123, collaborator.size());

      // Not mocked:
      ByteArrayOutputStream buf = new ByteArrayOutputStream(200);
      buf.write(65);
      assertEquals("A", buf.toString("UTF-8"));
   }

   @Test
   public void dynamicallyMockAMockedClass(@Mocked final Collaborator mock)
   {
      assertEquals(0, mock.value);

      new Expectations(mock)
      {
         {
            mock.getValue(); result = 123;
         }
      };

      // Mocked:
      assertEquals(123, mock.getValue());

      // Not mocked:
      Collaborator collaborator = new Collaborator();
      assertEquals(-1, collaborator.value);
      assertTrue(collaborator.simpleOperation(1, "b", null));
      assertEquals(45, new Collaborator(45).value);
   }

   @Test
   public void dynamicallyMockAnInstance()
   {
      final Collaborator collaborator = new Collaborator();

      new Expectations(collaborator)
      {
         {
            collaborator.getValue(); result = 123;
         }
      };

      // Mocked:
      assertEquals(123, collaborator.getValue());

      // Not mocked:
      assertTrue(collaborator.simpleOperation(1, "b", null));
      assertEquals(45, new Collaborator(45).value);
      assertEquals(-1, new Collaborator().value);
   }

   @Test(expected = AssertionError.class)
   public void expectTwoInvocationsOnStrictDynamicMockButReplayOnce()
   {
      final Collaborator collaborator = new Collaborator();

      new Expectations(collaborator)
      {
         {
            collaborator.getValue(); times = 2;
         }
      };

      assertEquals(0, collaborator.getValue());
   }

   @Test
   public void expectOneInvocationOnStrictDynamicMockButReplayTwice()
   {
      final Collaborator collaborator = new Collaborator(1);

      new Expectations(collaborator)
      {
         {
            collaborator.methodWhichCallsAnotherInTheSameClass(); result = false;
         }
      };

      // Mocked:
      assertFalse(collaborator.methodWhichCallsAnotherInTheSameClass());

      // No longer mocked, since it's strict:
      assertTrue(collaborator.methodWhichCallsAnotherInTheSameClass());
   }

   @Test
   public void expectTwoInvocationsOnStrictDynamicMockButReplayMoreTimes()
   {
      final Collaborator collaborator = new Collaborator(1);

      new Expectations(collaborator)
      {
         {
            collaborator.getValue(); times = 2;
         }
      };

      // Mocked:
      assertEquals(0, collaborator.getValue());
      assertEquals(0, collaborator.getValue());

      // No longer mocked, since it's strict and all expected invocations were already replayed:
      assertEquals(1, collaborator.getValue());
   }

   @Test(expected = AssertionError.class)
   public void expectTwoInvocationsOnNonStrictDynamicMockButReplayOnce()
   {
      final Collaborator collaborator = new Collaborator();

      new NonStrictExpectations(collaborator)
      {
         {
            collaborator.getValue(); times = 2;
         }
      };

      assertEquals(0, collaborator.getValue());
   }

   @Test(expected = AssertionError.class)
   public void expectOneInvocationOnNonStrictDynamicMockButReplayTwice()
   {
      final Collaborator collaborator = new Collaborator(1);

      new NonStrictExpectations(collaborator)
      {
         {
            collaborator.getValue(); times = 1;
         }
      };

      // Mocked:
      assertEquals(0, collaborator.getValue());

      // Still mocked because it's non-strict:
      assertEquals(0, collaborator.getValue());
   }

   @Test
   public void dynamicallyMockAnInstanceWithNonStrictExpectations()
   {
      final Collaborator collaborator = new Collaborator(2);

      new NonStrictExpectations(collaborator)
      {
         {
            collaborator.simpleOperation(1, "", null); result = false;
            Collaborator.doSomething(anyBoolean, "test");
         }
      };

      // Mocked:
      assertFalse(collaborator.simpleOperation(1, "", null));
      Collaborator.doSomething(true, "test");

      // Not mocked:
      assertEquals(2, collaborator.getValue());
      assertEquals(45, new Collaborator(45).value);
      assertEquals(-1, new Collaborator().value);

      try {
         Collaborator.doSomething(false, null);
         fail();
      }
      catch (IllegalStateException ignore) {}

      new Verifications()
      {
         {
            Collaborator.doSomething(anyBoolean, "test");
            collaborator.getValue(); times = 1;
            new Collaborator(45);
         }
      };
   }

   @Test
   public void mockMethodInSameClass()
   {
      final Collaborator collaborator = new Collaborator();

      new NonStrictExpectations(collaborator)
      {
         {
            collaborator.simpleOperation(1, anyString, null); result = false;
         }
      };

      assertFalse(collaborator.methodWhichCallsAnotherInTheSameClass());
      assertTrue(collaborator.simpleOperation(2, "", null));
      assertFalse(collaborator.simpleOperation(1, "", null));
   }

   static final class SubCollaborator extends Collaborator
   {
      SubCollaborator() { this(1); }
      SubCollaborator(int value) { super(value); }

      @Override
      String overridableMethod() { return super.overridableMethod() + " overridden"; }

      String format() { return String.valueOf(value); }
      static void causeFailure() { throw new RuntimeException(); }
   }

   @Test
   public void dynamicallyMockASubCollaboratorInstance()
   {
      final SubCollaborator collaborator = new SubCollaborator();

      new NonStrictExpectations(collaborator) {{
         collaborator.getValue(); result = 5;
         collaborator.format(); result = "test";
         SubCollaborator.causeFailure();
      }};

      // Mocked:
      assertEquals(5, collaborator.getValue());
      SubCollaborator.causeFailure();

      // Not mocked:
      assertTrue(collaborator.simpleOperation(0, null, null)); // not recorded
      assertEquals("1", new SubCollaborator().format()); // was recorded but on a different instance

      try {
         Collaborator.doSomething(true, null); // not recorded
         fail();
      }
      catch (IllegalStateException ignore) {}
   }

   @Test
   public void dynamicallyMockClassHierarchyForSpecifiedSubclass()
   {
      final SubCollaborator collaborator = new SubCollaborator();

      new NonStrictExpectations(SubCollaborator.class) {{
         collaborator.getValue(); result = 123;
         collaborator.format(); result = "test";
      }};

      // Mocked:
      assertEquals("test", collaborator.format());
      assertEquals(123, collaborator.getValue());

      // Not mocked:
      assertTrue(collaborator.simpleOperation(0, null, null));

      // Mocked sub-constructor/not mocked base constructor:
      assertEquals(-1, new SubCollaborator().value);

      new VerificationsInOrder() {{
         collaborator.format();
         new SubCollaborator();
      }};
   }

   @Test
   public void mockTheBaseMethodWhileExercisingTheOverride()
   {
      final Collaborator collaborator = new Collaborator();
      
      new Expectations(Collaborator.class)
      {
         {
            collaborator.overridableMethod(); result = "";
            collaborator.overridableMethod(); result = "mocked";
         }
      };

      assertEquals("", collaborator.overridableMethod());
      assertEquals("mocked overridden", new SubCollaborator().overridableMethod());
   }

   @Test
   public void dynamicallyMockAnAnonymousClassInstanceThroughTheImplementedInterface()
   {
      final Collaborator collaborator = new Collaborator();

      final Dependency dependency = new Dependency()
      {
         public boolean doSomething() { return false; }
         public List<?> doSomethingElse(int n) { return null; }
      };
      
      new NonStrictExpectations(collaborator, dependency)
      {
         {
            collaborator.getValue(); result = 5;
            dependency.doSomething(); result = true;
         }
      };

      // Mocked:
      assertEquals(5, collaborator.getValue());
      assertTrue(dependency.doSomething());

      // Not mocked:
      assertTrue(collaborator.simpleOperation(0, null, null));
      assertNull(dependency.doSomethingElse(3));

      new FullVerifications()
      {
         {
            dependency.doSomething();
            collaborator.getValue();
            dependency.doSomethingElse(anyInt);
            collaborator.simpleOperation(0, null, null);
         }
      };
   }

   @Test
   public void dynamicallyMockInstanceOfJREClass()
   {
      final List<String> list = new LinkedList<String>();
      @SuppressWarnings({"UseOfObsoleteCollectionType"}) List<String> anotherList = new Vector<String>();

      new NonStrictExpectations(list, anotherList)
      {
         {
            list.get(1); result = "an item";
            list.size(); result = 2;
         }
      };

      // Use mocked methods:
      assertEquals(2, list.size());
      assertEquals("an item", list.get(1));

      // Use unmocked methods:
      assertTrue(list.add("another"));
      assertEquals("another", list.remove(0));

      anotherList.add("one");
      assertEquals("one", anotherList.get(0));
      assertEquals(1, anotherList.size());
   }

   public interface AnotherInterface {}

   @Test
   public void attemptToUseDynamicMockingForInvalidTypes(AnotherInterface mockedInterface)
   {
      assertInvalidTypeForDynamicMocking(Dependency.class);
      assertInvalidTypeForDynamicMocking(Test.class);
      assertInvalidTypeForDynamicMocking(int[].class);
      assertInvalidTypeForDynamicMocking(new String[1]);
      assertInvalidTypeForDynamicMocking(char.class);
      assertInvalidTypeForDynamicMocking(123);
      assertInvalidTypeForDynamicMocking(Boolean.class);
      assertInvalidTypeForDynamicMocking(true);
      assertInvalidTypeForDynamicMocking(2.5);
      assertInvalidTypeForDynamicMocking(Mockit.newEmptyProxy(Dependency.class));
      assertInvalidTypeForDynamicMocking(mockedInterface);
   }

   private void assertInvalidTypeForDynamicMocking(Object classOrObject)
   {
      try {
         new Expectations(classOrObject) {};
         fail();
      }
      catch (IllegalArgumentException ignore) {}
   }

   @Test
   public void dynamicPartialMockingWithExactArgumentMatching()
   {
      final Collaborator collaborator = new Collaborator();

      new NonStrictExpectations(collaborator)
      {{
         collaborator.simpleOperation(1, "s", null); result = false;
      }};

      assertFalse(collaborator.simpleOperation(1, "s", null));
      assertTrue(collaborator.simpleOperation(2, "s", null));
      assertTrue(collaborator.simpleOperation(1, "S", null));
      assertTrue(collaborator.simpleOperation(1, "s", new Date()));
      assertTrue(collaborator.simpleOperation(1, null, new Date()));
      assertFalse(collaborator.simpleOperation(1, "s", null));

      new FullVerifications()
      {
         {
            collaborator.simpleOperation(anyInt, null, null);
         }
      };
   }

   @Test
   public void dynamicPartialMockingWithFlexibleArgumentMatching(final Collaborator mock)
   {
      new NonStrictExpectations(mock) {{
         mock.simpleOperation(anyInt, withPrefix("s"), null); result = false;
      }};

      assertFalse(mock.simpleOperation(1, "sSs", null));
      assertTrue(mock.simpleOperation(2, " s", null));
      assertTrue(mock.simpleOperation(1, "S", null));
      assertFalse(mock.simpleOperation(-1, "s", new Date()));
      assertTrue(mock.simpleOperation(1, null, null));
      assertFalse(mock.simpleOperation(0, "string", null));

      Collaborator collaborator = new Collaborator();
      assertTrue(collaborator.simpleOperation(1, "sSs", null));
      assertTrue(collaborator.simpleOperation(-1, null, new Date()));
   }

   @Test
   public void dynamicPartialMockingWithInstanceSpecificMatching()
   {
      final Collaborator mock = new Collaborator();

      new NonStrictExpectations(mock) {{
         mock.getValue(); result = 3;
      }};

      assertEquals(3, mock.getValue());
      final Collaborator collaborator2 = new Collaborator(4);
      assertEquals(4, collaborator2.getValue());

      new FullVerificationsInOrder() {{
         mock.getValue(); times = 1;
         collaborator2.getValue(); times = 1;
      }};
   }

   @Test
   public void dynamicPartialMockingWithInstanceSpecificMatchingOnTwoInstancesOfSameClass()
   {
      final Collaborator mock1 = new Collaborator();
      final Collaborator mock2 = new Collaborator();

      new NonStrictExpectations(mock1, mock2) {{
         mock1.getValue(); result = 1;
         mock2.getValue(); result = 2;
      }};

      assertEquals(2, mock2.getValue());
      assertEquals(1, mock1.getValue());

      new FullVerifications() {{
         mock1.getValue(); times = 1;
         mock2.getValue(); times = 1;
      }};
   }

   @Test
   public void methodWithNoRecordedExpectationCalledTwiceDuringReplay()
   {
      final Collaborator collaborator = new Collaborator(123);

      new NonStrictExpectations(collaborator) {};

      assertEquals(123, collaborator.getValue());
      assertEquals(123, collaborator.getValue());

      new FullVerifications()
      {
         {
            collaborator.getValue(); times = 2;
         }
      };
   }

   static final class TaskWithConsoleInput
   {
      boolean finished;

      void doIt()
      {
         int input = '\0';

         while (input != 'A') {
            try {
               input = System.in.read();
            }
            catch (IOException e) {
               throw new RuntimeException(e);
            }

            if (input == 'Z') {
               finished = true;
               break;
            }
         }
      }
   }

   private boolean runTaskWithTimeout(long timeoutInMillis) throws InterruptedException, ExecutionException
   {
      final TaskWithConsoleInput task = new TaskWithConsoleInput();
      Runnable asynchronousTask = new Runnable()
      {
         public void run() { task.doIt(); }
      };
      ExecutorService executor = Executors.newSingleThreadExecutor();

      while (!task.finished) {
         Future<?> worker = executor.submit(asynchronousTask);

         try {
            worker.get(timeoutInMillis, TimeUnit.MILLISECONDS);
         }
         catch (TimeoutException ignore) {
            executor.shutdownNow();
            return false;
         }
      }

      return true;
   }

   @Test
   public void taskWithConsoleInputTerminatingNormally() throws Exception
   {
      new Expectations(System.in)
      {
         {
            System.in.read(); returns((int) 'A', (int) 'x', (int) 'Z');
         }
      };

      assertTrue(runTaskWithTimeout(5000));
   }

   @Test
   public void taskWithConsoleInputTerminatingOnTimeout() throws Exception
   {
      new Expectations(System.in)
      {
         {
            System.in.read();
            result = new Delegate()
            {
               void takeTooLong() throws InterruptedException { Thread.sleep(5000); }
            };
         }
      };

      assertFalse("no timeout", runTaskWithTimeout(10));
   }

   static class ClassWithStaticInitializer
   {
      static boolean initialized = true;
      static int doSomething() { return initialized ? 1 : -1; }
   }

   @Test
   public void doNotStubOutStaticInitializersWhenDynamicallyMockingAClass()
   {
      new Expectations(ClassWithStaticInitializer.class)
      {
         {
            ClassWithStaticInitializer.doSomething(); result = 2;
         }
      };

      assertEquals(2, ClassWithStaticInitializer.doSomething());
      assertTrue(ClassWithStaticInitializer.initialized);
   }

   static final class ClassWithNative
   {
      int doSomething() { return nativeMethod(); }
      private native int nativeMethod();
   }

   @Test(expected = UnsatisfiedLinkError.class)
   public void attemptToPartiallyMockNativeMethod()
   {
      final ClassWithNative mock = new ClassWithNative();

      new Expectations(mock) {{
         // The native method is ignored when using dynamic mocking, so this actually tries to execute the real method,
         // failing since there is no native implementation.
         mock.nativeMethod();
      }};
   }

   @Test // with FileIO compiled with "target 1.1", this produced a VerifyError
   public void mockClassCompiledForJava11() throws Exception
   {
      final FileIO f = new FileIO();

      new Expectations(f) {{
         f.writeToFile("test");
      }};

      f.writeToFile("test");
   }

   static class Base { Base(boolean b) { if (!b) throw new IllegalAccessError(); } }
   static class Derived extends Base { Derived() { super(true); } }

   @Test(expected = IllegalAccessError.class)
   public void mockConstructorsInClassHierarchyWithMockedCallToSuperWhichFails()
   {
      new Expectations(Derived.class) {};

      // No constructor invocations were recorded: tries to execute real implementations,
      // but constructor in subclass now calls "super(false)", which fails.
      new Derived();
   }

   @Test
   public void mockedClassWithAnnotatedElements() throws Exception
   {
      new Expectations(Collaborator.class) {};

      Collaborator mock = new Collaborator(123);
      Class<?> mockedClass = mock.getClass();

      assertTrue(mockedClass.isAnnotationPresent(Deprecated.class));
      assertTrue(mockedClass.getDeclaredField("value").isAnnotationPresent(Deprecated.class));

      Constructor<?> mockedConstructor = mockedClass.getDeclaredConstructor(int.class);
      assertTrue(mockedConstructor.isAnnotationPresent(Deprecated.class));
      assertTrue(mockedConstructor.getParameterAnnotations()[0][0] instanceof Deprecated);

      Method mockedMethod = mockedClass.getDeclaredMethod("methodWhichCallsAnotherInTheSameClass");
      Ignore ignore = mockedMethod.getAnnotation(Ignore.class);
      assertNotNull(ignore);
      assertEquals("test", ignore.value());

      assertTrue(mockedClass.getDeclaredMethod("nativeMethod").isAnnotationPresent(Deprecated.class));
   }
}
