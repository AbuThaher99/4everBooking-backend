package org.example.GraduationProject.Common.Entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.GraduationProject.Common.Enums.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User extends BaseEntity implements UserDetails  {

    @Column(name = "image")
    private String image;

    @Column(name = "password", nullable = false)
    @NotNull(message = "Password cannot be blank")
    private String password;

    @Column(name = "firstName", nullable = false)
    @NotNull(message = "First name cannot be blank")
    private String firstName;

    @Column(name = "lastName", nullable = false)
    @NotNull(message = "Last name cannot be blank")
    private String lastName;

    @Column(name = "address" , nullable = false)
    @NotNull(message = "Address cannot be blank")
    private String address;

    @Column(name = "phone" , nullable = false)
    @NotNull(message = "Phone cannot be blank")
    private String phone;

    @Column(name = "dateOfBirth" , nullable = false)
    @NotNull(message = "Date of birth cannot be blank")
    private Date dateOfBirth;

    @Column(name = "email", unique = true, nullable = false)
    @NotNull(message = "Email cannot be blank")
    private String email;

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "isDeleted")
    @Builder.Default
    @JsonIgnore
    private boolean isDeleted = false;

    @OneToMany(mappedBy = "user")
    @JsonIgnore
    private List<Token> tokens;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL , fetch = FetchType.LAZY)
    @JsonBackReference("hallOwnerUser")
    private HallOwner hallOwner;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL , fetch = FetchType.LAZY)
    @JsonBackReference("customerUser")
    private Customer customer;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY , orphanRemoval = true)
    @JoinColumn(name = "userId", referencedColumnName = "id")
    @JsonManagedReference("UserRatings")
    private List<UserHallRatings> userHallRatings;

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    @JsonIgnore
    public String getUsername() {
        return email; // Use email as the username
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(getId(), user.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

}
