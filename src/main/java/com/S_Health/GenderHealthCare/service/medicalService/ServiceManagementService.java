package com.S_Health.GenderHealthCare.service.MedicalService;


import com.S_Health.GenderHealthCare.dto.ServiceDTO;
import com.S_Health.GenderHealthCare.dto.SpecializationDTO;
import com.S_Health.GenderHealthCare.dto.response.ComboResponse;
import com.S_Health.GenderHealthCare.entity.ComboItem;
import com.S_Health.GenderHealthCare.entity.Service;
import com.S_Health.GenderHealthCare.entity.Specialization;
import com.S_Health.GenderHealthCare.exception.exceptions.BadRequestException;
import com.S_Health.GenderHealthCare.repository.ComboItemRepository;
import com.S_Health.GenderHealthCare.repository.ServiceRepository;
import com.S_Health.GenderHealthCare.repository.SpecializationRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
public class ServiceManagementService {

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private SpecializationRepository specializationRepository;

    @Autowired
    private ComboItemRepository comboItemRepository;

    @Autowired
    private ModelMapper modelMapper;

    public List<ServiceDTO> getAllServices() {
        return serviceRepository.findByIsActiveTrue().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ServiceDTO getServiceById(Long id) {
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy dịch vụ với ID: " + id));

        if (!service.getIsActive()) {
            throw new BadRequestException("Dịch vụ không hoạt động");
        }

        return convertToDTO(service);
    }

    public List<ServiceDTO> searchServicesByName(String name) {
        return serviceRepository.findByNameContainingIgnoreCaseAndIsActiveTrue(name).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ServiceDTO> getServicesBySpecialization(Long specializationId) {
        Specialization specialization = specializationRepository.findById(specializationId)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy chuyên môn với ID: " + specializationId));

        if (!specialization.getIsActive()) {
            throw new BadRequestException("Chuyên môn không hoạt động");
        }

        return serviceRepository.findBySpecializationsContainingAndIsActiveTrue(specialization).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ServiceDTO createService(ServiceDTO serviceDTO) {
        // Kiểm tra tên dịch vụ có bị trùng không
        if (serviceRepository.existsByNameAndIsActiveTrue(serviceDTO.getName().trim())) {
            throw new BadRequestException("Tên dịch vụ đã tồn tại");
        }

        // Tạo dịch vụ
        Service service = new Service();
        service.setName(serviceDTO.getName().trim());
        service.setDescription(serviceDTO.getDescription());
        service.setDuration(serviceDTO.getDuration());
        service.setType(serviceDTO.getType());
        service.setPrice(serviceDTO.getPrice());
        service.setDiscountPercent(serviceDTO.getDiscountPercent() != null ? serviceDTO.getDiscountPercent() : 0.0);
        service.setIsCombo(serviceDTO.getIsCombo() != null ? serviceDTO.getIsCombo() : false);
        service.setIsActive(true);

        // Thêm các chuyên môn vào dịch vụ
        if (serviceDTO.getSpecializationIds() != null && !serviceDTO.getSpecializationIds().isEmpty()) {
            List<Specialization> specializations = new ArrayList<>();

            for (Long specializationId : serviceDTO.getSpecializationIds()) {
                Specialization specialization = specializationRepository.findById(specializationId)
                        .orElseThrow(() -> new BadRequestException("Không tìm thấy chuyên môn với ID: " + specializationId));

                if (!specialization.getIsActive()) {
                    throw new BadRequestException("Chuyên môn với ID: " + specializationId + " không hoạt động");
                }

                specializations.add(specialization);
            }

            service.setSpecializations(specializations);
        }

        Service savedService = serviceRepository.save(service);
        return convertToDTO(savedService);
    }

    @Transactional
    public ServiceDTO updateService(Long id, ServiceDTO serviceDTO) {
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy dịch vụ với ID: " + id));

        if (!service.getIsActive()) {
            throw new BadRequestException("Dịch vụ không hoạt động, không thể cập nhật");
        }

        // Kiểm tra tên dịch vụ có bị trùng không (nếu tên thay đổi)
        if (serviceDTO.getName() != null && !service.getName().equals(serviceDTO.getName().trim()) &&
                serviceRepository.existsByNameAndIsActiveTrue(serviceDTO.getName().trim())) {
            throw new BadRequestException("Tên dịch vụ đã tồn tại");
        }

        // Cập nhật thông tin cơ bản
        if (serviceDTO.getName() != null) {
            service.setName(serviceDTO.getName().trim());
        }

        if (serviceDTO.getDescription() != null) {
            service.setDescription(serviceDTO.getDescription());
        }

        if (serviceDTO.getDuration() != null) {
            service.setDuration(serviceDTO.getDuration());
        }

        if (serviceDTO.getType() != null) {
            service.setType(serviceDTO.getType());
        }

        if (serviceDTO.getPrice() != null) {
            service.setPrice(serviceDTO.getPrice());
        }

        if (serviceDTO.getDiscountPercent() != null) {
            service.setDiscountPercent(serviceDTO.getDiscountPercent());
        }

        // Cập nhật danh sách chuyên môn
        if (serviceDTO.getSpecializationIds() != null && !serviceDTO.getSpecializationIds().isEmpty()) {
            List<Specialization> specializations = new ArrayList<>();

            for (Long specializationId : serviceDTO.getSpecializationIds()) {
                Specialization specialization = specializationRepository.findById(specializationId)
                        .orElseThrow(() -> new BadRequestException("Không tìm thấy chuyên môn với ID: " + specializationId));

                if (!specialization.getIsActive()) {
                    throw new BadRequestException("Chuyên môn với ID: " + specializationId + " không hoạt động");
                }

                specializations.add(specialization);
            }

            service.setSpecializations(specializations);
        }

        Service updatedService = serviceRepository.save(service);
        return convertToDTO(updatedService);
    }

    @Transactional
    public ServiceDTO activateService(Long id) {
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy dịch vụ với ID: " + id));

        service.setIsActive(true);
        Service updatedService = serviceRepository.save(service);
        return convertToDTO(updatedService);
    }

    @Transactional
    public ServiceDTO deactivateService(Long id) {
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy dịch vụ với ID: " + id));

        service.setIsActive(false);
        Service updatedService = serviceRepository.save(service);
        return convertToDTO(updatedService);
    }

    @Transactional
    public ComboResponse createComboService(ServiceDTO serviceDTO) {
        // Kiểm tra tên dịch vụ combo có bị trùng không
        if (serviceRepository.existsByNameAndIsActiveTrue(serviceDTO.getName().trim())) {
            throw new BadRequestException("Tên dịch vụ combo đã tồn tại");
        }

        // Kiểm tra danh sách dịch vụ thành phần
        if (serviceDTO.getSubServiceIds() == null || serviceDTO.getSubServiceIds().isEmpty()) {
            throw new BadRequestException("Dịch vụ combo phải có ít nhất 1 dịch vụ thành phần");
        }

        // Tạo dịch vụ combo
        Service comboService = new Service();
        comboService.setName(serviceDTO.getName().trim());
        comboService.setDescription(serviceDTO.getDescription());
        comboService.setDuration(serviceDTO.getDuration());
        comboService.setType(serviceDTO.getType());
        comboService.setDiscountPercent(serviceDTO.getDiscountPercent() != null ? serviceDTO.getDiscountPercent() : 0.0);
        comboService.setIsCombo(true);
        comboService.setIsActive(true);

        // Thêm các chuyên môn vào dịch vụ combo
        if (serviceDTO.getSpecializationIds() != null && !serviceDTO.getSpecializationIds().isEmpty()) {
            List<Specialization> specializations = new ArrayList<>();

            for (Long specializationId : serviceDTO.getSpecializationIds()) {
                Specialization specialization = specializationRepository.findById(specializationId)
                        .orElseThrow(() -> new BadRequestException("Không tìm thấy chuyên môn với ID: " + specializationId));

                if (!specialization.getIsActive()) {
                    throw new BadRequestException("Chuyên môn với ID: " + specializationId + " không hoạt động");
                }

                specializations.add(specialization);
            }

            comboService.setSpecializations(specializations);
        }

        // Lưu dịch vụ combo trước để có ID
        Service savedComboService = serviceRepository.save(comboService);

        // Tính tổng giá và tạo các ComboItem
        List<ComboItem> comboItems = new ArrayList<>();
        List<ServiceDTO> subServiceDTOs = new ArrayList<>();
        double totalPrice = 0.0;

        for (Long subServiceId : serviceDTO.getSubServiceIds()) {
            Service subService = serviceRepository.findById(subServiceId)
                    .orElseThrow(() -> new BadRequestException("Không tìm thấy dịch vụ thành phần với ID: " + subServiceId));

            if (!subService.getIsActive()) {
                throw new BadRequestException("Dịch vụ thành phần với ID: " + subServiceId + " không hoạt động");
            }

            ComboItem comboItem = new ComboItem();
            comboItem.setComboService(savedComboService);
            comboItem.setSubService(subService);
            comboItem.setName(subService.getName());
            comboItems.add(comboItem);

            totalPrice += subService.getPrice();
            subServiceDTOs.add(convertToDTO(subService));
        }

        // Lưu các ComboItem
        comboItemRepository.saveAll(comboItems);

        // Tính giá cuối cùng sau khi áp dụng giảm giá
        double finalPrice = totalPrice * (1 - (savedComboService.getDiscountPercent() / 100));
        savedComboService.setPrice(finalPrice);
        savedComboService = serviceRepository.save(savedComboService);

        return new ComboResponse(convertToDTO(savedComboService), subServiceDTOs);
    }

    private ServiceDTO convertToDTO(Service service) {
        ServiceDTO dto = modelMapper.map(service, ServiceDTO.class);

        // Map danh sách chuyên môn
        if (service.getSpecializations() != null && !service.getSpecializations().isEmpty()) {
            List<SpecializationDTO> specializationDTOs = service.getSpecializations().stream()
                    .map(specialization -> modelMapper.map(specialization, SpecializationDTO.class))
                    .collect(Collectors.toList());

            dto.setSpecializations(specializationDTOs);

            List<Long> specializationIds = service.getSpecializations().stream()
                    .map(Specialization::getId)
                    .collect(Collectors.toList());

            dto.setSpecializationIds(specializationIds);
        }

        // Nếu là service combo, thêm danh sách subServiceIds
        if (Boolean.TRUE.equals(service.getIsCombo()) && service.getComboItems() != null && !service.getComboItems().isEmpty()) {
            List<Long> subServiceIds = service.getComboItems().stream()
                    .map(comboItem -> comboItem.getSubService().getId())
                    .collect(Collectors.toList());

            dto.setSubServiceIds(subServiceIds);
        }

        return dto;
    }
}
