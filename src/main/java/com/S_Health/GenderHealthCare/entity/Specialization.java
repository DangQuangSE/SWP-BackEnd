package com.S_Health.GenderHealthCare.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Specialization {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;
    String name;
    @ManyToMany(mappedBy = "specializations")
    @JsonIgnore
    List<User> consultants;
    @OneToMany(mappedBy = "specialization")
    List<HospitalSlotFree> hospitalSlotFrees;
}
