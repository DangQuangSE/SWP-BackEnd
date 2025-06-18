package com.S_Health.GenderHealthCare.service.medicalService;

import com.S_Health.GenderHealthCare.dto.ServiceDTO;
import com.S_Health.GenderHealthCare.dto.request.service.ComboServiceRequest;
import com.S_Health.GenderHealthCare.entity.ComboItem;
import com.S_Health.GenderHealthCare.entity.Service;
import com.S_Health.GenderHealthCare.repository.ComboItemRepository;
import com.S_Health.GenderHealthCare.repository.ServiceRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@org.springframework.stereotype.Service
public class ServiceManagementService {

    @Autowired
    private ServiceRepository serviceRepository;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private ComboItemRepository comboItemRepository;

    public List<ServiceDTO> getAllServices() {
        List<Service> services = serviceRepository.findAll();
        return services.stream()
                .map(service -> modelMapper.map(service, ServiceDTO.class))
                .toList();
    }

    public Optional<ServiceDTO> getServiceById(Long id) {
        Optional<Service> service = serviceRepository.findById(id);
        return service.map(s -> modelMapper.map(s, ServiceDTO.class));
    }

    public List<ServiceDTO> getServiceByName(String name) {
        List<Service> service = serviceRepository.findByNameContainingIgnoreCase(name);
        return service.stream()
                .map(s -> modelMapper.map(s, ServiceDTO.class)).toList();
    }

    public ServiceDTO createService(ServiceDTO serviceDTO) {
        Service service = modelMapper.map(serviceDTO, Service.class);
        Service saved = serviceRepository.save(service);
        return modelMapper.map(saved, ServiceDTO.class);
    }

    public void deleteService(Long id) {
        serviceRepository.deleteById(id);
    }

    public ServiceDTO updateService(Long id, ServiceDTO serviceDTO) {
        return serviceRepository.findById(id).map(existing -> {
            modelMapper.map(serviceDTO, existing); // Copy fields from dto to existing entity
            Service updated = serviceRepository.save(existing);
            return modelMapper.map(updated, ServiceDTO.class);
        }).orElseThrow(() -> new RuntimeException("Không tìm thấy ID này: " + id));

    }

    public Service activateService(Long id) {
        Service service = serviceRepository.getById(id);
        service.setIsActive(true);
        return serviceRepository.save(service);
    }

    public Service deactivateService(Long id) {
        Service service = serviceRepository.getById(id);
        service.setIsActive(false);
        return serviceRepository.save(service);
    }

    public ServiceDTO createComboService(ComboServiceRequest request) {
        Service combo = new Service();
        combo.setName(request.getName());
        combo.setDescription(request.getDescription());
        combo.setCreatedAt(LocalDateTime.now());
        combo.setIsCombo(true);
        combo.setIsActive(true);

        serviceRepository.save(combo);

        double totalPrice = 0;

        for (Long subId : request.getSubServiceIds()) {
            Service sub = serviceRepository.findById(subId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy dịch vụ này!"));
            ComboItem item = new ComboItem();
            item.setComboService(combo);
            item.setSubService(sub);
            comboItemRepository.save(item);
            totalPrice += sub.getPrice();
        }
        combo.setPrice(totalPrice * (1 - (request.getDiscountPercent() / 100.0)));
        serviceRepository.save(combo);
        ServiceDTO serviceDTO = modelMapper.map(combo, ServiceDTO.class);
        return serviceDTO;

    }
}

