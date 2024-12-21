package org.example.GraduationProject.Common.Entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.GraduationProject.Common.Converters.MapToJsonConverter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "reservations")
public class Reservations extends BaseEntity{
    @Column(name = "date" , nullable = false)
    @NotNull(message = "Date cannot be Null")
    private LocalDateTime date;

    @Column(name = "endDate")
    private LocalDateTime endDate;

    @Column(name = "isDeleted" , nullable = false)
    @Builder.Default
    private boolean isDeleted = false;

    @Column(name = "totalPrice" , nullable = false)
    private double totalPrice;

    @Column(name = "chosenServices" , nullable = false)
    @NotNull(message = "Chosen services cannot be Null")
    @Convert(converter = MapToJsonConverter.class)
    private Map<String, Object> chosenServices;

    @ManyToOne
    @JoinColumn(name = "customerId" , nullable = false)
    @JsonBackReference("customerReservations")
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "hallId", nullable = false)
    @JsonBackReference("hallReservations")
    private Hall hall;

    @Column(name = "category" , nullable = false)
    @NotNull(message = "Category cannot be Null")
    private String category;

    @Column(name = "invoice" , nullable = false)
    @NotNull(message = "Invoice cannot be Null")
    private String invoice;

    @Column(name = "isNotificationSent", nullable = false)
    @Builder.Default
    private boolean isNotificationSent = false;

    @Column(name = "last_modified", nullable = false)
    @UpdateTimestamp
    private LocalDateTime lastModified;

    // boolean flag to mark if the reservation is rated by the customer
    @Column(name = "isRated", nullable = false)
    @Builder.Default
    private boolean isRated = false;


}
