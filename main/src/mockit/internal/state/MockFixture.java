/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.state;

import java.lang.reflect.*;
import java.util.*;
import java.util.Map.*;

import mockit.internal.*;
import mockit.internal.expectations.mocking.*;

/**
 * Holds data about redefined real classes and their corresponding mock classes (if any), and
 * provides methods to add/remove such state both from this instance and from other state holders
 * with associated data.
 */
public final class MockFixture
{
   /**
    * Class names with their bytecode fixed definitions, that is, those to which the class should
    * eventually be restored.
    * <p/>
    * This map is to be used by other bytecode instrumentation tools such as JMockit Coverage,
    * which need to avoid having their bytecode redefinitions/transformations getting discarded when
    * test tear down executes and restores mocked classes.
    * <p/>
    * The modified bytecode arrays in the map allow a new redefinition for a given class to be made,
    * on top of the fixed definition.
    */
   private final Map<String, byte[]> fixedClassDefinitions = new HashMap<String, byte[]>();

   /**
    * Similar to <code>redefinedClasses</code>, but for classes modified by a ClassFileTransformer
    * such as the CaptureTransformer, and containing the pre-transform bytecode instead of the
    * modified one.
    */
   private final Map<String, byte[]> transformedClasses = new HashMap<String, byte[]>(2);

   /**
    * Real classes currently redefined in the running JVM and their current (modified) bytecodes.
    * <p/>
    * The keys in the map allow each redefined real class to be later restored to its original
    * definition at any moment, by re-reading the class file from disk.
    * <p/>
    * The modified bytecode arrays in the map allow a new redefinition for a given real class to be
    * made, on top of the current redefinition.
    */
   private final Map<Class<?>, byte[]> redefinedClasses = new HashMap<Class<?>, byte[]>(8);

   /**
    * Subset of all currently redefined classes which contain one or more native methods.
    * <p/>
    * This is needed because in order to restore such methods it is necessary (for some classes) to
    * re-register them with the JVM.
    *
    * @see #reregisterNativeMethodsForRestoredClass(Class)
    */
   private final Set<String> redefinedClassesWithNativeMethods = new HashSet<String>();

   /**
    * Maps redefined real classes to the internal name of the corresponding mock classes, when it's
    * the case.
    * <p/>
    * This allows any global state associated to a mock class to be discarded when the corresponding
    * real class is later restored to its original definition.
    */
   private final Map<Class<?>, String> realClassesToMockClasses = new HashMap<Class<?>, String>(8);

   private final Map<Class<?>, InstanceFactory> mockedTypesAndInstances = new HashMap<Class<?>, InstanceFactory>();

   // Methods to add/remove redefined classes /////////////////////////////////////////////////////////////////////////

   public void addFixedClass(String className, byte[] fixedClassfile)
   {
      fixedClassDefinitions.put(className, fixedClassfile);
   }

   public void addTransformedClass(String className, byte[] pretransformClassfile)
   {
      transformedClasses.put(className, pretransformClassfile);
   }

   public void addRedefinedClass(String mockClassInternalName, Class<?> redefinedClass, byte[] modifiedClassfile)
   {
      if (mockClassInternalName != null) {
         String previousNames = realClassesToMockClasses.put(redefinedClass, mockClassInternalName);

         if (previousNames != null) {
            realClassesToMockClasses.put(redefinedClass, previousNames + ' ' + mockClassInternalName);
         }
      }

      addRedefinedClass(redefinedClass, modifiedClassfile);

      // TODO: implement support for multiple simultaneous redefinitions for each class?
      // at least support the case where some class is stubbed out for the whole test class and
      // then mocked in one or more tests, particularly when using @Mock(invocations = n)
   }

   public void addRedefinedClass(Class<?> redefinedClass, byte[] modifiedClassfile)
   {
      redefinedClasses.put(redefinedClass, modifiedClassfile);
   }

   public void addInstanceForMockedType(Class<?> mockedType, InstanceFactory mockInstanceFactory)
   {
      mockedTypesAndInstances.put(mockedType, mockInstanceFactory);
   }

