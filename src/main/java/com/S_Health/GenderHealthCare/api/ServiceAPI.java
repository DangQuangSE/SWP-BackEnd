package com.S_Health.GenderHealthCare.api;

import com.S_Health.GenderHealthCare.dto.ServiceDTO;
import com.S_Health.GenderHealthCare.entity.Service;
import com.S_Health.GenderHealthCare.service.medicalService.ServiceManagementService;
import com.S_Health.GenderHealthCare.dto.request.service.ComboServiceRequest;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/service")
@RestController
//@SecurityRequirement(name = "api")
public class ServiceAPI {

    @Autowired
    ServiceManagementService serviceManagementService;
    @Autowired
    private ModelMapper modelMapper;

    @PostMapping
    public ServiceDTO createService(@RequestBody ServiceDTO serviceDTO) {
        return serviceManagementService.createService(serviceDTO);
    }

    @GetMapping
    public List<ServiceDTO> getAllServices() {
        return serviceManagementService.getAllServices();
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<ServiceDTO> getServiceById(@PathVariable Long id) {
        return serviceManagementService.getServiceById((long) id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());

    }

    @GetMapping("/name/{name}")
    public ResponseEntity<List<ServiceDTO>> getServiceByName(@PathVariable String name) {
        List<ServiceDTO> results = serviceManagementService.getServiceByName(name);
        if (results.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(results);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceDTO> updateService(@PathVariable long id, @RequestBody ServiceDTO serviceDTO) {
        try{
            ServiceDTO updated = serviceManagementService.updateService(id, serviceDTO);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ServiceDTO> deleteService(@PathVariable long id) {
        try {
            serviceManagementService.deleteService(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<ServiceDTO> activateService(@PathVariable Long id) {
        Service service = serviceManagementService.activateService(id);
        ServiceDTO serviceDTO = modelMapper.map(service, ServiceDTO.class);
        return ResponseEntity.ok(serviceDTO);
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ServiceDTO> deactivateService(@PathVariable Long id) {
        Service service = serviceManagementService.deactivateService(id);
        ServiceDTO serviceDTO = modelMapper.map(service, ServiceDTO.class);
        return ResponseEntity.ok(serviceDTO);
    }
    @PostMapping("/comboService")
    public ResponseEntity createComboService(@RequestBody ComboServiceRequest request){
        return ResponseEntity.ok(serviceManagementService.createComboService(request));
    }
}
