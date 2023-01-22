package com.example.demo.service;

import com.example.demo.api.request.CryptoRequest;
import com.example.demo.dto.CryptoCurrencyDto;
import com.example.demo.helpers.CryptosUrlBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static com.example.demo.service.CryptoCurrenciesService.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CryptoCurrenciesServiceTest {
    private final RestTemplate restTemplate = mock(RestTemplate.class);
    private final CryptosUrlBuilder cryptosUrlBuilder = new CryptosUrlBuilder();

    @Spy
    private CryptoCurrenciesService cryptoCurrenciesService = new CryptoCurrenciesService(restTemplate, cryptosUrlBuilder);

    @Test
    public void getPercentChange_whenCryptosAndPastDateAreValid_shouldReturnListOfCryptoDtos() throws Exception {
        // Given
        LocalDate pastDate = LocalDate.of(2022, 1, 1);
        Set<String> cryptoCurrencies = new HashSet<>(Arrays.asList("BTC", "ETH", "BNB"));
        CryptoRequest request = new CryptoRequest(cryptoCurrencies, pastDate);

        Map<String, Map<String, Double>> historicalPrices = new HashMap<>();
        Map<String, Double> btcEthPrices = new HashMap<>();
        btcEthPrices.put("BTC", 1.0 / 10000.0);
        btcEthPrices.put("ETH", 1.0 / 500.0);
        btcEthPrices.put("BNB", 1.0 / 200.0);
        historicalPrices.put("USD", btcEthPrices);

        Map<String, Double> currentPrices = new HashMap<>();
        currentPrices.put("BTC", 1.0 / 12000.0);
        currentPrices.put("ETH", 1.0 / 800.0);
        currentPrices.put("BNB", 1.0 / 100.0);

        String historicalUrl = cryptosUrlBuilder.buildApiUrl(CRYPTOCOMPARE_HISTORICAL_API_URL, String.join(",", cryptoCurrencies), pastDate);
        String currentUrl = cryptosUrlBuilder.buildApiUrl(CRYPTOCOMPARE_NOW_API_URL, String.join(",", cryptoCurrencies), null);
        ResponseEntity<Map<String, Map<String, Double>>> historicalResponse = new ResponseEntity<>(historicalPrices, HttpStatus.OK);
        ResponseEntity<Map<String, Double>> currentResponse = new ResponseEntity<>(currentPrices, HttpStatus.OK);

        when(restTemplate.exchange(eq(historicalUrl), eq(HttpMethod.GET), any(), eq(new ParameterizedTypeReference<Map<String, Map<String, Double>>>() {}))).thenReturn(historicalResponse);
        when(restTemplate.exchange(eq(currentUrl), eq(HttpMethod.GET), any(), eq(new ParameterizedTypeReference<Map<String, Double>>() {}))).thenReturn(currentResponse);

        // When
        List<CryptoCurrencyDto> result = cryptoCurrenciesService.getRates(request);

        // Then
        verify(restTemplate, times(1)).exchange(historicalUrl, HttpMethod.GET, null, new ParameterizedTypeReference<Map<String, Map<String, Double>>>() {});
        verify(restTemplate, times(1)).exchange(currentUrl, HttpMethod.GET, null, new ParameterizedTypeReference<Map<String, Double>>() {});
        assertThat(result.get(0).getSymbol()).isEqualTo("BTC");
        assertThat(result.get(0).getHistoricalPrice()).isEqualTo(new BigDecimal("10000.0"));
        assertThat(result.get(0).getCurrentPrice()).isEqualTo(new BigDecimal("12000.0"));
        assertThat(result.get(0).getPercentChange().intValue()).isEqualTo(20);
        assertThat(result.get(1).getSymbol()).isEqualTo("BNB");
        assertThat(result.get(1).getHistoricalPrice()).isEqualTo(new BigDecimal("200.0"));
        assertThat(result.get(1).getCurrentPrice()).isEqualTo(new BigDecimal("100.0"));
        assertThat(result.get(1).getPercentChange().intValue()).isEqualTo(-50);
        assertThat(result.get(2).getSymbol()).isEqualTo("ETH");
        assertThat(result.get(2).getHistoricalPrice()).isEqualTo(new BigDecimal("500.0"));
        assertThat(result.get(2).getCurrentPrice()).isEqualTo(new BigDecimal("800.0"));
        assertThat(result.get(2).getPercentChange().intValue()).isEqualTo(60);
    }
}
