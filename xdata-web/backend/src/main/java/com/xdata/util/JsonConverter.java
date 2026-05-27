package com.xdata.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

@Converter
@Slf4j
public class JsonConverter implements AttributeConverter<Object, String> {

    private final static ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Object attribute) {
        if (attribute == null) return null;
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            log.error("Error serializing object to JSON", e);
            return null;
        }
    }

    @Override
    public Object convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) return null;
        try {
            // Hinweis: Da wir den Zieltyp hier nicht kennen, ist dieser generische Converter 
            // eingeschränkt. Für PartialMarkParameters sollten wir einen spezifischen Converter nutzen.
            return objectMapper.readValue(dbData, Object.class);
        } catch (JsonProcessingException e) {
            log.error("Error deserializing JSON to object", e);
            return null;
        }
    }
}
