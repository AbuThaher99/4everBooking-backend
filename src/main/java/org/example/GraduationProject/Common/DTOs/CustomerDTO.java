package org.example.GraduationProject.Common.DTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerDTO {
    private String firstName;
    private String lastName;
    private String address;
    private String phone;
    private Date dateOfBirth;
}
