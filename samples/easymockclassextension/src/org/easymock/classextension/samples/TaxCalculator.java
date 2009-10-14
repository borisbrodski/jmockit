/*
 * Copyright (c) 2003-2007 OFFIS, Henri Tremblay.
 * This program is made available under the terms of the MIT License.
 */
package org.easymock.classextension.samples;

import java.math.*;

/**
 * Class to test and partially mock.
 */
public abstract class TaxCalculator
{
   private final BigDecimal[] values;

   protected TaxCalculator(BigDecimal... values)
   {
      this.values = values;
   }

   protected abstract BigDecimal rate();

   public final BigDecimal tax()
   {
      BigDecimal result = BigDecimal.ZERO;

      for (BigDecimal d : values) {
         result = result.add(d);
      }

      return result.multiply(rate());
   }
}
