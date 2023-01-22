package com.example.demo.service;

import com.example.demo.api.request.CryptoRequest;
import com.example.demo.dto.CryptoCurrencyDto;
import com.example.demo.exceptions.ExternalApiException;
import com.example.demo.helpers.CryptosUrlBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CryptoCurrenciesService {

    private static final String CRYPTOCOMPARE_API_PREFIX = "https://min-api.cryptocompare.com/data";
    public static final String CRYPTOCOMPARE_NOW_API_URL = CRYPTOCOMPARE_API_PREFIX + "/price";
    public static final String CRYPTOCOMPARE_HISTORICAL_API_URL = CRYPTOCOMPARE_API_PREFIX + "/pricehistorical";
    public static final String SYMBOL_USD = "USD";
    private static final String ERROR_MESSAGE = "Error while fetching %s prices from CryptoCompare API";

    private final RestTemplate restTemplate;
    private final CryptosUrlBuilder cryptosUrlBuilder;

    private final Logger logger = LoggerFactory.getLogger(CryptoCurrenciesService.class);

    public CryptoCurrenciesService(RestTemplate restTemplate, CryptosUrlBuilder cryptosUrlBuilder) {
        this.restTemplate = restTemplate;
        this.cryptosUrlBuilder = cryptosUrlBuilder;
    }

    public List<CryptoCurrencyDto> getRates(CryptoRequest request) {
        List<CryptoCurrencyDto> cryptoCurrencyDtos = new ArrayList<>();
        Map<String, Double> historicalPrices = fetchHistoricalPrices(request.getCryptoCurrencies(), request.getPastDate());
        Map<String, Double> currentPrices = fetchCurrentPrices(request.getCryptoCurrencies());
        for (String crypto : request.getCryptoCurrencies()) {
            if (historicalPrices.get(crypto) == null || currentPrices.get(crypto) == null) {
                logger.error("No info found on crypto {}. Ignoring it", crypto);
                continue;
            }
            BigDecimal historicalPrice = BigDecimal.valueOf(1 / historicalPrices.get(crypto));
            BigDecimal currentPrice = BigDecimal.valueOf(1 / currentPrices.get(crypto));
            BigDecimal percentChange = calculatePercentChange(historicalPrice, currentPrice);
            cryptoCurrencyDtos.add(new CryptoCurrencyDto(crypto, historicalPrice, currentPrice, percentChange));
        }
        return cryptoCurrencyDtos;
    }

    public String convertListOfResultsToString(List<CryptoCurrencyDto> cryptoCurrencyDtos) {
        cryptoCurrencyDtos.sort(Comparator.comparing(CryptoCurrencyDto::getPercentChange).reversed());
        return cryptoCurrencyDtos.stream().map(CryptoCurrencyDto::toString).collect(Collectors.joining(", "));
    }

    // A thought - I don't like the duplication in fetchHistoricalPrices and fetchCurrentPrices
    // but moving the shared code to a shared generic method caused problems with mockito in the service test
    // So for the sake of this exercise I decided to leave it as is, although, hate it.
    private Map<String, Double> fetchHistoricalPrices(Set<String> cryptos, LocalDate pastDate) {
        String tsyms = String.join(",", cryptos);
        String url = cryptosUrlBuilder.buildApiUrl(CRYPTOCOMPARE_HISTORICAL_API_URL, tsyms, pastDate);
        try {
            ResponseEntity<Map<String, Map<String, Double>>> response = restTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<>() {});
            return Objects.requireNonNull(response.getBody()).get(SYMBOL_USD);
        } catch (Exception e) {
            throw new ExternalApiException(String.format(ERROR_MESSAGE, SYMBOL_USD), e);
        }
    }

    private Map<String, Double> fetchCurrentPrices(Set<String> cryptos) {
        String tsyms = String.join(",", cryptos);
        String url = cryptosUrlBuilder.buildApiUrl(CRYPTOCOMPARE_NOW_API_URL, tsyms, null);
        try {
            ResponseEntity<Map<String, Double>> response = restTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<>() {
            });
            return response.getBody();
        } catch (Exception e) {
            throw new ExternalApiException(String.format(ERROR_MESSAGE, SYMBOL_USD), e);
        }
    }

    private BigDecimal calculatePercentChange(BigDecimal historicalPrice, BigDecimal currentPrice) {
        return currentPrice.subtract(historicalPrice).divide(historicalPrice, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
    }
}
