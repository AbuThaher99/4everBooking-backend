package org.example.GraduationProject.Common.DTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RatingDTO {
    private Long userId;
    private Long hallId;
    private double rating;
    private String comment;
}
