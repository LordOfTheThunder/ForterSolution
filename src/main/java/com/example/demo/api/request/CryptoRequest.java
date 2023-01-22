package com.example.demo.api.request;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
@AllArgsConstructor
public class CryptoRequest {
    private Set<String> cryptoCurrencies;
    private LocalDate pastDate;
}
