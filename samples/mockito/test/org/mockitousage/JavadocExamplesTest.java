package org.mockitousage;

import java.util.*;

import org.junit.*;
import static org.junit.Assert.*;
import org.junit.runner.*;
import static org.mockito.Mockito.*;
import org.mockito.runners.*;
import org.mockito.*;
import org.mockito.exceptions.verification.*;
import org.mockito.invocation.*;
import org.mockito.stubbing.*;
import org.hamcrest.beans.*;

/**
 * File created from code snippets in
 * <a href="http://mockito.googlecode.com/svn/branches/1.7/javadoc/org/mockito/Mockito.html">Mockito documentation</a>,
 * with some minor changes.
 */
@RunWith(MockitoJUnitRunner.class)
public class JavadocExamplesTest
{
   private final List<String> mockedList = mock(List.class);

   @Test
   public void verifyingBehavior()
   {
      //mock creation
      List<String> mockedList = mock(List.class);

      //using mock object
      mockedList.add("one");
      mockedList.clear();

      //verification
      verify(mockedList).add("one");
      verify(mockedList).clear();
   }

   @Test
   public void stubbing()
   {
      //You can mock concrete classes, not only interfaces
      List<String> mockedList = mock(LinkedList.class);

      //stubbing
      when(mockedList.get(0)).thenReturn("first");
      when(mockedList.get(1)).thenThrow(new RuntimeException());

      assertEquals("first", mockedList.get(0));

      try {
         mockedList.get(1);
      }
      catch (RuntimeException e) {
         // OK
      }

      assertNull(mockedList.get(999));
   }

   @Test
   public void stubbingAndVerifying()
   {
      List<String> mockedList = mock(LinkedList.class);

      when(mockedList.get(0)).thenReturn("first");

      assertEquals("first", mockedList.get(0));

      // Although it is possible to verify a stubbed invocation, usually it's just redundant
      // If your code cares what get(0) returns then something else breaks (often before even
      // verify() gets executed).
      // If your code doesn't care what get(0) returns then it should not be stubbed.
      verify(mockedList).get(0);
   }

   @Test
   public void argumentMatchers()
   {
      //stubbing using built-in anyInt() argument matcher
      when(mockedList.get(anyInt())).thenReturn("element");

      //stubbing using hamcrest:
      when(mockedList.contains(argThat(new HasProperty<String>("abc")))).thenReturn(true);

      assertEquals("element", mockedList.get(999));

      //you can also verify using an argument matcher
      verify(mockedList).get(anyInt());
   }

   @Test
   public void verifyingNumberOfInvocations()
   {
      //using mock
      mockedList.add("once");

      mockedList.add("twice");
      mockedList.add("twice");

      mockedList.add("three times");
      mockedList.add("three times");
      mockedList.add("three times");

      //following two verifications work exactly the same - times(1) is used by default
      verify(mockedList).add("once");
      verify(mockedList, times(1)).add("once");

      //exact number of invocations verification
      verify(mockedList, times(2)).add("twice");
      verify(mockedList, times(3)).add("three times");

      //verification using never(). never() is an alias to times(0)
      verify(mockedList, never()).add("never happened");

      //verification using atLeast()/atMost()
      verify(mockedList, atLeastOnce()).add("three times");
      verify(mockedList, atLeast(2)).add("three times");
      verify(mockedList, atMost(5)).add("three times");
   }

   @Test(expected = RuntimeException.class)
   public void stubbingVoidMethodsWithExceptions()
   {
      doThrow(new RuntimeException()).when(mockedList).clear();

      //following throws RuntimeException:
      mockedList.clear();
   }

   @Test
   public void verificationInOrder()
   {
      List<String> firstMock = mock(List.class);
      List<String> secondMock = mock(List.class);

      //using mocks
      firstMock.add("was called first");
      secondMock.add("was called second");

      //create inOrder object passing any mocks that need to be verified in order
      InOrder inOrder = inOrder(firstMock, secondMock);

      //following will make sure that firstMock was called before secondMock
      inOrder.verify(firstMock).add("was called first");
      inOrder.verify(secondMock).add("was called second");
   }

