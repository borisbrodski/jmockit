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

import javax.swing.*;

import org.junit.*;

import static org.junit.Assert.*;

public final class CovariantReturnTypesTest
{
   public static class SuperClass
   {
      public JTextField getTextField() { return null; }
   }

   public static final class SubClass extends SuperClass
   {
      @Override
      public JPasswordField getTextField() { return null; }
   }

   @Test
   public void methodInClassHierarchyUsingStrictExpectations()
   {
      final JTextField regularField = new JTextField();
      final JPasswordField passwordField = new JPasswordField();
      SubClass subClassInstance = new SubClass();

      new Expectations()
      {
         SubClass mock;

         {
            new SuperClass().getTextField(); result = regularField;

            mock.getTextField(); result = passwordField;
            mock.getTextField(); result = passwordField;
         }
      };

      assertSame(regularField, new SuperClass().getTextField());

      assertSame(passwordField, subClassInstance.getTextField());
      assertSame(passwordField, ((SuperClass) subClassInstance).getTextField());
   }

   @Test
   public void methodInClassHierarchyUsingNonStrictExpectations()
   {
      final JTextField regularField = new JTextField();
      final JPasswordField passwordField = new JPasswordField();

      new NonStrictExpectations()
      {
         SubClass mock;

         {
            new SuperClass().getTextField(); result = regularField;

            mock.getTextField(); result = passwordField;
         }
      };

      assertSame(regularField, new SuperClass().getTextField());

      SubClass subClassInstance = new SubClass();
      assertSame(passwordField, subClassInstance.getTextField());
      assertSame(passwordField, ((SuperClass) subClassInstance).getTextField());
   }

   public abstract static class AbstractBaseClass
   {
      protected AbstractBaseClass() {}
      public abstract JTextField getTextField();
   }

   public static class ConcreteClass extends AbstractBaseClass
   {
      @Override
      public JFormattedTextField getTextField() { return null; }
   }

   @Test
   public void concreteMethodImplementationUsingStrictExpectations()
   {
      final JTextField formattedField = new JFormattedTextField();
      ConcreteClass concreteInstance = new ConcreteClass();

      new Expectations()
      {
         ConcreteClass mock;

         {
            mock.getTextField(); result = formattedField;
            ((AbstractBaseClass) mock).getTextField(); result = formattedField;
         }
      };

      assertSame(formattedField, concreteInstance.getTextField());
      assertSame(formattedField, ((AbstractBaseClass) concreteInstance).getTextField());
   }

   @Test
   public void concreteMethodImplementationUsingNonStrictExpectations()
   {
      final JTextField formattedField1 = new JFormattedTextField();
      final JTextField formattedField2 = new JFormattedTextField();
      ConcreteClass concreteInstance = new ConcreteClass();

      new Expectations()
      {
         @NonStrict ConcreteClass mock;

         {
            mock.getTextField(); returns(formattedField1, formattedField2);
         }
      };

      assertSame(formattedField1, concreteInstance.getTextField());
      assertSame(formattedField2, ((AbstractBaseClass) concreteInstance).getTextField());
   }

   @Test
   public void abstractMethodImplementationUsingStrictExpectations()
   {
      final JTextField regularField = new JTextField();
      final JTextField formattedField = new JFormattedTextField();

      new Expectations()
      {
         @Capturing AbstractBaseClass mock;

         {
            mock.getTextField(); result = regularField;
            mock.getTextField(); result = formattedField;
         }
      };

      AbstractBaseClass firstInstance = new AbstractBaseClass()
      {
         @Override
         public JTextField getTextField() { return null; }
      };
      assertSame(regularField, firstInstance.getTextField());

      assertSame(formattedField, firstInstance.getTextField());
   }

   @Test
   public void abstractMethodImplementationUsingNonStrictExpectations()
   {
      final JTextField regularField = new JTextField();
      final JTextField formattedField = new JFormattedTextField();

      new Expectations()
      {
         @NonStrict @Capturing AbstractBaseClass mock;

         {
            mock.getTextField(); result = regularField; result = formattedField;
         }
      };

      AbstractBaseClass firstInstance = new AbstractBaseClass()
      {
         @Override
         public JTextField getTextField() { return null; }
      };
      assertSame(regularField, firstInstance.getTextField());

      assertSame(formattedField, firstInstance.getTextField());
   }

   public interface SuperInterface { void someOtherMethod(int i); Object getValue(); }
   public interface SubInterface extends SuperInterface { String getValue(); }

   @Test
   public void methodInSuperInterfaceWithVaryingReturnValuesUsingStrictExpectations(
      final SuperInterface mock)
   {
      final Object value = new Object();
      final String specificValue = "test";

      new Expectations()
      {
         {
            mock.getValue(); result = value;
            mock.getValue(); result = specificValue;
         }
      };

      assertSame(value, mock.getValue());
      assertSame(specificValue, mock.getValue());
   }

   @Test
   public void methodInSuperInterfaceWithVaryingReturnValuesUsingNonStrictExpectations(
      @NonStrict final SuperInterface mock)
   {
      final Object value = new Object();
      final String specificValue = "test";

      new Expectations()
      {
         {
            mock.getValue(); result = value; result = specificValue;
         }
      };

      assertSame(value, mock.getValue());
      assertSame(specificValue, mock.getValue());
   }

   @Test
   public void methodInSubInterfaceUsingStrictExpectations(final SubInterface mock)
   {
      @SuppressWarnings({"UnnecessaryLocalVariable"}) final SuperInterface base = mock;
      final Object value = new Object();
      final String specificValue = "test";

      new Expectations()
      {
         {
            base.getValue(); result = value;
            base.getValue(); result = specificValue;

            mock.someOtherMethod(anyInt);

            mock.getValue(); result = specificValue;
            base.getValue(); result = specificValue;
         }
      };

      assertSame(value, base.getValue());
      assertSame(specificValue, base.getValue());

      mock.someOtherMethod(1);

      assertSame(specificValue, mock.getValue());
      assertSame(specificValue, base.getValue());
   }

   @Test
   public void methodInSubInterfaceUsingNonStrictExpectations(@NonStrict final SubInterface mock)
   {
      @SuppressWarnings({"UnnecessaryLocalVariable"}) final SuperInterface base = mock;
      final Object value = new Object();
      final String specificValue1 = "test1";
      final String specificValue2 = "test2";

      new Expectations()
      {
         {
            base.getValue(); returns(specificValue1, value);

            mock.getValue(); result = specificValue2;
         }
      };

      assertSame(specificValue1, base.getValue());
      assertSame(value, base.getValue());

      assertSame(specificValue2, mock.getValue());
   }

   @Test
   public void methodInSubInterfaceReplayedThroughSuperInterfaceUsingStrictExpectations(
      final SubInterface mock)
   {
      final String specificValue = "test";

      new Expectations()
      {
         {
            mock.getValue(); result = specificValue;
         }
      };

      assertSame(specificValue, ((SuperInterface) mock).getValue());
   }

   @Test
   public void methodInSubInterfaceReplayedThroughSuperInterfaceUsingNonStrictExpectations(
      @NonStrict final SubInterface mock)
   {
      final String specificValue = "test";

      new Expectations()
      {
         {
            mock.getValue(); result = specificValue;
         }
      };

      assertSame(specificValue, ((SuperInterface) mock).getValue());
   }
}
