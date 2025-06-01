package com.S_Health.GenderHealthCare.entity;

import com.S_Health.GenderHealthCare.enums.Gender;
import com.S_Health.GenderHealthCare.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public String fullname;
    public String email;
    public String phone;
    public LocalDate dateOfBirth;
    public String password;
    public String imageUrl;
    public boolean isVerify;
    public boolean isActice;
    @CreationTimestamp
    public LocalDate createdAt;
    @UpdateTimestamp
    public LocalDate updatedAt;
    @Enumerated(EnumType.STRING)
    public Gender gender;
    @Enumerated(EnumType.STRING)
    public UserRole role;

//test
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getUsername() {
        return this.phone;
    }

    @Override
    public String getPassword() {
        return this.password;
    }
}
