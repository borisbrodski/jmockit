/*
 * JMockit
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

import java.util.*;

import static mockit.Deencapsulation.*;
import static org.junit.Assert.*;
import org.junit.*;

import mockit.internal.util.*;

@SuppressWarnings({"UnusedDeclaration", "ClassWithTooManyMethods"})
public final class DeencapsulationTest
{
   static final Class<?> innerClass = Utilities.loadClass("mockit.Subclass$InnerClass");
   final Subclass anInstance = new Subclass();

   @Test
   public void setValueOfNonConstantFinalField()
   {
      Subclass obj = new Subclass();

      setField(obj, "INITIAL_VALUE", 123);

      assertEquals(123, obj.INITIAL_VALUE);
   }

   @Test
   public void getInstanceFieldByName()
   {
      anInstance.setIntField(3);
      anInstance.setStringField("test");
      anInstance.setListField(Collections.<String>emptyList());

      Integer intValue = getField(anInstance, "intField");
      String stringValue = getField(anInstance, "stringField");
      List<String> listValue = getField(anInstance, "listField");

      assertEquals(anInstance.getIntField(), intValue.intValue());
      assertEquals(anInstance.getStringField(), stringValue);
      assertSame(anInstance.getListField(), listValue);
   }

   @Test(expected = IllegalArgumentException.class)
   public void attemptToGetInstanceFieldByNameWithWrongName()
   {
      getField(anInstance, "noField");
   }

   @Test
   public void getInheritedInstanceFieldByName()
   {
      anInstance.baseInt = 3;
      anInstance.baseString = "test";
      anInstance.baseSet = Collections.emptySet();

      Integer intValue = getField(anInstance, "baseInt");
      String stringValue = getField(anInstance, "baseString");
      Set<Boolean> listValue = getField(anInstance, "baseSet");

      assertEquals(anInstance.baseInt, intValue.intValue());
      assertEquals(anInstance.baseString, stringValue);
      assertSame(anInstance.baseSet, listValue);
   }

   @Test
   public void getInstanceFieldByType()
   {
      anInstance.setStringField("by type");
      anInstance.setListField(new ArrayList<String>());

      String stringValue = getField(anInstance, String.class);
      List<String> listValue = getField(anInstance, List.class);
      List<String> listValue2 = getField(anInstance, ArrayList.class);

      assertEquals(anInstance.getStringField(), stringValue);
      assertSame(anInstance.getListField(), listValue);
      assertSame(listValue, listValue2);
   }

   @Test(expected = IllegalArgumentException.class)
   public void attemptToGetInstanceFieldByTypeWithWrongType()
   {
      getField(anInstance, Byte.class);
   }

   @Test(expected = IllegalArgumentException.class)
   public void attemptToGetInstanceFieldByTypeForClassWithMultipleFieldsOfThatType()
   {
      getField(anInstance, int.class);
   }

   @Test
   public void getInheritedInstanceFieldByType()
   {
      Set<Boolean> fieldValueOnInstance = new HashSet<Boolean>();
      anInstance.baseSet = fieldValueOnInstance;

      Set<Boolean> setValue = getField(anInstance, fieldValueOnInstance.getClass());
      Set<Boolean> setValue2 = getField(anInstance, HashSet.class);

      assertSame(fieldValueOnInstance, setValue);
      assertSame(setValue, setValue2);
   }

   @Test
   public void getInstanceFieldOnBaseClassByType()
   {
      anInstance.setLongField(15);

      long longValue = getField(anInstance, long.class);

      assertEquals(15, longValue);
   }

   @Test
   public void getStaticFieldByName()
   {
      Subclass.setBuffer(new StringBuilder());

      StringBuilder b = getField(Subclass.class, "buffer");

      assertSame(Subclass.getBuffer(), b);
   }

   @Test(expected = IllegalArgumentException.class)
   public void attemptToGetStaticFieldByNameFromWrongClass()
   {
      getField(BaseClass.class, "buffer");
   }

   @Test
   public void getStaticFieldByType()
   {
      Subclass.setBuffer(new StringBuilder());

      StringBuilder b = getField(Subclass.class, StringBuilder.class);

      assertSame(Subclass.getBuffer(), b);
   }

   @Test
   public void setInstanceFieldByName()
   {
      anInstance.setIntField2(1);

      setField(anInstance, "intField2", 901);

      assertEquals(901, anInstance.getIntField2());
   }

   @Test(expected = IllegalArgumentException.class)
   public void attemptToSetInstanceFieldByNameWithWrongName()
   {
      setField(anInstance, "noField", 901);
   }

   @Test
   public void setInstanceFieldByType()
   {
      anInstance.setStringField("");

      setField(anInstance, "Test");

      assertEquals("Test", anInstance.getStringField());
   }

   @Test(expected = IllegalArgumentException.class)
   public void attemptToSetInstanceFieldByTypeWithWrongType()
   {
      setField(anInstance, (byte) 123);
   }

   @Test(expected = IllegalArgumentException.class)
   public void attemptToSetInstanceFieldByTypeForClassWithMultipleFieldsOfThatType()
   {
      setField(anInstance, 901);
   }

   @Test
   public void setStaticFieldByName()
   {
      Subclass.setBuffer(null);

      setField(Subclass.class, "buffer", new StringBuilder());

      assertNotNull(Subclass.getBuffer());
   }

   @Test(expected = IllegalArgumentException.class)
   public void attemptToSetStaticFieldByNameWithWrongName()
   {
      setField(Subclass.class, "noField", null);
   }

   @Test
   public void setStaticFieldByType()
   {
      Subclass.setBuffer(null);

      setField(Subclass.class, new StringBuilder());

      assertNotNull(Subclass.getBuffer());
   }

   @Test(expected = IllegalArgumentException.class)
   public void attemptToSetStaticFieldByTypeWithWrongType()
   {
      setField(Subclass.class, new StringBuffer());
   }

   @Test(expected = IllegalArgumentException.class)
   public void attemptToSetStaticFieldByTypeForClassWithMultipleFieldsOfThatType()
   {
      setField(Subclass.class, 'A');
   }

   @Test
   public void invokeInstanceMethodWithoutParameters()
   {
      Long result = invoke(anInstance, "aMethod");

      assertEquals(567L, result.longValue());
   }

   @Test
   public void invokeInstanceMethodWithMultipleParameters()
   {
      Object result = invoke(anInstance, "instanceMethod", (short) 7, "abc", true);

      assertNull(result);
   }

   @Test
   public void invokeInstanceMethodWithSingleParameterOfBaseTypeWhilePassingSubtypeInstance()
   {
      invoke(anInstance, "setListField", new ArrayList<String>());
   }

   @Test
   public void invokeStaticMethodWithoutParameters()
   {
      Boolean result = invoke(Subclass.class, "anStaticMethod");

      assertTrue(result);
   }

   @Test
   public void invokeStaticMethodByClassNameWithoutParameters()
   {
      Boolean result = invoke(Subclass.class.getName(), "anStaticMethod");

      assertTrue(result);
   }

   @Test
   public void invokeStaticMethodWithMultipleParameters()
   {
      Object result = invoke(Subclass.class, "staticMethod", (short) 7, "abc", true);

      assertNull(result);
   }

   @Test
   public void invokeStaticMethodByClassNameWithMultipleParameters()
   {
      Object result = invoke(Subclass.class.getName(), "staticMethod", (short) 7, "abc", true);

      assertNull(result);
   }

   @Test
   public void newInstanceUsingNoArgsConstructorFromSpecifiedParameterTypes()
   {
      Subclass instance = newInstance(Subclass.class.getName(), new Class<?>[] {});

      assertNotNull(instance);
      assertEquals(-1, instance.getIntField());
   }

   @Test
   public void newInstanceUsingNoArgsConstructorWithoutSpecifyingParameters()
   {
      Subclass instance = newInstance(Subclass.class.getName());

      assertNotNull(instance);
      assertEquals(-1, instance.getIntField());
   }

   @Test
   public void newInstanceByNameUsingMultipleArgsConstructorFromSpecifiedParameterTypes()
   {
      Subclass instance =
         newInstance(Subclass.class.getName(), new Class<?>[] {int.class, String.class}, 1, "XYZ");

      assertNotNull(instance);
      assertEquals(1, instance.getIntField());
      assertEquals("XYZ", instance.getStringField());
   }

   @Test
   public void newInstanceUsingMultipleArgsConstructorFromSpecifiedParameterTypes()
   {
      BaseClass instance =
         newInstance(Subclass.class, new Class<?>[] {int.class, String.class}, 1, "XYZ");

      assertNotNull(instance);
   }

   @Test(expected = IllegalArgumentException.class)
   public void attemptNewInstanceWithNoMatchingConstructor()
   {
      newInstance(Subclass.class.getName(), new Class<?>[] {char.class}, 'z');
   }

   @Test
   public void newInstanceByNameUsingMultipleArgsConstructorFromNonNullArgumentValues()
   {
      Subclass instance = newInstance(Subclass.class.getName(), 590, "");

      assertNotNull(instance);
      assertEquals(590, instance.getIntField());
      assertEquals("", instance.getStringField());
   }

   @Test
   public void newInstancePassingSubclassInstanceToConstructorWithSingleArgument()
   {
      List<String> aList = new ArrayList<String>();

      Subclass instance = newInstance(Subclass.class, aList);

      assertNotNull(instance);
      assertSame(aList, instance.getListField());
   }

   @Test
   public void newInstanceUsingMultipleArgsConstructorFromNonNullArgumentValues()
   {
      BaseClass instance = newInstance(Subclass.class, 590, "");

      assertNotNull(instance);
   }

   @Test
   public void newInnerInstanceUsingNoArgsConstructor()
   {
      Object innerInstance = newInnerInstance("InnerClass", anInstance);

      assertTrue(innerClass.isInstance(innerInstance));
   }

   @Test(expected = IllegalArgumentException.class)
   public void newInnerInstanceWithWrongInnerClassName()
   {
      newInnerInstance("NoClass", anInstance);
   }

   @Test
   public void newInnerInstanceByNameUsingMultipleArgsConstructor()
   {
      Object innerInstance = newInnerInstance("InnerClass", anInstance, true, 5L, "");

      assertTrue(innerClass.isInstance(innerInstance));
   }

   @Test
   public void newInnerInstanceUsingMultipleArgsConstructor()
   {
      Object innerInstance = newInnerInstance(innerClass, anInstance, true, 5L, "");

      assertTrue(innerClass.isInstance(innerInstance));
   }

   @Test
   public void newInnerInstancePassingSubclassInstanceToConstructorWithSingleArgument()
   {
      List<String> aList = new ArrayList<String>();

      Object innerInstance = newInnerInstance(innerClass, anInstance, aList);

      assertTrue(innerClass.isInstance(innerInstance));
   }
}
