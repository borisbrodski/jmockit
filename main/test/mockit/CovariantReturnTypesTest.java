package mockit;

import javax.swing.*;

import org.junit.*;

import mockit.integration.junit4.*;

public final class CovariantReturnTypesTest extends JMockitTest
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
            new SuperClass().getTextField(); returns(regularField);

            mock.getTextField(); returns(passwordField);
            mock.getTextField(); returns(passwordField);
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
            new SuperClass().getTextField(); returns(regularField);

            mock.getTextField(); returns(passwordField);
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
            mock.getTextField(); returns(formattedField);
            ((AbstractBaseClass) mock).getTextField(); returns(formattedField);
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
            mock.getTextField(); returns(regularField);
            mock.getTextField(); returns(formattedField);
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
            mock.getTextField(); returns(regularField); returns(formattedField);
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

   interface SuperInterface { Object getValue(); }
   interface SubInterface extends SuperInterface { String getValue(); }

   @Test
   public void methodInSuperInterfaceWithVaryingReturnValuesUsingStrictExpectations(
      final SuperInterface mock)
   {
      final Object value = new Object();
      final String specificValue = "test";

      new Expectations()
      {
         {
            mock.getValue(); returns(value);
            mock.getValue(); returns(specificValue);
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
            mock.getValue(); returns(value); returns(specificValue);
         }
      };

      assertSame(value, mock.getValue());
      assertSame(specificValue, mock.getValue());
   }

   @Test
   public void methodInSubInterfaceUsingStrictExpectations(final SubInterface mock)
   {
      final SuperInterface base = mock;
      final Object value = new Object();
      final String specificValue = "test";

      new Expectations()
      {
         {
            base.getValue(); returns(value);
            base.getValue(); returns(specificValue);

            mock.getValue(); returns(specificValue);
            base.getValue(); returns(specificValue);
         }
      };

      assertSame(value, base.getValue());
      assertSame(specificValue, base.getValue());

      assertSame(specificValue, mock.getValue());
      assertSame(specificValue, base.getValue());
   }

   @Test
   public void methodInSubInterfaceUsingNonStrictExpectations(@NonStrict final SubInterface mock)
   {
      final SuperInterface base = mock;
      final Object value = new Object();
      final String specificValue1 = "test1";
      final String specificValue2 = "test2";

      new Expectations()
      {
         {
            base.getValue(); returns(specificValue1, value);

            mock.getValue(); returns(specificValue2);
         }
      };

      assertSame(specificValue1, base.getValue());
      assertSame(value, base.getValue());

      assertSame(specificValue2, mock.getValue());
   }
}
