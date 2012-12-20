package mockit.integration.junit4;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockClass;
import mockit.Mockit;

/**
 * Base class for the JUnit rule tests, testing setup of simple mocks.
 * 
 * @author Boris Brodski
 * 
 */
public abstract class JUnit4RuleTestBase {
   protected static class MockingTarget0 {
      protected static boolean alwaysTrue() {
         return true;
      }
   }

   protected static class MockingTarget1 {
      protected static boolean alwaysTrue() {
         return true;
      }
   }

   protected static class MockingTarget2 {
      protected static boolean alwaysTrue() {
         return true;
      }
   }

   protected static class MockingTarget3 {
      protected static boolean alwaysTrue() {
         return true;
      }
   }

   protected static class MockingTarget4 {
      protected static boolean alwaysTrue() {
         return true;
      }
   }

   protected static class MockingTarget5 {
      protected static boolean alwaysTrue() {
         return true;
      }
   }

   @MockClass(realClass = MockingTarget0.class)
   protected static class MockingTarget0Mock {
      @Mock
      public static boolean alwaysTrue() {
         return false;
      }
   }

   @MockClass(realClass = MockingTarget1.class)
   protected static class MockingTarget1Mock {
      @Mock
      public static boolean alwaysTrue() {
         return false;
      }
   }

   @MockClass(realClass = MockingTarget2.class)
   protected static class MockingTarget2Mock {
      @Mock
      public static boolean alwaysTrue() {
         return false;
      }
   }

   @MockClass(realClass = MockingTarget3.class)
   protected static class MockingTarget3Mock {
      @Mock
      public static boolean alwaysTrue() {
         return false;
      }
   }

   @MockClass(realClass = MockingTarget4.class)
   protected static class MockingTarget4Mock {
      @Mock
      public static boolean alwaysTrue() {
         return false;
      }
   }

   @MockClass(realClass = MockingTarget5.class)
   protected static class MockingTarget5Mock {
      @Mock
      public static boolean alwaysTrue() {
         return false;
      }
   }

   private static final Class<?>[] MOCKING_TARGET_CLASSES = new Class<?>[] {
         MockingTarget0.class, MockingTarget1.class, MockingTarget2.class,
         MockingTarget3.class, MockingTarget4.class, MockingTarget5.class,

   };

   private static final Class<?>[] MOCKING_TARGET_MOCK_CLASSES = new Class<?>[] {
         MockingTarget0Mock.class, MockingTarget1Mock.class,
         MockingTarget2Mock.class, MockingTarget3Mock.class,
         MockingTarget4Mock.class, MockingTarget5Mock.class,

   };
   private static final int CLASS_COUNT = MOCKING_TARGET_CLASSES.length;

   protected static boolean[] mockingTargetMocked;
   protected static int[] mockingCount;
   protected static boolean[] lastMocked;

   static {
      init();
   }

   protected static void init() {
      mockingTargetMocked = new boolean[CLASS_COUNT];
      mockingCount = new int[CLASS_COUNT];
      lastMocked = new boolean[CLASS_COUNT];
   }

   protected static void mockTarget(int number, boolean mock) {
      assertTrue(callMockMethod(number));
      mockingTargetMocked[number] = mock;
      if (mock) {
         Mockit.setUpMocks(MOCKING_TARGET_MOCK_CLASSES[number]);
         assertFalse(callMockMethod(number));
         mockingCount[number]++;
      }
   }

   protected static void mockTargetEveryOtherTime(int number) {
      lastMocked[number] ^= true;
      mockTarget(number, lastMocked[number]);
   }

   protected static void verifyAllMocks() {
      for (int i = 0; i < 6; i++) {
         assertTrue("MockingTarget[" + i
               + "] mock wasn't removed after the test run", callMockMethod(i));
         assertNotEquals("MockingTarget[" + i
               + "] classs was not mocked during the test", mockingCount[i], 0);
      }
   }

   private static boolean callMockMethod(int i) {
      return (Boolean)Deencapsulation.invoke(MOCKING_TARGET_CLASSES[i], "alwaysTrue");
   }
}
