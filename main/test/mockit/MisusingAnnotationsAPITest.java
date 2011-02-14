/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import org.junit.*;

import static mockit.Mockit.*;
import static org.junit.Assert.*;

public final class MisusingAnnotationsAPITest
{
   @Test(expected = IllegalArgumentException.class)
   public void attemptToSetUpMockWithoutRealClass()
   {
      setUpMocks(MockCollaborator.class);
   }

   static class MockCollaborator
   {
      @Mock
      void provideSomeService() {}
   }

   interface BusinessInterface
   {
      @SuppressWarnings({"UnusedDeclaration"})
      void provideSomeService();
   }

   @MockClass(realClass = BusinessInterface.class)
   static class MockCollaborator2
   {
      @Mock
      void provideSomeService() {}
   }

   @Test(expected = IllegalArgumentException.class)
   public void attemptToSetUpMockForInterfaceButWithoutCreatingAProxy()
   {
      setUpMocks(new MockCollaborator2());
   }

   public static class Collaborator
   {
      boolean doSomething() { return false; }
   }

   public static class MockCollaborator3
   {
      @Mock
      boolean doSomething() { return true; }
   }

   @Test(expected = IllegalArgumentException.class)
   public void setUpMockForSingleRealClassByPassingTheMockClassLiteral()
   {
      setUpMock(MockCollaborator3.class);

      assertTrue(new Collaborator().doSomething());
   }

   @Test(expected = IllegalArgumentException.class)
   public void setUpMockForSingleRealClassByPassingAMockClassInstance()
   {
      setUpMock(new MockCollaborator3());

      assertTrue(new Collaborator().doSomething());
   }
}