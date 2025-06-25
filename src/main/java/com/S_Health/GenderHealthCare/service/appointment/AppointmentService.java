package com.S_Health.GenderHealthCare.service.appointment;

import com.S_Health.GenderHealthCare.dto.AppointmentDTO;
import com.S_Health.GenderHealthCare.dto.AppointmentDetailDTO;
import com.S_Health.GenderHealthCare.dto.ResultDTO;
import com.S_Health.GenderHealthCare.dto.request.appointment.UpdateAppointmentRequest;
import com.S_Health.GenderHealthCare.dto.request.service.BookingRequest;
import com.S_Health.GenderHealthCare.entity.*;
import com.S_Health.GenderHealthCare.enums.AppointmentStatus;
import com.S_Health.GenderHealthCare.enums.PaymentStatus;
import com.S_Health.GenderHealthCare.enums.UserRole;
import com.S_Health.GenderHealthCare.exception.exceptions.BadRequestException;
import com.S_Health.GenderHealthCare.repository.*;
import com.S_Health.GenderHealthCare.service.MedicalService.BookingService;
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
                appointment.setStatus(request.getStatus());
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
    }

    public void cancelAppointment(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy lịch hẹn!"));

        if (appointment.getStatus() == AppointmentStatus.CANCELED) {
            throw new BadRequestException("Lịch hẹn đã bị hủy trước đó.");
        }
        // Đánh dấu các AppointmentDetail là không hoạt động
        List<AppointmentDetail> details = appointmentDetailRepository.findByAppointmentAndIsActiveTrue(appointment);
        for (AppointmentDetail detail : details) {
            detail.setIsActive(false);
            User consultant = detail.getConsultant();
            ConsultantSlot consultantSlot = consultantSlotRepository
                    .findByConsultantAndDateAndStartTimeAndIsActiveTrue(detail.getConsultant(), detail.getSlotTime().toLocalDate(), detail.getSlotTime().toLocalTime());
            consultantSlot.setCurrentBooking(consultantSlot.getCurrentBooking() - 1);
            consultantSlot.setAvailableBooking(consultantSlot.getAvailableBooking() + 1);
            consultantSlotRepository.save(consultantSlot);
        }
        appointmentDetailRepository.saveAll(details);
        // Cập nhật trạng thái lịch hẹn
        appointment.setStatus(AppointmentStatus.CANCELED);
        appointment.setIsActive(false);
        // Hoàn slot: ServiceSlotPool
        ServiceSlotPool slot = appointment.getServiceSlotPool();
        if (slot != null) {
            slot.setAvailableBooking(slot.getAvailableBooking() + 1);
            slot.setCurrentBooking(slot.getCurrentBooking() - 1);
            serviceSlotPoolRepository.save(slot);
        }
        appointmentRepository.save(appointment);
    }

    public void checkInAppointment(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy lịch hẹn!"));
        appointment.setStatus(AppointmentStatus.CHECKED);
        List<AppointmentDetail> appointmentDetails = appointmentDetailRepository.findByAppointment(appointment);
        for (AppointmentDetail appointmentDetail : appointmentDetails){
            appointmentDetail.setStatus(AppointmentStatus.CHECKED);
            appointmentDetailRepository.save(appointmentDetail);
        }
        appointmentRepository.save(appointment);
    }

    public List<AppointmentDTO> getAppointmentsForDoctorOnDate(LocalDate date) {
        return null;
    }
}
