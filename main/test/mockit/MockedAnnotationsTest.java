package mockit;

import static org.junit.Assert.*;
import org.junit.*;
import org.junit.runner.*;

import mockit.integration.junit4.*;

@RunWith(JMockit.class)
public final class MockedAnnotationsTest
{
   @interface MyAnnotation
   {
      String value();
      boolean flag() default true;
      String[] values() default {};
   }

   @Test
   public void specifyValuesForAnnotationAttributes(final MyAnnotation a)
   {
      assertSame(MyAnnotation.class, a.annotationType());

      new Expectations()
      {
         {
            a.flag(); returns(false);
            a.value(); returns("test");
            a.values(); returns(new String[] {"abc", "dEf"});
         }
      };

      assertFalse(a.flag());
      assertEquals("test", a.value());
      assertArrayEquals(new String[] {"abc", "dEf"}, a.values());
   }

   @Test
   public void verifyUsesOfAnnotationAttributes(final MyAnnotation a)
   {
      new NonStrictExpectations()
      {
         {
            a.value(); returns("test");
            a.values(); returns(new String[] {"abc", "dEf"});
         }
      };

      // Same rule for regular methods applies (ie, if no return value was recorded, invocations
      // will get the default for the return type).
      assertFalse(a.flag());

      assertEquals("test", a.value());
      assertArrayEquals(new String[] {"abc", "dEf"}, a.values());
      a.value();

      new FullVerifications()
      {
         {
            // Mocked methods called here always return the default value according to return type.
            a.flag();
            a.value(); repeats(2);
            a.values();
         }
      };
   }

   @Test
   public void mockASingleAnnotationAttribute(@Mocked("value") final MyAnnotation a)
   {
      new Expectations()
      {
         {
            a.value(); returns("test");
         }
      };

      assertFalse(a.flag());
      assertEquals("test", a.value());
      assertEquals(0, a.values().length);
   }
}