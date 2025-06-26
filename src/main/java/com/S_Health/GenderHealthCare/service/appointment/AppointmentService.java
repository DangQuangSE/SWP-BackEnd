package com.S_Health.GenderHealthCare.service.appointment;

import com.S_Health.GenderHealthCare.dto.AppointmentDTO;
import com.S_Health.GenderHealthCare.dto.AppointmentDetailDTO;
import com.S_Health.GenderHealthCare.dto.PatientHistoryDTO;
import com.S_Health.GenderHealthCare.dto.ResultDTO;
import com.S_Health.GenderHealthCare.dto.request.appointment.UpdateAppointmentRequest;
import com.S_Health.GenderHealthCare.dto.response.MedicalProfileDTO;
import com.S_Health.GenderHealthCare.entity.*;
import com.S_Health.GenderHealthCare.enums.AppointmentStatus;
import com.S_Health.GenderHealthCare.enums.UserRole;
import com.S_Health.GenderHealthCare.exception.exceptions.BadRequestException;
import com.S_Health.GenderHealthCare.repository.*;
import com.S_Health.GenderHealthCare.service.audit.AppointmentAuditService;
import com.S_Health.GenderHealthCare.utils.AuthUtil;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AppointmentService {
    @Autowired
    AppointmentRepository appointmentRepository;
    @Autowired
    AppointmentDetailRepository appointmentDetailRepository;
    @Autowired
    MedicalResultRepository medicalResultRepository;
    @Autowired
    ServiceSlotPoolRepository serviceSlotPoolRepository;
    @Autowired
    AuthenticationRepository authenticationRepository;
    @Autowired
    ConsultantSlotRepository consultantSlotRepository;
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    AuthUtil authUtil;
    @Autowired
    AppointmentAuditService auditService;


    public AppointmentDTO getAppointmentById(long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("không tìm thấy lịch hẹn này!"));
        //lấy ra danh sách appointmentDetail;
        List<AppointmentDetail> appointmentDetails = appointmentDetailRepository.findByAppointmentAndIsActiveTrue(appointment);
        List<AppointmentDetailDTO> detailDTOS = new ArrayList<>();
        //lấy ra danh sách result
        for (AppointmentDetail appointmentDT : appointmentDetails) {
            MedicalResult medicalResult = medicalResultRepository.findByAppointmentDetail(appointmentDT)
                    .orElseThrow(() -> new BadRequestException("Không có kết quả!"));
            AppointmentDetailDTO detailDTO = modelMapper.map(appointmentDT, AppointmentDetailDTO.class);
            detailDTO.setMedicalResult(modelMapper.map(medicalResult, ResultDTO.class));
            detailDTOS.add(detailDTO);
        }
        AppointmentDTO appointmentDTO = modelMapper.map(appointment, AppointmentDTO.class);
        appointmentDTO.setAppointmentDetails(detailDTOS);
        return appointmentDTO;
    }

    @Transactional
    public AppointmentDTO updateAppointment(Long appointmentId, UpdateAppointmentRequest request) {

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy lịch hẹn!"));

        Long userId = authUtil.getCurrentUserId();
        User user = authUtil.getCurrentUser();

        boolean isOwner = appointment.getCustomer().getId() == (userId);
        boolean isPrivileged = user.getRole() == UserRole.ADMIN
                || user.getRole() == UserRole.STAFF;


        if (!isOwner && !isPrivileged) {
            throw new BadRequestException("Bạn không có quyền chỉnh sửa lịch hẹn này!");
        }

        // Nếu thay đổi slot
        if (request.getSlotId() != null) {
            ServiceSlotPool newSlot = serviceSlotPoolRepository.findById(request.getSlotId())
                    .orElseThrow(() -> new BadRequestException("Không tìm thấy slot mới"));

            LocalDateTime newSlotTime = LocalDateTime.of(request.getPreferredDate(), newSlot.getStartTime());

            // Nếu là user thì giới hạn đổi slot phải trước ít nhất 1 ngày
            if (isOwner && newSlotTime.minusDays(1).isBefore(LocalDateTime.now())) {
                throw new BadRequestException("Chỉ được đổi slot trước ít nhất 1 ngày!");
            }

            // Cập nhật slot cũ và mới
            ServiceSlotPool oldSlot = appointment.getServiceSlotPool();
            oldSlot.setAvailableBooking(oldSlot.getAvailableBooking() + 1);
            oldSlot.setCurrentBooking(oldSlot.getCurrentBooking() - 1);

            newSlot.setAvailableBooking(newSlot.getAvailableBooking() - 1);
            newSlot.setCurrentBooking(newSlot.getCurrentBooking() + 1);

            serviceSlotPoolRepository.save(oldSlot);
            serviceSlotPoolRepository.save(newSlot);

            appointment.setServiceSlotPool(newSlot);
            appointment.setPreferredDate(request.getPreferredDate());
        }

        if (request.getNote() != null) {
            appointment.setNote(request.getNote());
        }

        //  Các thuộc tính chỉ có staff/admin được sửa
        if (isPrivileged) {
            if (request.getStatus() != null) {
                AppointmentStatus oldStatus = appointment.getStatus();
                if (!oldStatus.equals(request.getStatus())) {
                    // Ghi log thay đổi trạng thái
                    auditService.logStatusChange(
                        appointmentId,
                        oldStatus,
                        request.getStatus(),
                        user,
                        "Cập nhật trạng thái qua API updateAppointment"
                    );
                    appointment.setStatus(request.getStatus());
                }
            }
            if (request.getConsultantId() != null) {
                User consultant = authenticationRepository.findById(request.getConsultantId())
                        .orElseThrow(() -> new BadRequestException("Không tìm thấy tư vấn viên!"));
                appointment.setConsultant(consultant);
            }
            if (request.getPrice() != null) {
                appointment.setPrice(request.getPrice());
            }
        }

        appointmentRepository.save(appointment);
        return getAppointmentById(appointmentId);
    }


    @Transactional
    public void deleteAppointment(long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy lịch hẹn!"));
        List<AppointmentDetail> details = appointmentDetailRepository.findByAppointment(appointment);
        for (AppointmentDetail detail : details) {
            detail.setIsActive(false);
        }
        appointmentDetailRepository.saveAll(details);
        appointment.setIsActive(false);
        appointmentRepository.save(appointment);
        User currentUser = authUtil.getCurrentUser();
        AppointmentStatus oldStatus = appointment.getStatus();
        auditService.logStatusChange(
                id,
                oldStatus,
                AppointmentStatus.DELETED,
                currentUser,
                "Xóa lịch hẹn"
        );
    }

    public void cancelAppointment(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy lịch hẹn!"));

        if (appointment.getStatus() == AppointmentStatus.CANCELED) {
            throw new BadRequestException("Lịch hẹn đã bị hủy trước đó.");
        }

        AppointmentStatus oldStatus = appointment.getStatus();

        // Đánh dấu các AppointmentDetail là không hoạt động
        List<AppointmentDetail> details = appointmentDetailRepository.findByAppointmentAndIsActiveTrue(appointment);
        for (AppointmentDetail detail : details) {
            ConsultantSlot consultantSlot = consultantSlotRepository
                    .findByConsultantAndDateAndStartTimeAndIsActiveTrue(detail.getConsultant(), detail.getSlotTime().toLocalDate(), detail.getSlotTime().toLocalTime());
            consultantSlot.setCurrentBooking(consultantSlot.getCurrentBooking() - 1);
            consultantSlot.setAvailableBooking(consultantSlot.getAvailableBooking() + 1);
            consultantSlotRepository.save(consultantSlot);
        }
        appointmentDetailRepository.saveAll(details);
        // Cập nhật trạng thái lịch hẹn
        appointment.setStatus(AppointmentStatus.CANCELED);
        appointment.setUpdate_at(LocalDateTime.now());
        // Hoàn slot: ServiceSlotPool
        ServiceSlotPool slot = appointment.getServiceSlotPool();
        if (slot != null) {
            slot.setAvailableBooking(slot.getAvailableBooking() + 1);
            slot.setCurrentBooking(slot.getCurrentBooking() - 1);
            serviceSlotPoolRepository.save(slot);
        }
        appointmentRepository.save(appointment);

        // Ghi log thay đổi trạng thái
        User currentUser = authUtil.getCurrentUser();
        auditService.logStatusChange(
            id,
            oldStatus,
            AppointmentStatus.CANCELED,
            currentUser,
            "Hủy lịch hẹn"
        );
    }

    public void checkInAppointment(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy lịch hẹn!"));
        if (appointment.getStatus() == AppointmentStatus.CHECKED) {
            throw new BadRequestException("Lịch hẹn này đã được check in!");
        }
        try {
            AppointmentStatus oldStatus = appointment.getStatus();
            appointment.setStatus(AppointmentStatus.CHECKED);
            appointment.setUpdate_at(LocalDateTime.now());

            List<AppointmentDetail> appointmentDetails = appointmentDetailRepository.findByAppointment(appointment);

            for (AppointmentDetail appointmentDetail : appointmentDetails) {
                appointmentDetail.setStatus(AppointmentStatus.CHECKED);
                appointmentDetailRepository.save(appointmentDetail);
            }

            appointmentRepository.save(appointment);

            // Ghi log thay đổi trạng thái
            User currentUser = authUtil.getCurrentUser();
            auditService.logStatusChange(
                id,
                oldStatus,
                AppointmentStatus.CHECKED,
                currentUser,
                "Check-in lịch hẹn"
            );
        } catch (Exception e) {
            throw new BadRequestException("Không thể cập nhật trạng thái lịch hẹn: " + e.getMessage());
        }

    }

    public List<AppointmentDTO> getAppointmentsForConsultantOnDate(LocalDate date, AppointmentStatus status) {
        // Lấy thông tin bác sĩ hiện tại
        User currentDoctor = authUtil.getCurrentUser();

        // Tìm tất cả các appointments theo ngày và bác sĩ
        List<Appointment> appointments;
        if (status != null) {
            // Nếu có status, lọc theo cả ngày và status
            appointments = appointmentRepository.findByPreferredDateAndConsultantAndStatusAndIsActiveTrue(
                    date, currentDoctor, status);
        } else {
            // Nếu không có status, chỉ lọc theo ngày
            appointments = appointmentRepository.findByPreferredDateAndConsultantAndIsActiveTrue(
                    date, currentDoctor);
        }

        // Chuyển đổi sang DTO và trả về kết quả
        return appointments.stream()
                .map(appointment -> {
                    AppointmentDTO dto = modelMapper.map(appointment, AppointmentDTO.class);
                    // Lấy ra danh sách appointmentDetail
                    List<AppointmentDetail> details = appointmentDetailRepository
                            .findByAppointmentAndIsActiveTrue(appointment);
                    List<AppointmentDetailDTO> detailDTOs = details.stream()
                            .map(detail -> {
                                AppointmentDetailDTO detailDTO = modelMapper.map(detail, AppointmentDetailDTO.class);
                                // Lấy ra medical result nếu có
                                medicalResultRepository.findByAppointmentDetail(detail)
                                        .ifPresent(result ->
                                                detailDTO.setMedicalResult(modelMapper.map(result, ResultDTO.class))
                                        );
                                return detailDTO;
                            })
                            .collect(Collectors.toList());
                    dto.setAppointmentDetails(detailDTOs);
                    return dto;
                })
                .collect(Collectors.toList());
    }


    public PatientHistoryDTO getPatientHistoryFromAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy lịch hẹn"));
        // Get the medical profile
        MedicalProfile medicalProfile = appointment.getMedicalProfile();
        if (medicalProfile == null) {
            throw new BadRequestException("Lịch hẹn này không có hồ sơ y tế");
        }
        // Get past appointments for this patient with this service
        List<Appointment> pastAppointments = appointmentRepository.findByMedicalProfileAndStatusAndIsActiveTrue(
                medicalProfile, AppointmentStatus.COMPLETED);
        // Convert to DTOs
        List<AppointmentDTO> pastAppointmentDTOs = pastAppointments.stream()
                .map(app -> getAppointmentById(app.getId()))
                .collect(Collectors.toList());
        // Create and return the history DTO
        PatientHistoryDTO historyDTO = new com.S_Health.GenderHealthCare.dto.PatientHistoryDTO();
        historyDTO.setMedicalProfile(modelMapper.map(medicalProfile, MedicalProfileDTO.class));
        historyDTO.setPastAppointments(pastAppointmentDTOs);
        return historyDTO;
    }

    public List<AppointmentDTO> getAppointmentsByStatus(AppointmentStatus status) {
        User currentUser = authUtil.getCurrentUser();
        List<Appointment> appointments;
        if (currentUser.getRole() == UserRole.CONSULTANT) {
            // Lấy các cuộc hẹn mà consultant này phụ trách
            appointments = appointmentRepository.findByConsultantAndStatusAndIsActiveTrue(currentUser, status);
        } else if (currentUser.getRole() == UserRole.CUSTOMER) {
            // Lấy các cuộc hẹn của khách hàng này
            appointments = appointmentRepository.findByCustomerAndStatusAndIsActiveTrue(currentUser, status);
        } else {
            // Admin hoặc Staff có thể xem tất cả
            appointments = appointmentRepository.findByStatusAndIsActiveTrue(status);
        }
        return appointments.stream()
                .map(appointment -> {
                    AppointmentDTO dto = modelMapper.map(appointment, AppointmentDTO.class);
                    // Lấy ra danh sách appointmentDetail
                    List<AppointmentDetail> details = appointmentDetailRepository
                            .findByAppointmentAndIsActiveTrue(appointment);
                    List<AppointmentDetailDTO> detailDTOs = details.stream()
                            .map(detail -> {
                                AppointmentDetailDTO detailDTO = modelMapper.map(detail, AppointmentDetailDTO.class);
                                // Lấy ra medical result nếu có
                                medicalResultRepository.findByAppointmentDetail(detail)
                                        .ifPresent(result ->
                                                detailDTO.setMedicalResult(modelMapper.map(result, ResultDTO.class))
                                        );
                                return detailDTO;
                            })
                            .collect(Collectors.toList());
                    dto.setAppointmentDetails(detailDTOs);
                    return dto;
                })
                .collect(Collectors.toList());
    }
}