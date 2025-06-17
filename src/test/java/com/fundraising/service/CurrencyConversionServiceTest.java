package com.fundraising.service;

import com.fundraising.enums.Currency;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.*;

class CurrencyConversionServiceTest {

    private CurrencyConversionService service;

    @BeforeEach
    void setUp() {
        service = new CurrencyConversionService();
    }

    @Test
    void shouldReturnSameAmountForSameCurrency() {
        BigDecimal amount = new BigDecimal("100.00");
        BigDecimal result = service.convert(amount, Currency.USD, Currency.USD);
        assertEquals(amount, result);
    }

    @Test
    void shouldConvertUsdToEur() {
        BigDecimal amount = new BigDecimal("100.00");
        BigDecimal result = service.convert(amount, Currency.USD, Currency.EUR);
        BigDecimal expected = new BigDecimal("85.00");
        assertEquals(expected, result);
    }

    @Test
    void shouldConvertUsdToGbp() {
        BigDecimal amount = new BigDecimal("100.00");
        BigDecimal result = service.convert(amount, Currency.USD, Currency.GBP);
        BigDecimal expected = new BigDecimal("75.00");
        assertEquals(expected, result);
    }

    @Test
    void shouldConvertEurToUsd() {
        BigDecimal amount = new BigDecimal("100.00");
        BigDecimal result = service.convert(amount, Currency.EUR, Currency.USD);
        BigDecimal expected = new BigDecimal("118.00");
        assertEquals(expected, result);
    }

    @Test
    void shouldConvertGbpToUsd() {
        BigDecimal amount = new BigDecimal("100.00");
        BigDecimal result = service.convert(amount, Currency.GBP, Currency.USD);
        BigDecimal expected = new BigDecimal("133.00");
        assertEquals(expected, result);
    }

    @Test
    void shouldRoundToTwoDecimalPlaces() {
        BigDecimal amount = new BigDecimal("33.33");
        BigDecimal result = service.convert(amount, Currency.USD, Currency.EUR);
        // 33.33 * 0.85 = 28.3305, should round to 28.33
        BigDecimal expected = new BigDecimal("28.33");
        assertEquals(expected, result);
    }

    @Test
    void shouldHandleZeroAmount() {
        BigDecimal amount = BigDecimal.ZERO;
        BigDecimal result = service.convert(amount, Currency.USD, Currency.EUR);
        assertEquals(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), result);
    }

    @Test
    void shouldHandleVerySmallAmounts() {
        BigDecimal amount = new BigDecimal("0.01");
        BigDecimal result = service.convert(amount, Currency.USD, Currency.EUR);
        BigDecimal expected = new BigDecimal("0.01"); // 0.01 * 0.85 = 0.0085, rounds to 0.01
        assertEquals(expected, result);
    }
}