   public Object getNewInstanceForMockedType(Class<?> mockedType)
   {
      InstanceFactory instanceFactory = mockedTypesAndInstances.get(mockedType);

      if (instanceFactory == null) {
         return null;
      }

      TestRun.getExecutingTest().setShouldIgnoreMockingCallbacks(true);

      try {
         return instanceFactory.create();
      }
      finally {
         TestRun.getExecutingTest().setShouldIgnoreMockingCallbacks(false);
      }
   }

   public void restoreAndRemoveTransformedClasses(Set<String> transformedClassesToRestore)
   {
      RedefinitionEngine redefinitionEngine = new RedefinitionEngine();

      for (String transformedClassName : transformedClassesToRestore) {
         byte[] definitionToRestore = transformedClasses.get(transformedClassName);
         redefinitionEngine.restoreToDefinition(transformedClassName, definitionToRestore);
      }

      transformedClasses.keySet().removeAll(transformedClassesToRestore);
   }

   public void restoreAndRemoveRedefinedClasses(Set<Class<?>> redefinedClassesToRestore)
   {
      RedefinitionEngine redefinitionEngine = new RedefinitionEngine();

      for (Class<?> redefinedClass : redefinedClassesToRestore) {
         redefinitionEngine.restoreOriginalDefinition(redefinedClass);

         if (redefinedClassesWithNativeMethods.contains(redefinedClass.getName())) {
            reregisterNativeMethodsForRestoredClass(redefinedClass);
         }

         discardStateForCorrespondingMockClassIfAny(redefinedClass);
      }

      redefinedClasses.keySet().removeAll(redefinedClassesToRestore);
      mockedTypesAndInstances.keySet().removeAll(redefinedClassesToRestore);
   }

   private void discardStateForCorrespondingMockClassIfAny(Class<?> redefinedClass)
   {
      String mockClassesInternalNames = realClassesToMockClasses.remove(redefinedClass);

      TestRun.getMockClasses().getMockStates().removeClassState(redefinedClass, mockClassesInternalNames);
   }

   // Methods that deal with redefined native methods /////////////////////////////////////////////////////////////////

   public void addRedefinedClassWithNativeMethods(String redefinedClassInternalName)
   {
      redefinedClassesWithNativeMethods.add(redefinedClassInternalName.replace('/', '.'));
   }

   private void reregisterNativeMethodsForRestoredClass(Class<?> realClass)
   {
      Method registerNatives = null;

      try {
         registerNatives = realClass.getDeclaredMethod("registerNatives");
      }
      catch (NoSuchMethodException ignore) {
         try {
            registerNatives = realClass.getDeclaredMethod("initIDs");
         }
         catch (NoSuchMethodException alsoIgnore) {
            // OK
         }
      }

      if (registerNatives != null) {
         try {
            registerNatives.setAccessible(true);
            registerNatives.invoke(null);
         }
         catch (IllegalAccessException ignore) {
            // Won't happen.
         }
         catch (InvocationTargetException ignore) {
            // Shouldn't happen either.
         }
      }

      // OK, although another solution will be required for this particular class if it requires
      // natives to be explicitly registered again (not all do, such as java.lang.Float).
   }

   // Getter methods for the maps of redefined classes ////////////////////////////////////////////////////////////////

   public byte[] getFixedClassfile(String className)
   {
      return fixedClassDefinitions.get(className);
   }

   public int getRedefinedClassCount()
   {
      return redefinedClasses.size();
   }

   public byte[] getRedefinedClassfile(Class<?> redefinedClass)
   {
      return redefinedClasses.get(redefinedClass);
   }

   public Set<String> getTransformedClasses()
   {
      return transformedClasses.keySet();
   }

   public Set<Class<?>> getRedefinedClasses()
   {
      return redefinedClasses.keySet();
   }

   public boolean containsRedefinedClass(Class<?> redefinedClass)
   {
      return redefinedClasses.containsKey(redefinedClass);
   }

   public void turnRedefinedClassesIntoFixedOnes()
   {
      Iterator<Entry<Class<?>, byte[]>> itr = redefinedClasses.entrySet().iterator();

      while (itr.hasNext()) {
         Entry<Class<?>, byte[]> classAndBytecode = itr.next();
         itr.remove();

         addFixedClass(classAndBytecode.getKey().getName(), classAndBytecode.getValue());
      }
   }
}
