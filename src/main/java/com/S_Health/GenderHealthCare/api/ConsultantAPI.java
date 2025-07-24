package com.S_Health.GenderHealthCare.api;

import com.S_Health.GenderHealthCare.dto.response.ConsultantDTO;
import com.S_Health.GenderHealthCare.service.UserService;
import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/consultant")
@RequiredArgsConstructor
public class ConsultantAPI {

    @Autowired
    private UserService consultantService;

    @GetMapping
    public ResponseEntity<ConsultantDTO> getConsultant(@PathParam("consultantId") Long consultantId) {
        return ResponseEntity.ok(consultantService.getConsultantProfile(consultantId));
    }
}
