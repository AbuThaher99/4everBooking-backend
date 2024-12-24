package org.example.GraduationProject.Common.DTOs;

import jakarta.persistence.Convert;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.GraduationProject.Common.Converters.MapToJsonConverter;

import java.time.LocalDateTime;
import java.util.Map;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetReservationDTO {
    private Long id;
    private Long hallId;
    private LocalDateTime time;
    @Convert(converter = MapToJsonConverter.class)
    private Map<String ,Object> services;
    private LocalDateTime endTime;
    private String Category;
    private Double totalPrice;
    private String hallName;
    private boolean isRated;
}
