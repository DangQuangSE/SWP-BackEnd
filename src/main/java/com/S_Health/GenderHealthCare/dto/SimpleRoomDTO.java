package com.S_Health.GenderHealthCare.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SimpleRoomDTO {
    long id;
    String name;
    String description;
    String specializationName;
}