   @Test
   public void verifyingThatInteractionsNeverHappened()
   {
      List<String> mockTwo = mock(List.class);
      List<String> mockThree = mock(List.class);

      //using mocks - only mockedList is interacted
      mockedList.add("one");

      //ordinary verification
      verify(mockedList).add("one");

      //verify that method was never called on a mock
      verify(mockedList, never()).add("two");

      //verify that other mocks were not interacted
      verifyZeroInteractions(mockTwo, mockThree);
   }

   @Test(expected = NoInteractionsWanted.class)
   public void verifyingThatInteractionsNeverHappenedWhenTheyDid()
   {
      List<String> mockTwo = mock(List.class);

      mockedList.add("one");
      mockTwo.size();

      verify(mockedList).add("one");

      verifyZeroInteractions(mockTwo);
   }

   @Test
   public void verifyingAllInteractions()
   {
      mockedList.add("one");
      mockedList.add("two");

      // Verifies first interaction:
      verify(mockedList).add("one");

      // Verifies second (and last) interaction:
      verify(mockedList).add("two");

      // Verify that no other interactions happened to mockedList:
      verifyNoMoreInteractions(mockedList);
   }

   @Test(expected = NoInteractionsWanted.class)
   public void verifyingAllInteractionsWhenMoreOfThemHappen()
   {
      mockedList.add("one");
      mockedList.add("two");
      mockedList.size();

      verify(mockedList).add("one");
      verify(mockedList).add("two");
      verifyNoMoreInteractions(mockedList);
   }

   @Test
   public void stubbingConsecutiveCalls()
   {
      Iterator<String> mock = mock(Iterator.class);

      when(mock.next())
         .thenThrow(new IllegalStateException())
         .thenReturn("foo");

      // First call: throws exception.
      try {
         mock.next();
      }
      catch (IllegalStateException e) {
         // OK
      }

      // Second call: prints "foo".
      assertEquals("foo", mock.next());

      // Any consecutive call: prints "foo" as well (last stubbing wins).
      assertEquals("foo", mock.next());
   }

   @Test
   public void stubbingWithCallbacks()
   {
      TestedClass mock = mock(TestedClass.class);

      when(mock.someMethod(anyString())).thenAnswer(new Answer()
      {
         public Object answer(InvocationOnMock invocation)
         {
            Object[] args = invocation.getArguments();
            Object mock = invocation.getMock();
            return "called with arguments: " + Arrays.toString(args);
         }
      });

      assertEquals("called with arguments: [foo]", mock.someMethod("foo"));
   }

   static class TestedClass
   {
      private final List<String> items = new LinkedList<String>();

      public String someMethod(String s) { return ""; }
      public String addItem(String item) { items.add(item); return ""; }
      public int getItemCount() { return items.size(); }
      public Object getItem(int index) { return items.get(index); }
   }

   @Test
   public void spyingOnRealObjects()
   {
      TestedClass spy = spy(new TestedClass());

      //optionally, you can stub out some methods:
      when(spy.getItemCount()).thenReturn(100);

      // When using the regular "when(spy.someMethod(...)).thenDoXyz(...)" API, all calls to a spy
      // object will not only perform stubbing, but also execute the real method:
      // when(spy.getItem(1)).thenReturn("an item"); => would throw an IndexOutOfBoundsException

      // Therefore, a different API must sometimes be used for stubbing, to avoid side effects:
      doReturn("an item").when(spy).getItem(1);
      
      //using the spy calls real methods, except those stubbed out
      spy.addItem("one");
      spy.addItem("two");

      assertEquals("one", spy.getItem(0));
      assertEquals("an item", spy.getItem(1));
      assertEquals(100, spy.getItemCount());

      //optionally, you can verify
      verify(spy).addItem("one"); // the real "addItem" is not called here
      verify(spy).addItem("two");
   }
}
