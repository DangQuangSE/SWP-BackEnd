package com.S_Health.GenderHealthCare.service.medicalProfile;

import com.S_Health.GenderHealthCare.dto.AppointmentDTO;
import com.S_Health.GenderHealthCare.dto.response.MedicalProfileDTO;
import com.S_Health.GenderHealthCare.entity.Appointment;
import com.S_Health.GenderHealthCare.entity.MedicalProfile;
import com.S_Health.GenderHealthCare.entity.User;
import com.S_Health.GenderHealthCare.enums.AppointmentStatus;
import com.S_Health.GenderHealthCare.exception.exceptions.BadRequestException;
import com.S_Health.GenderHealthCare.repository.AppointmentRepository;
import com.S_Health.GenderHealthCare.repository.MedicalProfileRepository;
import com.S_Health.GenderHealthCare.repository.ServiceRepository;
import com.S_Health.GenderHealthCare.service.appointment.AppointmentService;
import com.S_Health.GenderHealthCare.utils.AuthUtil;
import org.checkerframework.checker.units.qual.A;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MedicalProfileService {
    @Autowired
    MedicalProfileRepository medicalProfileRepository;
    @Autowired
    ServiceRepository serviceRepository;
    @Autowired
    AppointmentRepository appointmentRepository;
    @Autowired
    AppointmentService appointmentService;
    @Autowired
    AuthUtil authUtil;
    @Autowired
    ModelMapper modelMapper;

    public void createMedicalProfile(Appointment appointment) {
        User user = authUtil.getCurrentUser();
        com.S_Health.GenderHealthCare.entity.Service service = serviceRepository.findById(appointment.getService().getId())
                .orElseThrow(() -> new RuntimeException("Service not found"));
        // Tìm MedicalProfile đã tồn tại
        Optional<MedicalProfile> existingProfile = medicalProfileRepository
                .findByCustomerAndServiceAndIsActiveTrue(user, service);
        MedicalProfile medicalProfile;
        List<Appointment> appointments = new ArrayList<>();
        appointments.add(appointment);
        if (existingProfile.isPresent()) {
            medicalProfile = existingProfile.get();
        } else {
            // Tạo mới nếu chưa tồn tại
            medicalProfile = new MedicalProfile();
            medicalProfile.setCustomer(user);
            medicalProfile.setService(service);
            medicalProfile = medicalProfileRepository.save(medicalProfile);
        }
        // Gán medicalProfile cho appointment và lưu
        appointment.setMedicalProfile(medicalProfile);
        appointmentRepository.save(appointment);
        medicalProfile.setAppointments(appointments);
        medicalProfileRepository.save(medicalProfile);
    }

    public MedicalProfileDTO getMyProfile(Long serviceId) {
        User user = authUtil.getCurrentUser();
        com.S_Health.GenderHealthCare.entity.Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy dịch vụ này!"));
        MedicalProfile medicalProfile = medicalProfileRepository.findByCustomerAndServiceAndIsActiveTrue(user, service)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy hồ sơ khám bệnh!"));
        return modelMapper.map(medicalProfile, MedicalProfileDTO.class);
    }
    public List<AppointmentDTO> getAppointmentsByMedicalProfile(long medicalProfileId) {
        List<Appointment> appointments = appointmentRepository.findByMedicalProfileId(medicalProfileId);
        return appointments.stream()
                .map(app -> appointmentService.getAppointmentById(app.getId()))
                .collect(Collectors.toList());
    }
    public List<AppointmentDTO> getAppointmentByStatusAndMedicalProfile(Long profileId, AppointmentStatus status) {
        List<Appointment> appointments = appointmentRepository.findByMedicalProfileIdAndStatus(profileId, status);
        return appointments.stream()
                .map(entry -> appointmentService.getAppointmentById(entry.getId()))
                .collect(Collectors.toList());
    }
}

