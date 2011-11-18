/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package fakes;

import java.io.*;
import java.math.*;
import java.net.*;
import java.util.*;

public final class CurrencyConversion2
{
   private final String url;

   public static List<String> currencySymbols()
   {
      return Arrays.asList("USD", "BRL", "CNY");
   }

   public CurrencyConversion2(String fromCurrency, String toCurrency)
   {
      validateSymbol("from", fromCurrency);
      validateSymbol("to", toCurrency);
      url =
         "http://www.gocurrency.com/v2/dorate.php?inV=1&Calculate=Convert&from=" + fromCurrency + "&to=" + toCurrency;
   }

   private void validateSymbol(String whichOne, String currencySymbol)
   {
      if (!currencySymbols().contains(currencySymbol)) {
         throw new IllegalArgumentException("Invalid " + whichOne + " currency: " + currencySymbol);
      }
   }

   public BigDecimal getConversionRate()
   {
      InputStream response;

      try {
         response = new URL(url).openStream();
      }
      catch (IOException e) {
         throw new RuntimeException(e);
      }

      Scanner s = new Scanner(response).skip("(?s).*<div id=\"converter_results\">");
      String innermostHtml = s.findWithinHorizon("<b>.+</b>", 0);

      String[] parts = innermostHtml.split("\\s*=\\s*");
      String value = parts[1].split(" ")[0];

      return new BigDecimal(value);
   }
}
