package com.example.demo.helpers;

import com.example.demo.service.CryptoCurrenciesService;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.time.ZoneOffset;

@Controller
public class CryptosUrlBuilder {
    public String buildApiUrl(String urlPrefix, String crypto, LocalDate pastDate) {
        StringBuilder url = new StringBuilder(urlPrefix);
        url.append("?fsym=").append(CryptoCurrenciesService.SYMBOL_USD).append("&tsyms=").append(crypto);
        if (pastDate != null) {
            url.append("&ts=").append(pastDate.atStartOfDay(ZoneOffset.UTC).toEpochSecond());
        }
        return url.toString();
    }
}
