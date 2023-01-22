package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@AllArgsConstructor
@Data
public class CryptoCurrencyDto {
    private String symbol;
    private BigDecimal historicalPrice;
    private BigDecimal currentPrice;
    private BigDecimal percentChange;

    @Override
    public String toString() {
        return String.format("%s: %.2f%%", symbol, percentChange);
    }
}
