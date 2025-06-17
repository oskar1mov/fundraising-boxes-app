package com.fundraising.service;

import com.fundraising.enums.Currency;
import java.math.BigDecimal;

/**
 * Interface for currency conversion services
 */
public interface CurrencyConverter {

    /**
     * Converts amount from source currency to target currency
     * @param amount Amount to convert
     * @param fromCurrency Source currency
     * @param toCurrency Target currency
     * @return Converted amount rounded to 2 decimal places
     */
    BigDecimal convert(BigDecimal amount, Currency fromCurrency, Currency toCurrency);
}