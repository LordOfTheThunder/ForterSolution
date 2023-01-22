package com.example.demo.converters;

import com.example.demo.dto.Operations;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToOperationsConverter implements Converter<String, Operations> {
    @Override
    public Operations convert(String source) {
        return Operations.valueOf(source.replaceAll("(.)([A-Z])", "$1_$2").toUpperCase());
    }
}
