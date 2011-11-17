/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package fakes;

import java.io.*;
import java.math.*;
import java.util.*;

import org.apache.http.*;
import org.apache.http.client.*;
import org.junit.*;
import static org.junit.Assert.*;

import mockit.*;

public final class CurrencyConversionUnitTest
{
   final List<String> validCurrencies = CurrencyConversion.currencySymbols();

   @Test
   public void convertFromOneCurrencyToAnother() throws Exception
   {
      String fromCurrency = validCurrencies.get(0);
      String toCurrency = validCurrencies.get(1);
      final InputStream httpResponseContent =
         new ByteArrayInputStream("<div id=\"converter_results\"><ul><li><b>1 X = 1.3 Y</b>".getBytes());

      new Expectations() {
         @Capturing // so that any class implementing the base (interface) type gets mocked
         @Cascading // so that intermediate objects obtained from chained calls get mocked
         HttpClient httpClient;

         HttpEntity httpEntity; // provides access to the intermediate (cascaded) object

         {
            httpEntity.getContent();
            result = httpResponseContent;
         }
      };

      BigDecimal rate = CurrencyConversion.convertFromTo(fromCurrency, toCurrency);

      assertEquals("1.3", rate.toPlainString());
   }

   @Test
   public void convertFromOneCurrencyToAnother_shorter()
   {
      String fromCurrency = validCurrencies.get(0);
      String toCurrency = validCurrencies.get(1);

      new Expectations() {
         @Capturing @Cascading HttpClient httpClient;
         @Input InputStream content =
            new ByteArrayInputStream("<div id=\"converter_results\"><ul><li><b>1 X = 0.15 Y</b>".getBytes());
      };

      BigDecimal rate = CurrencyConversion.convertFromTo(fromCurrency, toCurrency);

      assertEquals("0.15", rate.toPlainString());
   }
}
