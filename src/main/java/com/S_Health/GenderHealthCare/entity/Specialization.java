package com.S_Health.GenderHealthCare.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Specialization {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String name;
    String description;
    Boolean isActive = true;

    @CreationTimestamp
    LocalDateTime createdAt;

    @UpdateTimestamp
    LocalDateTime updatedAt;

    // Quan hệ nhiều-nhiều với Service
    @ManyToMany(mappedBy = "specializations")
    @JsonIgnore
    List<Service> services;

    // Quan hệ nhiều-nhiều với User (consultant)
    @ManyToMany(mappedBy = "specializations")
    @JsonIgnore
    List<User> consultants;

    // Quan hệ một-nhiều với Room
    @OneToMany(mappedBy = "specialization")
    @JsonIgnore
    List<Room> rooms;
}
