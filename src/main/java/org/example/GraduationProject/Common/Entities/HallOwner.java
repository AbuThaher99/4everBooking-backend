package org.example.GraduationProject.Common.Entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "hallOwners")
public class HallOwner extends BaseEntity{


    @Column(name = "companyName" , nullable = false)
    @NotNull(message = "Company name cannot be Null")
    @NotBlank(message = "Company name cannot be blank")
    private String companyName;


    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "hallOwnerId", referencedColumnName = "id")
    @JsonManagedReference("hallOwnerHall")
    private List<Hall> halls;

    @OneToOne
    @JoinColumn(name = "userId")
    @JsonBackReference("hallOwnerUser")
    private User user;

}
