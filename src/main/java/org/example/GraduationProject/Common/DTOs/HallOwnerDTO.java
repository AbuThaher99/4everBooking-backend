package org.example.GraduationProject.Common.DTOs;

import lombok.*;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HallOwnerDTO {
    private String firstName;
    private String lastName;
    private String address;
    private String phone;
    private Date dateOfBirth;
    private String companyName;
}
