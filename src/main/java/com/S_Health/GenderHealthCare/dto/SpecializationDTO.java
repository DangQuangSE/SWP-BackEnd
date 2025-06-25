package com.S_Health.GenderHealthCare.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SpecializationDTO {
     long id;
     String name;
     Long serviceId;
     String serviceName;
     int consultantCount;
}
