package org.example.GraduationProject.Common.DTOs;

import jakarta.persistence.Convert;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.GraduationProject.Common.Converters.MapToJsonConverter;
import org.example.GraduationProject.Common.Enums.HallCategory;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReservationDTO {
    private Long hallId;
    private Long customerId;
    private LocalDateTime time;
    @Convert(converter = MapToJsonConverter.class)
    private Map<String ,Object> services;
    private LocalDateTime endTime;
    private HallCategory selectedCategory;
}
