/*
 * JMockit Annotations
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