/*
 * Copyright (c) 2003-2007 OFFIS, Henri Tremblay.
 * This program is made available under the terms of the MIT License.
 */
package org.easymock.classextension.samples;

import java.lang.reflect.*;
import java.math.*;

import org.junit.*;
import static org.junit.Assert.*;

import org.easymock.classextension.*;
import static org.easymock.classextension.EasyMock.*;

public final class ConstructorCalledMockTest
{
   private TaxCalculator tc;

   @Before
   public void setUp()
   {
      // Get the one and only constructor:
      Constructor<?> constructor = TaxCalculator.class.getDeclaredConstructors()[0];
      BigDecimal[] taxValues = {new BigDecimal("5"), new BigDecimal("15")};
      ConstructorArgs constructorArgs = new ConstructorArgs(constructor, (Object) taxValues);

      // No need to specify any methods, abstract ones are mocked by default:
      tc = createMock(TaxCalculator.class, constructorArgs);
   }

   @After
   public void tearDown()
   {
      verify(tc);
   }

   @Test
   public void testTax()
   {
      expect(tc.rate()).andStubReturn(new BigDecimal("0.20"));
      replay(tc);

      assertEquals(new BigDecimal("4.00"), tc.tax());
   }

   @Test
   public void testTax_ZeroRate()
   {
      expect(tc.rate()).andStubReturn(BigDecimal.ZERO);
      replay(tc);

      assertEquals(BigDecimal.ZERO, tc.tax());
   }
}
