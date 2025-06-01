package com.S_Health.GenderHealthCare.entity;

import com.S_Health.GenderHealthCare.enums.Gender;
import com.S_Health.GenderHealthCare.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class User {
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
    public boolean isActive;
    @CreationTimestamp
    public LocalDate createdAt;
    @UpdateTimestamp
    public LocalDate updatedAt;
    @Enumerated(EnumType.STRING)
    public Gender gender;
    @Enumerated(EnumType.STRING)
    public UserRole role;
}
