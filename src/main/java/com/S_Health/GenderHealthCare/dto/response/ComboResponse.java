package com.S_Health.GenderHealthCare.dto.response;


import com.S_Health.GenderHealthCare.dto.ServiceDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComboResponse {
    private ServiceDTO comboService;
    private List<ServiceDTO> subServices;
}
