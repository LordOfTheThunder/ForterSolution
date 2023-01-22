package com.example.demo.api;

import com.example.demo.api.request.CryptoRequest;
import com.example.demo.dto.CryptoCurrencyDto;
import com.example.demo.service.CryptoCurrenciesService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CryptoCurrenciesController.class)
@AutoConfigureMockMvc
public class CryptoCurrenciesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CryptoCurrenciesService cryptoCurrenciesService;

    @Test
    public void testGetRates() throws Exception {
        LocalDate pastDate = LocalDate.now().minusDays(1);
        Set<String> cryptos = Set.of("BTC", "ETH", "BNB");
        CryptoRequest request = new CryptoRequest(cryptos, pastDate);
        BigDecimal historicalPrice = new BigDecimal("10000");
        BigDecimal currentPrice = new BigDecimal("20000");
        BigDecimal percentChange = new BigDecimal("100");
        CryptoCurrencyDto cryptoCurrencyDto = new CryptoCurrencyDto("BTC", historicalPrice, currentPrice, percentChange);
        List<CryptoCurrencyDto> cryptoCurrencyDtos = Collections.singletonList(cryptoCurrencyDto);
        when(cryptoCurrenciesService.getRates(request)).thenReturn(cryptoCurrencyDtos);
        String expectedResult = cryptoCurrenciesService.convertListOfResultsToString(cryptoCurrencyDtos);
        if (expectedResult == null) expectedResult = "";

        mockMvc.perform(get("/api/v1/cryptocurrencies/rates")
                        .param("op", "compareToPast")
                        .param("currencies", String.join(",", cryptos))
                        .param("pastDate", pastDate.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResult));

        verify(cryptoCurrenciesService, times(1)).getRates(request);
    }

    @Test
    public void getRates_whenPastDateIsInFuture_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/cryptocurrencies/rates")
                        .param("op", "compareToPast")
                        .param("currencies", "BTC,ETH")
                        .param("pastDate", LocalDate.now().plusDays(1).toString()))
                .andExpect(status().isBadRequest());

        verify(cryptoCurrenciesService, never()).getRates(any());
    }

    @Test
    public void getRates_whenCurrenciesListIsEmpty_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/cryptocurrencies/rates")
                        .param("op", "compareToPast")
                        .param("pastDate", LocalDate.now().minusDays(1).toString()))
                .andExpect(status().isBadRequest());

        verify(cryptoCurrenciesService, never()).getRates(any());
    }

    @Test
    public void getRates_whenPastDateIsEmpty_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/cryptocurrencies/rates")
                        .param("op", "compareToPast")
                        .param("currencies", "BTC,ETH"))
                .andExpect(status().isBadRequest());

        verify(cryptoCurrenciesService, never()).getRates(any());
    }

    @Test
    public void getRates_whenOpIsInvalid_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/cryptocurrencies/rates")
                        .param("op", "invalidOp")
                        .param("currencies", "BTC,ETH")
                        .param("pastDate", LocalDate.now().minusDays(1).toString()))
                .andExpect(status().isBadRequest());

        verify(cryptoCurrenciesService, never()).getRates(any());
    }

}
