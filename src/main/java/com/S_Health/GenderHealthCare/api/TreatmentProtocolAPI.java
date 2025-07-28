package com.S_Health.GenderHealthCare.api;

import com.S_Health.GenderHealthCare.dto.TagDTO;
import com.S_Health.GenderHealthCare.dto.request.TreatmentProtocolRequest;
import com.S_Health.GenderHealthCare.dto.request.tag.TagRequest;
import com.S_Health.GenderHealthCare.dto.response.TreatmentProtocolResponse;
import com.S_Health.GenderHealthCare.entity.TreatmentProtocol;
import com.S_Health.GenderHealthCare.service.MedicalService.TreatmentProtocolService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/treatment")
@SecurityRequirement(name = "api")
public class TreatmentProtocolAPI {
    @Autowired
    TreatmentProtocolService treatmentProtocolService;

    @PostMapping
    public ResponseEntity<TreatmentProtocolResponse> createTag(@RequestBody TreatmentProtocolRequest request) {
        return ResponseEntity.ok(treatmentProtocolService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<TreatmentProtocolResponse>> getAllTreatmentProtocol() {
        return ResponseEntity.ok(treatmentProtocolService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TreatmentProtocolResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(treatmentProtocolService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TreatmentProtocolResponse> update(@PathVariable Long id,
                                                            @RequestBody TreatmentProtocolRequest request)  {
        TreatmentProtocolResponse response = treatmentProtocolService.update(id, request);
        return ResponseEntity.ok(response);
    }
}
