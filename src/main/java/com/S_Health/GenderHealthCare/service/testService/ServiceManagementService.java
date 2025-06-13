package com.S_Health.GenderHealthCare.service.testService;

import com.S_Health.GenderHealthCare.dto.ServiceDTO;
import com.S_Health.GenderHealthCare.entity.MedicalService;
import com.S_Health.GenderHealthCare.repository.ServiceRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

@org.springframework.stereotype.Service
public class ServiceManagementService {

    @Autowired
    private ServiceRepository serviceRepository;
    @Autowired
    private ModelMapper modelMapper;

    public List<ServiceDTO> getAllServices() {
        List<MedicalService> services = serviceRepository.findAll();
        return services.stream()
                .map(service -> modelMapper.map(service, ServiceDTO.class))
                .toList();
    }

    public Optional<ServiceDTO> getServiceById(Long id) {
        Optional<MedicalService> service = serviceRepository.findById(id);
        return service.map(s -> modelMapper.map(s, ServiceDTO.class));
    }

    public List<ServiceDTO> getServiceByName(String name) {
        List<MedicalService> service = serviceRepository.findByNameContainingIgnoreCase(name);
        return service.stream()
                .map(s -> modelMapper.map(s, ServiceDTO.class)).toList();
    }

    public ServiceDTO createService(ServiceDTO serviceDTO) {
        MedicalService service = modelMapper.map(serviceDTO, MedicalService.class);
        MedicalService saved = serviceRepository.save(service);
        return modelMapper.map(saved, ServiceDTO.class);
    }

    public void deleteService(Long id) {
        serviceRepository.deleteById(id);
    }

    public ServiceDTO updateService(Long id, ServiceDTO serviceDTO) {
        return serviceRepository.findById(id).map(existing -> {
            modelMapper.map(serviceDTO, existing); // Copy fields from dto to existing entity
            MedicalService updated = serviceRepository.save(existing);
            return modelMapper.map(updated, ServiceDTO.class);
        }).orElseThrow(() -> new RuntimeException("Không tìm thấy ID này: " + id));

    }

    public MedicalService activateService(Long id) {
        MedicalService service = serviceRepository.getById(id);
        service.setIsActive(true);
        return serviceRepository.save(service);
    }

    public MedicalService deactivateService(Long id) {
        MedicalService service = serviceRepository.getById(id);
        service.setIsActive(false);
        return serviceRepository.save(service);
    }
}
