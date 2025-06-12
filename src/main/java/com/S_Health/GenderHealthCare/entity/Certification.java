package com.S_Health.GenderHealthCare.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Certification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;
    String name;
    String image;
    @ManyToOne
    @JoinColumn(name = "consultant_id")
    User consultant;
}
