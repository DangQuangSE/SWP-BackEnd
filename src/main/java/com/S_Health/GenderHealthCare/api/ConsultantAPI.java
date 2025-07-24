package com.S_Health.GenderHealthCare.api;

import com.S_Health.GenderHealthCare.dto.UserDTO;
import com.S_Health.GenderHealthCare.dto.response.ConsultantDTO;
import com.S_Health.GenderHealthCare.service.authentication.ManageUserService;
import com.S_Health.GenderHealthCare.service.schedule.ServiceSlotPoolService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/consultants")
@SecurityRequirement(name = "api")
@Tag(name = "Consultant API", description = "API để lấy thông tin bác sĩ cho booking")
public class ConsultantAPI {

    @Autowired
    private ManageUserService manageUserService;

    @Autowired
    private ServiceSlotPoolService serviceSlotPoolService;

    @GetMapping("/by-service/{serviceId}")
    @Operation(summary = "Lấy danh sách bác sĩ theo dịch vụ",
               description = "Lấy danh sách bác sĩ có thể thực hiện dịch vụ cụ thể để người dùng chọn")
    public ResponseEntity<List<ConsultantDTO>> getConsultantsByService(@PathVariable Long serviceId) {
        List<ConsultantDTO> consultants = manageUserService.getConsultantsByService(serviceId);
        return ResponseEntity.ok(consultants);
    }

    @GetMapping
    @Operation(summary = "Lấy danh sách tất cả bác sĩ",
               description = "Backup API - lấy tất cả bác sĩ")
    public ResponseEntity<List<UserDTO>> getAllConsultants() {
        List<UserDTO> consultants = manageUserService.getUsersByRole("CONSULTANT");
        return ResponseEntity.ok(consultants);
    }
}
