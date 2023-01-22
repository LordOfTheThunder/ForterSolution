package com.example.demo.configuration;

import com.example.demo.converters.StringToOperationsConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final StringToOperationsConverter stringToOperationsConverter;

    public WebConfig(StringToOperationsConverter stringToOperationsConverter) {
        this.stringToOperationsConverter = stringToOperationsConverter;
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(stringToOperationsConverter);
    }
}
