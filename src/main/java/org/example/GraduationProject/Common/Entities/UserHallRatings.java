package org.example.GraduationProject.Common.Entities;


import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "userHallRatings")
public class UserHallRatings extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "userId", nullable = false)
    @NotNull(message = "User is required")
    @JsonBackReference("UserRatings")
    private User user;

    @ManyToOne
    @JoinColumn(name = "hallId", nullable = false)
    @NotNull(message = "Hall is required")
    @JsonBackReference("HallRatings")
    private Hall hall;

    @Column(name = "rating", nullable = false)
    private double rating;

    @Column(name = "comment", nullable = false, columnDefinition = "TEXT")
    private String comment;

}
