package com.S_Health.GenderHealthCare.service.MedicalService;

import com.S_Health.GenderHealthCare.dto.ServiceDTO;
import com.S_Health.GenderHealthCare.dto.request.service.ServiceRequest;
import com.S_Health.GenderHealthCare.dto.response.ComboResponse;
import com.S_Health.GenderHealthCare.entity.ComboItem;
import com.S_Health.GenderHealthCare.entity.Service;
import com.S_Health.GenderHealthCare.exception.exceptions.BadRequestException;
import com.S_Health.GenderHealthCare.repository.ComboItemRepository;
import com.S_Health.GenderHealthCare.repository.ServiceRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

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

    public ComboResponse createComboService(ServiceRequest request) {
        Service combo = new Service();
        combo.setName(request.getName());
        combo.setDescription(request.getDescription());
        combo.setDiscountPercent(request.getDiscountPercent());
        combo.setDuration(request.getDuration());
        combo.setType(request.getType());
        combo.setIsCombo(true);
        combo.setIsActive(true);
        serviceRepository.save(combo);
        List<Long> subServices = request.getSubServiceIds();
        List<ComboItem> comboItems = new ArrayList<>();
        List<ServiceDTO> serviceDTOS = new ArrayList<>();
        Double totalPrice = 0D;
        for (Long id : subServices) {
            //lấy ra thông tin subservice và lưu vào comboItem
            Service sub = serviceRepository.findById(id)
                    .orElseThrow(() -> new BadRequestException("Không tìm thấy dịch vụ " + id));
            ComboItem comboItem = ComboItem.builder()
                    .comboService(combo)
                    .subService(sub)
                    .name(sub.getName())
                    .build();
            comboItems.add(comboItem);
            ServiceDTO svDto = modelMapper.map(sub, ServiceDTO.class);
            serviceDTOS.add(svDto);
            totalPrice += sub.getPrice();
        }
        comboItemRepository.saveAll(comboItems);
        combo.setPrice(totalPrice * (1 - (combo.getDiscountPercent() / 100)));
        serviceRepository.save(combo);
        ServiceDTO serviceDTO = modelMapper.map(combo, ServiceDTO.class);
        return new ComboResponse(serviceDTO, serviceDTOS);
    }
}

