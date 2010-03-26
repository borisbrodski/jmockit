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
import java.net.*;
import java.nio.channels.*;
import java.util.*;

import org.junit.*;

import mockit.internal.state.*;

import static org.junit.Assert.*;

public final class CascadingTest
{
   static class Foo
   {
      Bar getBar() { return null; }

      static Bar globalBar() { return null; }

      void doSomething(String s) { throw new RuntimeException(s); }
      int getIntValue() { return 1; }
      private Boolean getBooleanValue() { return true; }
      final List<Integer> getList() { return null; }
   }

   static class Bar
   {
      int doSomething() { return 1; }
      Baz getBaz() { return null; }
      AnEnum getEnum() { return null; }
   }

   interface Baz
   {
      void runIt();
   }

   enum AnEnum { First, Second, Third }

   @After
   public void verifyThereAreNoCascadingMockedTypesOutsideTestMethods()
   {
      assert TestRun.getExecutingTest().getCascadingMockedTypes().isEmpty();
      assert TestRun.mockFixture().getMockedTypesAndInstances().isEmpty();
   }

   @Test
   public void cascadeOneLevelDuringReplay(@Cascading Foo foo)
   {
      assert foo.getBar().doSomething() == 0;
      assert Foo.globalBar().doSomething() == 0;
      assert foo.getBar() != Foo.globalBar();

      foo.doSomething("test");
      assert foo.getIntValue() == 0;
      assert foo.getBooleanValue() == null;
      assert foo.getList().isEmpty();
   }

   @Test
   public void cascadeOneLevelDuringRecord()
   {
      final List<Integer> list = Arrays.asList(1, 2, 3);

      new NonStrictExpectations()
      {
         @Cascading Foo foo;

         {
            foo.doSomething(anyString); minTimes = 2;
            foo.getBar().doSomething(); result = 2;
            Foo.globalBar().doSomething(); result = 3;
            foo.getBooleanValue(); result = true;
            foo.getIntValue(); result = -1;
            foo.getList(); result = list;
         }
      };

      Foo foo = new Foo();
      foo.doSomething("1");
      assert foo.getBar().doSomething() == 2;
      foo.doSomething("2");
      assert Foo.globalBar().doSomething() == 3;
      assert foo.getBooleanValue();
      assert foo.getIntValue() == -1;
      assert list == foo.getList();
   }

   @Test
   public void cascadeOneLevelDuringVerify(@Cascading final Foo foo)
   {
      Bar bar = foo.getBar();
      bar.doSomething();
      bar.doSomething();

      Foo.globalBar().doSomething();

      assert foo.getIntValue() == 0;
      assert foo.getBooleanValue() == null;

      assert foo.getList().isEmpty();

      new Verifications()
      {
         {
            foo.getBar().doSomething(); minTimes = 3;
         }
      };

      new VerificationsInOrder()
      {
         {
            foo.getIntValue();
            foo.getBooleanValue();
         }
      };
   }

   @Test
   public void cascadeTwoLevelsDuringReplay(@Cascading Foo foo)
   {
      foo.getBar().getBaz().runIt();
   }

   @Test
   public void cascadeTwoLevelsDuringRecord()
   {
      new Expectations()
      {
         @Cascading @Mocked final Foo foo = new Foo();

         {
            foo.getBar().doSomething(); result = 1;
            Foo.globalBar().doSomething(); result = 2;

            foo.getBar().getBaz().runIt(); times = 2;
         }
      };

      Foo foo = new Foo();
      assert foo.getBar().doSomething() == 1;
      assert Foo.globalBar().doSomething() == 2;

      Baz baz = foo.getBar().getBaz();
      baz.runIt();
      baz.runIt();
   }

   @Test
   public void cascadeTwoLevelsWithInvocationRecordedOnLastMockOnly(@Cascading Foo foo)
   {
      new Expectations()
      {
         Baz baz;

         {
            baz.runIt();
         }
      };

      // Intermediate mocked type Bar is never mentioned above.
      foo.getBar().getBaz().runIt();
   }

   @Test
   public void cascadeTwoLevelsAndVerifyInvocationOnLastMockOnly(@Cascading Foo foo, final Baz baz)
   {
      foo.getBar().getBaz().runIt();

      new Verifications()
      {
         {
            baz.runIt();
         }
      };
   }

   // Tests using the java.lang.Process and java.lang.ProcessBuilder classes //////////////////////

