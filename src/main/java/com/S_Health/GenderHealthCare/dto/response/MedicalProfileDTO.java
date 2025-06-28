package com.S_Health.GenderHealthCare.dto.response;

import com.S_Health.GenderHealthCare.entity.Appointment;
import com.S_Health.GenderHealthCare.entity.User;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MedicalProfileDTO {
    long id;
    long customer_id;
    long service_id;
    List<Appointment> appointments;
    //String note;
}
