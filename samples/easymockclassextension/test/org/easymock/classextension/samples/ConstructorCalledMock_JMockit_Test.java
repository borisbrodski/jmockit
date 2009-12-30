/*
 * JMockit Samples
 * Copyright (c) 2007-2009 Rogério Liesenfeld
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
package org.easymock.classextension.samples;

import java.math.*;

import org.junit.*;

import mockit.*;

import static org.junit.Assert.*;

public final class ConstructorCalledMock_JMockit_Test
{
   @Mocked(methods = "rate", constructorArgsMethod = "taxCalculatorValues")
   private TaxCalculator tc;

   @SuppressWarnings({"UnusedDeclaration"})
   private Object[] taxCalculatorValues(BigDecimal... values)
   {
      BigDecimal[] taxValues = {new BigDecimal("5"), new BigDecimal("15")};
      return new Object[] {taxValues};
   }

   @Test
   public void testTax()
   {
      new Expectations()
      {
         {
            tc.rate(); result = new BigDecimal("0.20");
         }
      };

      assertEquals(new BigDecimal("4.00"), tc.tax());
   }

   @Test
   public void testTax_ZeroRate()
   {
      new Expectations()
      {
         {
            tc.rate(); result = BigDecimal.ZERO;
         }
      };

      assertEquals(BigDecimal.ZERO, tc.tax());
   }
}