   @Test
   public void cascadeOnJREClasses() throws Exception
   {
      new NonStrictExpectations()
      {
         @Cascading ProcessBuilder pb;

         {
            ProcessBuilder sameBuilder = pb.directory((File) any);
            assert pb != sameBuilder;

            Process process = sameBuilder.start();
            process.getOutputStream().write(5);
            process.exitValue(); result = 1;
         }
      };

      Process process = new ProcessBuilder("test").directory(new File("myDir")).start();
      process.getOutputStream().write(5);
      process.getOutputStream().flush();
      assert process.exitValue() == 1;
   }

   // Tests using java.net classes ////////////////////////////////////////////////////////////////

   static final class SocketFactory
   {
      public Socket createSocket() { return new Socket(); }

      public Socket createSocket(String host, int port) throws IOException
      {
         return new Socket(host, port);
      }
   }

   @Test
   public void cascadeOneLevelWithArgumentMatchers(@Cascading final SocketFactory sf)
      throws Exception
   {
      new NonStrictExpectations()
      {
         {
            sf.createSocket(anyString, 80); result = null;
         }
      };

      assert sf.createSocket("expected", 80) == null;
      assert sf.createSocket("unexpected", 8080) != null;
   }

   @Test
   public void recordAndVerifyOneLevelDeep(@Cascading final SocketFactory sf) throws Exception
   {
      final OutputStream out = new ByteArrayOutputStream();

      new NonStrictExpectations()
      {
         {
            sf.createSocket().getOutputStream(); result = out;
         }
      };

      assert out == sf.createSocket().getOutputStream();

      new FullVerifications()
      {
         {
            sf.createSocket().getOutputStream();
         }
      };
   }

   @Test
   public void recordAndVerifyOnTwoCascadingMocksOfTheSameType(
      @Cascading final SocketFactory sf1, @Cascading final SocketFactory sf2) throws Exception
   {
      final OutputStream out1 = new ByteArrayOutputStream();
      final OutputStream out2 = new ByteArrayOutputStream();

      new NonStrictExpectations()
      {
         {
            sf1.createSocket().getOutputStream(); result = out1;
            sf2.createSocket().getOutputStream(); result = out2;
         }
      };

      assert out1 == sf1.createSocket().getOutputStream();
      assert out2 == sf2.createSocket().getOutputStream();

      new FullVerificationsInOrder()
      {
         {
            sf1.createSocket().getOutputStream();
            sf2.createSocket().getOutputStream();
         }
      };
   }

   @Test
   public void recordAndVerifySameInvocationOnMocksReturnedFromInvocationsWithDifferentArguments(
      @Cascading final SocketFactory sf) throws Exception
   {
      new NonStrictExpectations()
      {
         {
            sf.createSocket().getPort(); result = 1;
            sf.createSocket("first", 80).getPort(); result = 2;
            sf.createSocket("second", 80).getPort(); result = 3;
            sf.createSocket(anyString, 81).getPort(); result = 4;
         }
      };

      assert sf.createSocket().getPort() == 1;
      assert sf.createSocket("first", 80).getPort() == 2;
      assert sf.createSocket("second", 80).getPort() == 3;
      assert sf.createSocket("third", 81).getPort() == 4;

      new Verifications()
      {
         {
            sf.createSocket("first", 80).getPort();
            sf.createSocket().getPort(); times = 1;
            sf.createSocket(anyString, 81).getPort(); maxTimes = 1;
            sf.createSocket("second", 80).getPort();
            sf.createSocket("fourth", -1); times = 0;
         }
      };
   }

   @Test
   public void cascadeOnInheritedMethod(@Cascading SocketChannel sc)
   {
      assert sc.provider() != null;
   }

   @Test
   public void recordAndVerifyWithMixedCascadeLevels(@Cascading final SocketFactory sf)
      throws Exception
   {
      new NonStrictExpectations()
      {
         {
            sf.createSocket("first", 80).getKeepAlive(); result = true;
            sf.createSocket("second", anyInt).getChannel().close(); times = 1;
         }
      };

      sf.createSocket("second", 80).getChannel().close();
      assert sf.createSocket("first", 80).getKeepAlive();
      sf.createSocket("first", 8080).getChannel().provider().openPipe();

      new Verifications()
      {
         {
            sf.createSocket("first", 8080).getChannel().provider().openPipe();
         }
      };
   }

