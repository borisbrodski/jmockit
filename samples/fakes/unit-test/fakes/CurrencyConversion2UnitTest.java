/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package fakes;

import java.io.*;
import java.math.*;
import java.net.*;
import java.util.*;

import org.junit.*;
import static org.junit.Assert.*;

import mockit.*;

public final class CurrencyConversion2UnitTest
{
   final List<String> validCurrencies = CurrencyConversion2.currencySymbols();
   final String fromCurrency = validCurrencies.get(0);
   final String toCurrency = validCurrencies.get(1);
   String html = "<div id=\"converter_results\"><ul><li><b>1 X = 1.3 Y</b>";

   @Test
   public void convertFromOneCurrencyToAnother() throws Exception
   {
      new Expectations() {
         URL url;

         {
            new URL(withMatch(".+&from=" + fromCurrency + "&to=" + toCurrency + ".*")).openStream();
            result = new ByteArrayInputStream(html.getBytes());
         }
      };

      BigDecimal rate = new CurrencyConversion2(fromCurrency, toCurrency).getConversionRate();

      assertEquals("1.3", rate.toPlainString());
   }

   @Test
   public void convertFromOneCurrencyToAnother_shorterButIncomplete()
   {
      // A more complicated HTML fragment, for some variation.
      html =
         "<h4>Conversion rate</h4>\r\n" +
         "<div id=\"converter_results\"><ul><li>\r\n" +
         "   <b>1 X = 0.15 Y</b>\r\n" +
         "</li></ul></div>";

      // Note that this test does not verify proper creation of the URL, nor that the
      // remote site actually gets accessed.
      new Expectations() {
         @Mocked URL url;
         @Input InputStream content = new ByteArrayInputStream(html.getBytes());
      };

      BigDecimal rate = new CurrencyConversion2(fromCurrency, toCurrency).getConversionRate();

      assertEquals("0.15", rate.toPlainString());
   }
}
