package fakes;

import java.math.*;
import java.util.*;

import org.junit.*;
import static org.junit.Assert.*;

// Run with "-Djmockit-mocks=fakes.CurrencyConversionHttpClientFake" for fake Web site access.
public final class CurrencyConversionIntegrationTest
{
   final List<String> validCurrencies = CurrencyConversion.currencySymbols();

   @Test(expected = IllegalArgumentException.class)
   public void convertFromInvalidCurrency()
   {
      CurrencyConversion.convertFromTo("invalid", validCurrencies.get(0));
   }

   @Test(expected = IllegalArgumentException.class)
   public void convertToInvalidCurrency()
   {
      CurrencyConversion.convertFromTo(validCurrencies.get(0), "invalid");
   }

   @Test
   public void convertToSameCurrency()
   {
      String currency = validCurrencies.get(0);

      BigDecimal identityRate = CurrencyConversion.convertFromTo(currency, currency);

      assertEquals(BigDecimal.ONE, identityRate);
   }

   @Test
   public void convertFromDollarToCheapCurrency()
   {
      double rate = CurrencyConversion.convertFromTo("USD", "CNY").doubleValue();

      assertTrue(rate > 1.0);
   }

   @Test
   public void convertFromOneCurrencyToAnother()
   {
      String fromCurrency = validCurrencies.get(0);
      String toCurrency = validCurrencies.get(1);

      BigDecimal rate = CurrencyConversion.convertFromTo(fromCurrency, toCurrency);
      assertTrue(rate.doubleValue() > 0.0);

      BigDecimal inverseRate = CurrencyConversion.convertFromTo(toCurrency, fromCurrency);
      assertTrue(inverseRate.doubleValue() > 0.0);

      assertEquals(1.0, rate.multiply(inverseRate).doubleValue(), 0.005);
   }
}
