package com.S_Health.GenderHealthCare.dto.response.consultant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultantDTO {

    long id;
    String fullname;
    String phone;
    String email;
    MultipartFile img;
    String imageUrl;
    LocalDate dateOfBirth;
    String address;
    List<String> specializationNames;
    List<ConsultantCertification> certification;
    String gender;
    double rating;


}
