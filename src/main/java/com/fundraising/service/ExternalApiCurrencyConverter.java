package com.fundraising.service;

import com.fundraising.dto.ExchangeRateResponse;
import com.fundraising.enums.Currency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;

/**
 * Currency converter that uses external API for real-time exchange rates
 * Falls back to static rates if external API is unavailable
 */
@Service("externalApiCurrencyConverter")
public class ExternalApiCurrencyConverter implements CurrencyConverter {

    private static final Logger logger = LoggerFactory.getLogger(ExternalApiCurrencyConverter.class);
    private static final String API_BASE_URL = "https://api.exchangerate-api.com/v4/latest/";
    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    private final WebClient webClient;
    private final CurrencyConverter fallbackConverter;

    public ExternalApiCurrencyConverter(WebClient.Builder webClientBuilder,
                                        @Qualifier("staticCurrencyConverter") CurrencyConverter fallbackConverter) {
        this.webClient = webClientBuilder
                .baseUrl(API_BASE_URL)
                .build();
        this.fallbackConverter = fallbackConverter;
    }

    @Override
    public BigDecimal convert(BigDecimal amount, Currency fromCurrency, Currency toCurrency) {
        if (fromCurrency == toCurrency) {
            return amount;
        }

        try {
            return convertUsingExternalApi(amount, fromCurrency, toCurrency);
        } catch (Exception e) {
            logger.warn("External API conversion failed for {} to {}: {}. Falling back to static rates.",
                    fromCurrency, toCurrency, e.getMessage());
            return fallbackConverter.convert(amount, fromCurrency, toCurrency);
        }
    }

    private BigDecimal convertUsingExternalApi(BigDecimal amount, Currency fromCurrency, Currency toCurrency) {
        try {
            ExchangeRateResponse response = webClient
                    .get()
                    .uri(fromCurrency.name())
                    .retrieve()
                    .bodyToMono(ExchangeRateResponse.class)
                    .timeout(TIMEOUT)
                    .block();

            if (response == null || response.getRates() == null) {
                throw new RuntimeException("Invalid response from exchange rate API");
            }

            BigDecimal rate = response.getRates().get(toCurrency.name());
            if (rate == null) {
                throw new RuntimeException("Exchange rate not found for " + toCurrency);
            }

            BigDecimal convertedAmount = amount.multiply(rate);
            return convertedAmount.setScale(2, RoundingMode.HALF_UP);

        } catch (WebClientResponseException e) {
            throw new RuntimeException("API request failed with status: " + e.getStatusCode(), e);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error during API call", e);
        }
    }

}