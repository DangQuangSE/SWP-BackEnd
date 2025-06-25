package com.S_Health.GenderHealthCare.service;

import com.S_Health.GenderHealthCare.dto.SpecializationDTO;
import com.S_Health.GenderHealthCare.dto.request.SpecializationRequest;
import com.S_Health.GenderHealthCare.entity.Service;
import com.S_Health.GenderHealthCare.entity.Specialization;
import com.S_Health.GenderHealthCare.exception.exceptions.BadRequestException;
import com.S_Health.GenderHealthCare.repository.ServiceRepository;
import com.S_Health.GenderHealthCare.repository.SpecializationRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service

public class SpecializationService {
    @Autowired
    SpecializationRepository specializationRepository;
    @Autowired
    ServiceRepository serviceRepository;
    @Autowired
    ModelMapper modelMapper;

    public List<SpecializationDTO> getAllSpecializations() {
        return specializationRepository.findAll().stream()
                .map(entity -> modelMapper.map(entity, SpecializationDTO.class))
                .collect(Collectors.toList());
    }

    public SpecializationDTO getSpecializationById(Long id) {
        return specializationRepository.findById(id)
                .map(entity -> modelMapper.map(entity, SpecializationDTO.class))
                .orElseThrow(() -> new BadRequestException("Không tìm thấy chuyên môn với ID: " + id));
    }

    public List<SpecializationDTO> getSpecializationsByServiceId(Long serviceId) {
        return specializationRepository.findByServiceId(serviceId).stream()
                .map(entity -> modelMapper.map(entity, SpecializationDTO.class))
                .collect(Collectors.toList());
    }

    @Transactional
    public SpecializationDTO createSpecialization(SpecializationRequest request) {
        Service service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new BadRequestException("Không tìm thấy dịch vụ với ID: " + request.getServiceId()));

        Specialization specialization = new Specialization();
        specialization.setName(request.getName().trim());
        specialization.setService(service);

        return modelMapper.map(specializationRepository.save(specialization), SpecializationDTO.class);
    }

    @Transactional
    public SpecializationDTO updateSpecialization(Long id, SpecializationRequest request) {
        Specialization specialization = specializationRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy chuyên môn với ID: " + id));

        Service service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new BadRequestException("Không tìm thấy dịch vụ với ID: " + request.getServiceId()));

        specialization.setName(request.getName().trim());
        specialization.setService(service);

        return modelMapper.map(specializationRepository.save(specialization), SpecializationDTO.class);
    }
    @Transactional
    public void deleteSpecialization(Long id) {
        Specialization specialization = specializationRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy chuyên môn với ID: " + id));

        if (specialization.getConsultants() != null && !specialization.getConsultants().isEmpty()) {
            throw new BadRequestException("Không thể xóa chuyên môn đang được sử dụng bởi các consultant");
        }
        specialization.setActive(false);
        specializationRepository.save(specialization);
    }
}
