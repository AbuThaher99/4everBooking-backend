package org.example.GraduationProject.Common.Entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.GraduationProject.Common.Converters.HallCategoryMapConverter;
import org.example.GraduationProject.Common.Converters.ListToStringConverter;
import org.example.GraduationProject.Common.Converters.MapToJsonConverter;
import org.example.GraduationProject.Common.Enums.HallCategory;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "halls")
public class Hall extends BaseEntity{

    @Column(name = "name" , nullable = false)
    @NotNull(message = "Name cannot be Null")
    private String name;

    @Column(name = "image" , nullable = false ,length = 1000)
    @NotNull(message = "Image cannot be Null")
    private String image;

    @Column(name = "location" , nullable = false)
    @NotNull(message = "Location cannot be Null")
    private String location;

    @Column(name = "capacity" , nullable = false)
    @NotNull(message = "Capacity cannot be Null")
    private int capacity;


    @Column(name = "description" , nullable = false,columnDefinition = "TEXT")
    @NotNull(message = "Description cannot be Null")
    private String description;

    @Column(name = "isReserved" , nullable = false)
    @Builder.Default
    private boolean isReserved = false;

    @Column(name = "phone" , nullable = false)
    @NotNull(message = "Phone cannot be Null")
    private String phone;

    @Column(name = "services" , nullable = false)
    @Convert(converter = MapToJsonConverter.class)
    @NotNull(message = "Services cannot be Null")
    private Map<String, Object> services;

    @Column(name = "longitude", nullable = false)
    @NotNull(message = "Longitude cannot be Null")
    private double longitude;

    @Column(name = "latitude", nullable = false)
    @NotNull(message = "Latitude cannot be Null")
    private double latitude;

    @ManyToOne
    @JoinColumn(name = "hallOwnerId")
    @NotNull(message = "Hall Owner is required")
    @JsonBackReference("hallOwnerHall")
    private HallOwner hallOwner;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY , orphanRemoval = true)
    @JoinColumn(name = "hallId", referencedColumnName = "id")
    @JsonManagedReference("hallReservations")
    private List<Reservations> reservations;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY , orphanRemoval = true)
    @JoinColumn(name = "hallId", referencedColumnName = "id")
    @JsonManagedReference("HallRatings")
    private List<UserHallRatings> HallRatings;

    @Column(name = "isDeleted", nullable = false)
    @Builder.Default
    private boolean isDeleted = false;

    @Column(name = "categories", nullable = false)
    @Convert(converter = HallCategoryMapConverter.class)
    @NotNull(message = "At least one category is required")
    private Map<HallCategory, Double> categories;

    @Column(name = "averageRating", nullable = false)
    @Builder.Default
    private double averageRating = 0.0;

    @Column(name = "isProcessed", nullable = false)
    @Builder.Default
    private boolean isProcessed = false;

    @Column(name = "proofFile" , nullable = false ,length = 1000)
    @NotNull(message = "Proof file cannot be Null")
    private String proofFile;

}
