package fakes;

import java.io.*;
import java.math.*;
import java.util.*;

import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;

public final class CurrencyConversion
{
   public static List<String> currencySymbols()
   {
      return Arrays.asList("USD", "BRL", "CNY");
   }

   public static BigDecimal convertFromTo(String fromCurrency, String toCurrency)
   {
      List<String> valid = currencySymbols();

      if (!valid.contains(fromCurrency)) {
         throw new IllegalArgumentException("Invalid from currency: " + fromCurrency);
      }

      if (!valid.contains(toCurrency)) {
         throw new IllegalArgumentException("Invalid to currency: " + toCurrency);
      }

      String url =
         "http://www.gocurrency.com/v2/dorate.php?inV=1&from=" + fromCurrency +
         "&to=" + toCurrency + "&Calculate=Convert";

      try {
         HttpClient httpclient = new DefaultHttpClient();
         HttpGet httpget = new HttpGet(url);
         HttpResponse response = httpclient.execute(httpget);
         HttpEntity entity = response.getEntity();
         StringBuilder result = new StringBuilder();

         if (entity != null) {
            InputStream inStream = entity.getContent();
            InputStreamReader irs = new InputStreamReader(inStream);
            BufferedReader br = new BufferedReader(irs);
            String l;

            while ((l = br.readLine()) != null) {
               result.append(l);
            }
         }

         String theWholeThing = result.toString();
         int start = theWholeThing.lastIndexOf("<div id=\"converter_results\"><ul><li>");
         String substring = result.substring(start);
         int startOfInterestingStuff = substring.indexOf("<b>") + 3;
         int endOfInterestingStuff = substring.indexOf("</b>", startOfInterestingStuff);
         String interestingStuff = substring.substring(startOfInterestingStuff, endOfInterestingStuff);
         String[] parts = interestingStuff.split("=");
         String value = parts[1].trim().split(" ")[0];
         BigDecimal bottom = new BigDecimal(value);
         return bottom;
      }
      catch (IOException e) {
         throw new RuntimeException(e);
      }
   }
}
