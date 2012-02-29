/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import java.text.*;
import java.util.*;

import static org.junit.Assert.*;
import org.junit.*;

public final class MockingNewInstancesWithVaryingBehaviorTest
{
   private static final String DATE_FORMAT = "yyyy-MM-dd";
   private static final String FORMATTED_DATE = "2012-02-28";

   private static final String TIME_FORMAT = "HH";
   private static final String FORMATTED_TIME = "13";

   private void exerciseAndVerifyTestedCode()
   {
      Date now = new Date();

      String hour = new SimpleDateFormat(TIME_FORMAT).format(now);
      assertEquals(FORMATTED_TIME, hour);

      String date = new SimpleDateFormat(DATE_FORMAT).format(now);
      assertEquals(FORMATTED_DATE, date);
   }

   /// Tests using the Mockups API ////////////////////////////////////////////////////////////////////////////////////

   DateFormat dateFormat;
   DateFormat hourFormat;

   @Test
   public void usingMockUpsWithItField()
   {
      new MockUp<SimpleDateFormat>() {
         SimpleDateFormat it;

         @Mock
         void $init(String pattern)
         {
            if (DATE_FORMAT.equals(pattern)) dateFormat = it;
            else if (TIME_FORMAT.equals(pattern)) hourFormat = it;
         }
      };

      new MockUp<DateFormat>() {
         DateFormat it;

         @Mock
         String format(Date d)
         {
            assert d != null;
            if (it == dateFormat) return FORMATTED_DATE;
            else if (it == hourFormat) return FORMATTED_TIME;
            else return null;
         }
      };

      exerciseAndVerifyTestedCode();
   }

   @Test
   public void usingMockUpsWithInvocationParameter()
   {
      new MockUp<SimpleDateFormat>() {
         @Mock
         void $init(Invocation inv, String pattern)
         {
            DateFormat dt = inv.getInvokedInstance();
            if (DATE_FORMAT.equals(pattern)) dateFormat = dt;
            else if (TIME_FORMAT.equals(pattern)) hourFormat = dt;
         }
      };

      new MockUp<DateFormat>() {
         @Mock
         String format(Invocation inv, Date d)
         {
            assert d != null;
            DateFormat dt = inv.getInvokedInstance();
            if (dt == dateFormat) return FORMATTED_DATE;
            else if (dt == hourFormat) return FORMATTED_TIME;
            else return null;
         }
      };

      exerciseAndVerifyTestedCode();
   }

   /// Tests using the Expectations API ///////////////////////////////////////////////////////////////////////////////

   @Test // not too complex, but inelegant
   public void usingPartialMockingAndDelegate(final SimpleDateFormat sdf)
   {
      new NonStrictExpectations(SimpleDateFormat.class) {{
         sdf.format((Date) any);
         result = new Delegate() {
            String format(Invocation inv)
            {
               String pattern = inv.<SimpleDateFormat>getInvokedInstance().toPattern();
               if (DATE_FORMAT.equals(pattern)) return FORMATTED_DATE;
               else if (TIME_FORMAT.equals(pattern)) return FORMATTED_TIME;
               else return null;
            }
         };
      }};

      exerciseAndVerifyTestedCode();
   }

   final Map<Object, String> formats = new IdentityHashMap<Object, String>();

   final class SDFFormatDelegate implements Delegate<Object>
   {
      final String format;
      SDFFormatDelegate(String format) { this.format = format; }

      @SuppressWarnings("UnusedDeclaration")
      void saveInstance(Invocation inv) { formats.put(inv.getInvokedInstance(), format); }
   }

   @Test // too complex
   public void usingDelegates()
   {
      new NonStrictExpectations() {
         @Mocked SimpleDateFormat mockSDF;

         {
            new SimpleDateFormat(DATE_FORMAT); result = new SDFFormatDelegate(FORMATTED_DATE);
            new SimpleDateFormat(TIME_FORMAT); result = new SDFFormatDelegate(FORMATTED_TIME);

            mockSDF.format((Date) any);
            result = new Delegate() {String format(Invocation inv) { return formats.get(inv.getInvokedInstance()); }};
         }
      };

      exerciseAndVerifyTestedCode();
   }

   @Test // nice TODO: doesn't work yet, add support
   public void usingCapturingOnFinalMockFields()
   {
      new NonStrictExpectations() {
         @Capturing final SimpleDateFormat dateFmt;
         @Capturing final SimpleDateFormat hourFmt;

         {
            dateFmt = new SimpleDateFormat(DATE_FORMAT);
            dateFmt.format((Date) any); result = FORMATTED_DATE;

            hourFmt = new SimpleDateFormat(TIME_FORMAT);
            hourFmt.format((Date) any); result = FORMATTED_TIME;
         }
      };

      exerciseAndVerifyTestedCode();
   }
}
