package com.fundraising.service;

import com.fundraising.enums.Currency;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Service("staticCurrencyConverter")
public class CurrencyConversionService implements CurrencyConverter {

    // Direct exchange rates between all currency pairs
    private static final Map<String, BigDecimal> EXCHANGE_RATES = Map.of(
            // From USD
            "USD_EUR", new BigDecimal("0.85"),
            "USD_GBP", new BigDecimal("0.75"),
            // From EUR
            "EUR_USD", new BigDecimal("1.18"),
            "EUR_GBP", new BigDecimal("0.88"),
            // From GBP
            "GBP_USD", new BigDecimal("1.33"),
            "GBP_EUR", new BigDecimal("1.13")
    );

    /**
     * Converts amount from source currency to target currency using direct exchange rates
     * @param amount Amount to convert
     * @param fromCurrency Source currency
     * @param toCurrency Target currency
     * @return Converted amount rounded to 2 decimal places
     */
    public BigDecimal convert(BigDecimal amount, Currency fromCurrency, Currency toCurrency) {
        if (fromCurrency == toCurrency) {
            return amount;
        }

        String rateKey = fromCurrency.name() + "_" + toCurrency.name();
        BigDecimal rate = EXCHANGE_RATES.get(rateKey);

        if (rate == null) {
            throw new IllegalArgumentException("Exchange rate not found for " + fromCurrency + " to " + toCurrency);
        }

        BigDecimal convertedAmount = amount.multiply(rate);
        return convertedAmount.setScale(2, RoundingMode.HALF_UP);
    }
}