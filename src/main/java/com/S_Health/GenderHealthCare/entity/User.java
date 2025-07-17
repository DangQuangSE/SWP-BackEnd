package com.S_Health.GenderHealthCare.entity;

import com.S_Health.GenderHealthCare.enums.Gender;
import com.S_Health.GenderHealthCare.enums.UserRole;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
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
@FieldDefaults(level = AccessLevel.PRIVATE)

public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String fullname;
    String email;
    String phone;
    LocalDate dateOfBirth;
    String password;
    String imageUrl;
    String address;
    boolean isVerify;
    boolean isActive;
    @CreationTimestamp
    LocalDate createdAt;
    @UpdateTimestamp
    LocalDate updatedAt;
    @Enumerated(EnumType.STRING)
    Gender gender;
    @Enumerated(EnumType.STRING)
    UserRole role;

    @OneToMany(mappedBy = "consultant")
    @JsonIgnore
    List<Schedule> schedules;

    @OneToMany(mappedBy = "consultant")
    @JsonIgnore
    List<Certification> certifications;

    // Quan hệ nhiều-nhiều với Specialization (cho consultant)
    @ManyToMany
    @JoinTable(
            name = "user_specialization",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "specialization_id")
    )
    List<Specialization> specializations;

    @OneToMany(mappedBy = "customer")
    @JsonIgnore
    List<MedicalProfile> medicalProfiles;

    @OneToMany(mappedBy = "customer")
    @JsonIgnore
    List<Appointment> appointments;
    @OneToMany(mappedBy = "consultant")
    @JsonIgnore
    List<AppointmentDetail> appointmentDetails;
    @OneToMany(mappedBy = "consultant")
    @JsonIgnore
    List<ConsultantSlot> consultantSlots;
    @OneToMany(mappedBy = "user")
    @JsonIgnore
    List<CycleTracking> cycleTrackings;

    @OneToMany(mappedBy = "user")
    @JsonIgnore
    List<Notification> notifications;

    @OneToMany(mappedBy = "author")
    @JsonIgnore
    List<Blog> blogs;

    @OneToMany(mappedBy = "commenter")
    @JsonIgnore
    List<Comment> comments;
    @OneToMany(mappedBy = "paidBy")
    @JsonIgnore
    List<Payment> payments;
    @OneToMany(mappedBy = "consultant")
    @JsonIgnore
    List<MedicalResult> medicalResults;

    @OneToMany(mappedBy = "consultant")
    @JsonIgnore
    List<RoomConsultant> roomAssignments;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    public long getId() {
        return this.id;
    }

}