   @Test
   public void overrideCascadedMockAndRecordStrictExpectationOnIt(
      @Cascading final Foo foo, final Bar mockBar)
   {
      new Expectations()
      {
         {
            foo.getBar(); result = mockBar;
            mockBar.doSomething();
         }
      };

      Bar bar = foo.getBar();
      bar.doSomething();
   }

   @Test
   public void overrideCascadedMockAndRecordNonStrictExpectationOnIt(@Cascading final Foo foo)
   {
      new NonStrictExpectations()
      {
         Bar mockBar;

         {
            foo.getBar(); result = mockBar;
            mockBar.doSomething(); times = 1; result = 123;
         }
      };

      Bar bar = foo.getBar();
      assertEquals(123, bar.doSomething());
   }

   @Test
   public void overrideTwoCascadedMocksOfTheSameType(
      @Cascading final Foo foo1, @Cascading final Foo foo2)
   {
      new Expectations()
      {
         Bar bar1;
         Bar bar2;

         {
            foo1.getBar(); result = bar1;
            foo2.getBar(); result = bar2;
            bar1.doSomething();
            bar2.doSomething();
         }
      };

      Bar bar1 = foo1.getBar();
      Bar bar2 = foo2.getBar();
      bar1.doSomething();
      bar2.doSomething();
   }

   @Test(expected = AssertionError.class)
   public void overrideTwoCascadedMocksOfTheSameTypeButReplayInDifferentOrder(
      @Cascading final Foo foo1, @Cascading final Foo foo2)
   {
      new Expectations()
      {
         Bar bar1;
         Bar bar2;

         {
            foo1.getBar(); result = bar1;
            foo2.getBar(); result = bar2;
            bar1.doSomething();
            bar2.doSomething();
         }
      };

      Bar bar1 = foo1.getBar();
      Bar bar2 = foo2.getBar();
      bar2.doSomething();
      bar1.doSomething();
   }

   @Test
   public void cascadedEnum(@Cascading final Foo mock)
   {
      new Expectations()
      {
         {
            mock.getBar().getEnum(); result = AnEnum.Second;
         }
      };

      assertEquals(AnEnum.Second, mock.getBar().getEnum());
   }

   @Test
   public void cascadedNonStrictEnumReturningConsecutiveValuesThroughResultField(
      @Cascading final Foo mock)
   {
      new NonStrictExpectations()
      {
         {
            mock.getBar().getEnum();
            result = AnEnum.First;
            result = AnEnum.Second;
            result = AnEnum.Third;
         }
      };

      assertSame(AnEnum.First, mock.getBar().getEnum());
      assertSame(AnEnum.Second, mock.getBar().getEnum());
      assertSame(AnEnum.Third, mock.getBar().getEnum());
   }

   @Test
   public void cascadedNonStrictEnumReturningConsecutiveValuesThroughReturnsMethod(
      @NonStrict @Cascading final Foo mock)
   {
      new Expectations()
      {
         {
            mock.getBar().getEnum();
            returns(AnEnum.First, AnEnum.Second, AnEnum.Third);
         }
      };

      assertSame(AnEnum.First, mock.getBar().getEnum());
      assertSame(AnEnum.Second, mock.getBar().getEnum());
      assertSame(AnEnum.Third, mock.getBar().getEnum());
   }

   @Test
   public void cascadedStrictEnumReturningConsecutiveValuesThroughResultField(
      @Cascading final Foo mock)
   {
      new Expectations()
      {
         {
            mock.getBar().getEnum();
            result = AnEnum.Third;
            result = AnEnum.Second;
            result = AnEnum.First;
         }
      };

      Bar bar = mock.getBar();
      assertSame(AnEnum.Third, bar.getEnum());
      assertSame(AnEnum.Second, bar.getEnum());
      assertSame(AnEnum.First, bar.getEnum());
   }

   @Test
   public void cascadedStrictEnumReturningConsecutiveValuesThroughReturnsMethod(
      @Cascading final Foo mock)
   {
      new Expectations()
      {
         {
            mock.getBar().getEnum();
            returns(AnEnum.First, AnEnum.Second, AnEnum.Third);
         }
      };

      Bar bar = mock.getBar();
      assertSame(AnEnum.First, bar.getEnum());
      assertSame(AnEnum.Second, bar.getEnum());
      assertSame(AnEnum.Third, bar.getEnum());
   }
}
