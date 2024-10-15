package org.example.GraduationProject.Common.Converters;



import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.example.GraduationProject.Common.Enums.HallCategory;

import java.io.IOException;
import java.util.List;

@Converter
public class ListToStringConverter implements AttributeConverter<List<HallCategory>, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<HallCategory> attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (IOException e) {
            throw new IllegalArgumentException("Error converting list to JSON string", e);
        }
    }

    @Override
    public List<HallCategory> convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        try {
            return objectMapper.readValue(dbData, new TypeReference<List<HallCategory>>() {});
        } catch (IOException e) {
            throw new IllegalArgumentException("Error converting JSON string to list", e);
        }
    }
}

