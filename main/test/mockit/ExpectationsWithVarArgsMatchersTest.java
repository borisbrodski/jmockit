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

import java.util.*;

import org.junit.*;

public final class ExpectationsWithVarArgsMatchersTest
{
   static class Collaborator
   {
      List<?> complexOperation(Object input1, Object... otherInputs)
      {
         return input1 == null ? Collections.emptyList() : Arrays.asList(otherInputs);
      }
   }

   @Mocked private Collaborator mock;

   @Test(expected = AssertionError.class)
   public void replayVarargsMethodWithDifferentThanExpectedNonVarargsArgument()
   {
      new Expectations()
      {
         {
            mock.complexOperation(1, 2, 3);
         }
      };

      mock.complexOperation(2, 2, 3);
   }

   @Test(expected = AssertionError.class)
   public void replayVarargsMethodWithDifferentThanExpectedNumberOfVarargsArguments()
   {
      new Expectations()
      {
         {
            mock.complexOperation(1, 2, 3);
         }
      };

      mock.complexOperation(1, 2);
   }

   @Test(expected = AssertionError.class)
   public void replayVarargsMethodWithDifferentThanExpectedVarargsArgument()
   {
      new Expectations()
      {
         {
            mock.complexOperation(1, 2, 3);
         }
      };

      mock.complexOperation(1, 2, 4);
   }

   @Test
   public void expectInvocationOnMethodWithVarargsArgumentUsingArgumentMatchers()
   {
      new Expectations()
      {
         {
            mock.complexOperation(withEqual(1), withNotEqual(2), withNull());
         }
      };

      mock.complexOperation(1, 3, null);
   }

   @SuppressWarnings({"NullArgumentToVariableArgMethod"})
   @Test
   public void expectInvocationWithNoVarArgs()
   {
      //noinspection UnusedDeclaration
      class VarArgs
      {
         public void varsOnly(int... ints) {}
         public void mixed(String arg0, int... ints) {}
      }

      VarArgs varArgs = new VarArgs();

      new Expectations()
      {
         VarArgs bar;
         {
            bar.varsOnly();
            bar.varsOnly(null);
            bar.mixed("abcd");
            bar.mixed("abcd", null);
         }
      };

      varArgs.varsOnly();
      varArgs.varsOnly(null);
      varArgs.mixed("abcd");
      varArgs.mixed("abcd", null);
   }

   @Test
   public void expectInvocationWithNonPrimitiveVarArgs()
   {
      //noinspection UnusedDeclaration
      class VarArgs
      {
         public void mixed(String[] strings, Integer... ints) {}
      }

      VarArgs varArgs = new VarArgs();
      final String[] strings1 = new String[0];
      final String[] strings2 = {"efgh", "ijkl"};

      new Expectations()
      {
         VarArgs bar;
         {
            bar.mixed(null, 4, 5, 6);
            bar.mixed(strings1, 4, 5, 6);
            bar.mixed(strings2, 4, 5, 6);
            bar.mixed(null);
            bar.mixed(strings1);
            bar.mixed(strings2);
         }
      };

      varArgs.mixed(null, 4, 5, 6);
      varArgs.mixed(strings1, 4, 5, 6);
      varArgs.mixed(strings2, 4, 5, 6);
      varArgs.mixed(null);
      varArgs.mixed(strings1);
      varArgs.mixed(strings2);
   }

   @SuppressWarnings({"NullArgumentToVariableArgMethod"})
   @Test
   public void expectInvocationWithPrimitiveVarArgs()
   {
      //noinspection UnusedDeclaration
      class VarArgs
      {
         public void varsOnly(int... ints) {}
         public void mixed(String arg0, String[] strings, int... ints) {}
      }

      VarArgs varArgs = new VarArgs();
      final String[] strings1 = new String[0];
      final String[] strings2 = {"efgh", "ijkl"};

      new Expectations()
      {
         VarArgs bar;
         {
            bar.varsOnly(1, 2, 3);
            bar.varsOnly(null);
            bar.mixed("abcd", null, 4, 5, 6);
            bar.mixed("abcd", strings1, 4, 5, 6);
            bar.mixed("abcd", strings2, 4, 5, 6);
            bar.mixed("abcd", null);
            bar.mixed("abcd", strings1);
            bar.mixed("abcd", strings2);
            bar.mixed("abcd", null, null);
            bar.mixed(null, null, null);
         }
      };

      varArgs.varsOnly(1, 2, 3);
      varArgs.varsOnly(null);
      varArgs.mixed("abcd", null, 4, 5, 6);
      varArgs.mixed("abcd", strings1, 4, 5, 6);
      varArgs.mixed("abcd", strings2, 4, 5, 6);
      varArgs.mixed("abcd", null);
      varArgs.mixed("abcd", strings1);
      varArgs.mixed("abcd", strings2);
      varArgs.mixed("abcd", null, null);
      varArgs.mixed(null, null, null);
   }

   @Test
   public void expectInvocationWithPrimitiveVarArgsUsingMatchers()
   {
      //noinspection UnusedDeclaration
      class VarArgs
      {
         public void mixed(String[] strings, int... ints) {}
      }

      VarArgs varArgs = new VarArgs();
      final String[] strings1 = new String[0];
      final String[] strings2 = {"efgh", "ijkl"};

      new Expectations()
      {
         VarArgs bar;
         {
            bar.mixed((String[]) withNull(), withEqual(4), withEqual(5), withEqual(6));
            bar.mixed(withEqual(strings1), withEqual(4), withEqual(5), withEqual(6));
            bar.mixed(withEqual(strings2), withEqual(4), withEqual(5), withEqual(6));
            bar.mixed((String[]) withNull());
            bar.mixed(withEqual(strings1));
            bar.mixed(withEqual(strings2));
         }
      };

      varArgs.mixed(null, 4, 5, 6);
      varArgs.mixed(strings1, 4, 5, 6);
      varArgs.mixed(strings2, 4, 5, 6);
      varArgs.mixed(null);
      varArgs.mixed(strings1);
      varArgs.mixed(strings2);
   }
}
