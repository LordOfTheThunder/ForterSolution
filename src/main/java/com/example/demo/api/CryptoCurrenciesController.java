package com.example.demo.api;

import com.example.demo.api.request.CryptoRequest;
import com.example.demo.dto.CryptoCurrencyDto;
import com.example.demo.dto.Operations;
import com.example.demo.service.CryptoCurrenciesService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("api/v1/cryptocurrencies")
@Validated
public class CryptoCurrenciesController {
    private final CryptoCurrenciesService cryptoCurrenciesService;

    @Autowired
    public CryptoCurrenciesController(CryptoCurrenciesService cryptoCurrenciesService) {
        this.cryptoCurrenciesService = cryptoCurrenciesService;
    }

    @GetMapping("rates")
    public ResponseEntity<String> getRates(
            @Valid
            @NotNull
            @RequestParam(value = "op") Operations operation,
            @Valid
            @NotEmpty
            @RequestParam(value = "currencies") Set<String> cryptoCurrencies,
            @Valid
            @Past
            @NotNull
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate pastDate
    ) {
        // For now we ignore the operation param in the handler because we only handle one operation
        // Once there are more operations we will refactor the code to act based on the operation
        CryptoRequest request = new CryptoRequest(cryptoCurrencies, pastDate);
        List<CryptoCurrencyDto> cryptoCurrencyDtos = cryptoCurrenciesService.getRates(request);
        return new ResponseEntity<>(cryptoCurrenciesService.convertListOfResultsToString(cryptoCurrencyDtos), HttpStatus.OK);
    }
}
