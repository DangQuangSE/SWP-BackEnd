package com.S_Health.GenderHealthCare.service;

import com.S_Health.GenderHealthCare.dto.SpecializationDTO;
import com.S_Health.GenderHealthCare.dto.request.SpecializationRequest;
import com.S_Health.GenderHealthCare.entity.Specialization;
import com.S_Health.GenderHealthCare.exception.exceptions.AppException;
import com.S_Health.GenderHealthCare.repository.SpecializationRepository;
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
    ModelMapper modelMapper;

    public List<SpecializationDTO> getAllSpecializations() {
        return specializationRepository.findByIsActiveTrue().stream()
                .map(specialization -> modelMapper.map(specialization, SpecializationDTO.class))
                .collect(Collectors.toList());
    }

    public SpecializationDTO getSpecializationById(Long id) {
        Specialization specialization = specializationRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy chuyên môn với ID: " + id));

        if (!specialization.getIsActive()) {
            throw new AppException("Chuyên môn không hoạt động");
        }

        return modelMapper.map(specialization, SpecializationDTO.class);
    }

    public List<SpecializationDTO> searchSpecializationsByName(String name) {
        return specializationRepository.findByNameContainingIgnoreCaseAndIsActiveTrue(name).stream()
                .map(specialization -> modelMapper.map(specialization, SpecializationDTO.class))
                .collect(Collectors.toList());
    }

    @Transactional
    public SpecializationDTO createSpecialization(SpecializationRequest request) {
        // Kiểm tra tên chuyên môn có bị trùng không
        if (specializationRepository.existsByNameAndIsActiveTrue(request.getName().trim())) {
            throw new AppException("Tên chuyên môn đã tồn tại");
        }

        Specialization specialization = new Specialization();
        specialization.setName(request.getName().trim());
        specialization.setDescription(request.getDescription());
        specialization.setIsActive(true);

        Specialization savedSpecialization = specializationRepository.save(specialization);
        return modelMapper.map(savedSpecialization, SpecializationDTO.class);
    }

    @Transactional
    public SpecializationDTO updateSpecialization(Long id, SpecializationRequest request) {
        Specialization specialization = specializationRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy chuyên môn với ID: " + id));

        if (!specialization.getIsActive()) {
            throw new AppException("Không thể cập nhật chuyên môn đã bị xóa");
        }

        // Kiểm tra xem tên chuyên môn mới có trùng với chuyên môn khác không
        if (!specialization.getName().equalsIgnoreCase(request.getName().trim()) &&
                specializationRepository.existsByNameAndIsActiveTrue(request.getName().trim())) {
            throw new AppException("Tên chuyên môn đã tồn tại");
        }

        specialization.setName(request.getName().trim());
        specialization.setDescription(request.getDescription());

        Specialization updatedSpecialization = specializationRepository.save(specialization);
        return modelMapper.map(updatedSpecialization, SpecializationDTO.class);
    }

    @Transactional
    public void deleteSpecialization(Long id) {
        Specialization specialization = specializationRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy chuyên môn với ID: " + id));

        if (!specialization.getIsActive()) {
            throw new AppException("Chuyên môn đã bị xóa trước đó");
        }

        // Kiểm tra xem chuyên môn có được sử dụng không
        if (!specialization.getServices().isEmpty()) {
            throw new AppException("Không thể xóa chuyên môn đang được sử dụng bởi các dịch vụ");
        }

        if (!specialization.getConsultants().isEmpty()) {
            throw new AppException("Không thể xóa chuyên môn đang được sử dụng bởi các bác sĩ");
        }
        specialization.setIsActive(false);
        specializationRepository.save(specialization);
    }
}


