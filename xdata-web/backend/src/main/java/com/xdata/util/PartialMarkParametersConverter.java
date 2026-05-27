package com.xdata.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xdata.partialmarking.core.PartialMarkParameters;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

@Converter
@Slf4j
public class PartialMarkParametersConverter implements AttributeConverter<PartialMarkParameters, String> {

    private final static ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(PartialMarkParameters attribute) {
        if (attribute == null) return null;
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            log.error("Error serializing PartialMarkParameters to JSON", e);
            return null;
        }
    }

    @Override
    public PartialMarkParameters convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) return new PartialMarkParameters();
        try {
            return objectMapper.readValue(dbData, PartialMarkParameters.class);
        } catch (Exception e) {
            log.warn("Error deserializing JSON to PartialMarkParameters: {}. Using defaults.", dbData);
            return new PartialMarkParameters();
        }
    }
}
