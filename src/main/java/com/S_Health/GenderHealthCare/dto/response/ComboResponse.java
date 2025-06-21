package com.S_Health.GenderHealthCare.dto.response;

import com.S_Health.GenderHealthCare.dto.ServiceDTO;
import com.S_Health.GenderHealthCare.entity.ComboItem;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ComboResponse {
    //combo
    ServiceDTO serviceDTO;
    //các service có trong combo
    List<ServiceDTO> subServices;

}
