package org.example.GraduationProject.Common.DTOs;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.GraduationProject.Common.Converters.MapToJsonConverter;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HallDTO {
    private Long id;

    private String name;

    private String location;

    private int capacity;

    private double price;

    private String description;

    private String phone;

    @Convert(converter = MapToJsonConverter.class)
    private Map<String, Object> services;

    private double longitude;

    private double latitude;


}
