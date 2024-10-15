package org.example.GraduationProject.Common.Entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "emails")
public class Email extends BaseEntity {

    @Column(name = "email", nullable = false)
    private String email;


    @Column(name = "verified", nullable = false)
    @Builder.Default
    private boolean verified = false;


    @Column(name = "verificationCode", nullable = false)
    private String verificationCode;
}
