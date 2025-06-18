package com.S_Health.GenderHealthCare.service.MedicalService;

import com.S_Health.GenderHealthCare.dto.request.service.BookingRequest;
import com.S_Health.GenderHealthCare.dto.response.AppointmentDetailDTO;
import com.S_Health.GenderHealthCare.dto.response.BookingResponse;
import com.S_Health.GenderHealthCare.entity.Appointment;
import com.S_Health.GenderHealthCare.entity.AppointmentDetail;
import com.S_Health.GenderHealthCare.entity.ComboItem;
import com.S_Health.GenderHealthCare.entity.User;
import com.S_Health.GenderHealthCare.enums.AppointmentStatus;
import com.S_Health.GenderHealthCare.repository.AppointmentDetailRepository;
import com.S_Health.GenderHealthCare.repository.AppointmentRepository;
import com.S_Health.GenderHealthCare.repository.AuthenticationRepository;
import com.S_Health.GenderHealthCare.repository.ServiceRepository;
import com.S_Health.GenderHealthCare.service.schedule.ServiceSlotPoolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingService {
    @Autowired
    ServiceSlotPoolService serviceSlotPoolService;
    @Autowired
    ServiceRepository serviceRepository;
    @Autowired
    AuthenticationRepository authenticationRepository;
    @Autowired
    AppointmentDetailRepository appointmentDetailRepository;
    @Autowired
    AppointmentRepository appointmentRepository;

    public BookingResponse bookingService(BookingRequest request, long customerId) {
        com.S_Health.GenderHealthCare.entity.Service service = serviceRepository.findById(request.getService_id())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy dịch vụ!"));

        User customer = authenticationRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khách hàng!"));
        LocalDateTime slotDateTime = LocalDateTime.of(request.getPreferredDate(), request.getSlot());
        boolean conflict = appointmentDetailRepository.existsByAppointment_Customer_IdAndSlotTime(customerId, slotDateTime);
        if (conflict) {
            throw new IllegalArgumentException("Bạn đã có lịch hẹn vào khung giờ này");
        }
        // Tạo Appointment gốc
        Appointment appointment = new Appointment();
        appointment.setCustomer(customer);
        appointment.setNote(request.getNote());
        appointment.setStatus(AppointmentStatus.PENDING);
        appointment.setCreated_at(LocalDateTime.now());
        appointment.setPreferredDate(request.getPreferredDate());

        List<AppointmentDetailDTO> appointmentDetailDTOS = new ArrayList<>();

        List<com.S_Health.GenderHealthCare.entity.Service> subServices = service.getIsCombo()
                ? service.getSubServices().stream()
                .map(ComboItem::getSubService)
                .collect(Collectors.toList())
                : List.of(service);
        Double totalPrice = 0d;
        for (com.S_Health.GenderHealthCare.entity.Service sub : subServices) {
            List<User> consultantsForSub = serviceSlotPoolService.getConsultantInSpecialization(sub.getId());
            User availableConsultant = findAvailableConsultant(request, consultantsForSub);
            if (availableConsultant == null) {
                throw new IllegalArgumentException("Không có tư vấn viên rảnh cho dịch vụ: " + sub.getName());
            }

            totalPrice += sub.getPrice();
            // Lưu AppointmentDetail
            AppointmentDetail detail = new AppointmentDetail();
            detail.setAppointment(appointment);
            detail.setConsultant(availableConsultant);
            detail.setService(sub);
            detail.setSlotTime(LocalDateTime.of(request.getPreferredDate(), request.getSlot())); // dùng chung slot cho tất cả
            appointmentDetailRepository.save(detail);

            appointmentDetailDTOS.add(AppointmentDetailDTO.builder()
                    .serviceName(sub.getName())
                    .consultantName(availableConsultant.getFullname())
                    .build());
        }
        appointment.setPrice(totalPrice * (1 - service.getDiscountPercent()));
        appointmentRepository.save(appointment);
        return BookingResponse.builder()
                .appointmentId(appointment.getId())
                .customerName(customer.getFullname())
                .date(request.getPreferredDate())
                .time(request.getSlot())
                .note(request.getNote())
                .status(AppointmentStatus.PENDING)
                .details(appointmentDetailDTOS)
                .build();
    }

    public User findAvailableConsultant(BookingRequest request, List<User> consultants) {
        for (User consultant : consultants) {
            boolean hasSchedule = consultant.getSchedules().stream()
                    .anyMatch(s -> s.getWorkDate().equals(request.getPreferredDate()) &&
                            !request.getSlot().isBefore(s.getStartTime()) &&
                            !request.getSlot().plusMinutes(90).isAfter(s.getEndTime()));
            if (!hasSchedule) continue;
            LocalDateTime slotTime = LocalDateTime.of(request.getPreferredDate(), request.getSlot());
            int booked = appointmentDetailRepository.countByConsultant_idAndSlotTime(consultant.getId(), slotTime);

            if (booked < 6) {
                return consultant;
            }
        }
        return null;
    }
}
