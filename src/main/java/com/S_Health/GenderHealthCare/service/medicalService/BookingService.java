package com.S_Health.GenderHealthCare.service.MedicalService;

import com.S_Health.GenderHealthCare.dto.SlotDTO;
import com.S_Health.GenderHealthCare.dto.request.service.BookingRequest;
import com.S_Health.GenderHealthCare.dto.response.AppointmentDetailDTO;
import com.S_Health.GenderHealthCare.dto.response.BookingResponse;
import com.S_Health.GenderHealthCare.entity.*;
import com.S_Health.GenderHealthCare.enums.AppointmentStatus;
import com.S_Health.GenderHealthCare.enums.SlotStatus;
import com.S_Health.GenderHealthCare.exception.exceptions.BadRequestException;
import com.S_Health.GenderHealthCare.repository.*;
import com.S_Health.GenderHealthCare.service.schedule.ServiceSlotPoolService;
import com.S_Health.GenderHealthCare.utils.AuthUtil;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
    @Autowired
    ServiceSlotPoolRepository serviceSlotPoolRepository;
    @Autowired
    ConsultantSlotRepository consultantSlotRepository;
    @Autowired
    AuthUtil authUtil;

    @Transactional
    public BookingResponse bookingService(BookingRequest request, long customerId) {
        // 1. Validate & lấy dữ liệu cần thiết
        BookingContext context = validateAndFetchBookingEntities(request, customerId);

        // 2. Tạo appointment
        Appointment appointment = new Appointment();
        appointment.setCustomer(context.customer());
        appointment.setNote(request.getNote());
        appointment.setStatus(AppointmentStatus.PENDING);
        appointment.setService(context.service());
        appointment.setServiceSlotPool(context.slotPool());
        appointment.setPreferredDate(request.getPreferredDate());
        appointmentRepository.save(appointment);

        // 3. Lặp các service con (nếu combo)
        List<AppointmentDetailDTO> appointmentDetails = new ArrayList<>();
        List<ConsultantSlot> updatedSlots = new ArrayList<>();
        double totalPrice = 0;

        for (com.S_Health.GenderHealthCare.entity.Service sub : context.services()) {
            AppointmentDetailData result = createAppointmentDetail(request, appointment, sub);
            appointmentDetails.add(result.dto());
            updatedSlots.add(result.slot());
            totalPrice += sub.getPrice();
        }

        // 4. Cập nhật lại slot pool tổng
        updateServiceSlotPool(context.slotPool(), updatedSlots);

        // 5. Set giá và save lại appointment
        appointment.setPrice(totalPrice * (1 - (context.service().getDiscountPercent()/100)));
        appointmentRepository.save(appointment);
        consultantSlotRepository.saveAll(updatedSlots);

        return BookingResponse.builder()
                .appointmentId(appointment.getId())
                .customerName(context.customer().getFullname())
                .date(request.getPreferredDate())
                .time(request.getSlot())
                .note(request.getNote())
                .status(AppointmentStatus.PENDING)
                .details(appointmentDetails)
                .build();
    }

    //validate request
    private BookingContext validateAndFetchBookingEntities(BookingRequest request, long customerId) {
        com.S_Health.GenderHealthCare.entity.Service service = serviceRepository.findById(request.getService_id())
                .orElseThrow(() -> new BadRequestException("Không tìm thấy dịch vụ!"));

        ServiceSlotPool slotPool = serviceSlotPoolRepository.findById(request.getSlot_id())
                .orElseThrow(() -> new BadRequestException("Không tìm thấy khung giờ này!"));

        if (!slotPool.getDate().equals(request.getPreferredDate()) ||
                !slotPool.getStartTime().equals(request.getSlot())) {
            throw new BadRequestException("Khung giờ không khớp với ngày/giờ yêu cầu!");
        }

        User customer = authenticationRepository.findById(customerId)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy khách hàng!"));

        LocalDateTime slotTime = LocalDateTime.of(request.getPreferredDate(), request.getSlot());
        if (appointmentDetailRepository.existsByAppointment_Customer_IdAndSlotTime(customerId, slotTime)) {
            throw new BadRequestException("Bạn đã có lịch hẹn vào khung giờ này");
        }
        System.out.println(service.getComboItems());
        List<com.S_Health.GenderHealthCare.entity.Service> services = service.getIsCombo()
                ? service.getComboItems().stream().map(ComboItem::getSubService).toList()
                : List.of(service);
        System.out.println(services);
        return new BookingContext(service, slotPool, customer, services);
    }

    private record BookingContext(
            com.S_Health.GenderHealthCare.entity.Service service,
            ServiceSlotPool slotPool,
            User customer,
            List<com.S_Health.GenderHealthCare.entity.Service> services
    ) {
    }

    //tạo appointmentDetail và cập nhật consultantSlot
    private AppointmentDetailData createAppointmentDetail(BookingRequest request, Appointment appointment, com.S_Health.GenderHealthCare.entity.Service subService) {
        List<User> consultants = serviceSlotPoolService.getConsultantInSpecialization(subService.getId());
        for (User item : consultants){
            System.out.println(item.getFullname());
        }
        User consultant = findAvailableConsultant(request, consultants);
        if (consultant == null) {
            throw new BadRequestException("Không có tư vấn viên rảnh cho dịch vụ: " + subService.getName());
        }

        ConsultantSlot slot = consultantSlotRepository
                .findByConsultantAndDateAndStartTimeAndStatus(consultant, request.getPreferredDate(), request.getSlot(), SlotStatus.ACTIVE)
                .orElseThrow(() -> new BadRequestException("Khung giờ không tồn tại"));
        slot.setCurrentBooking(slot.getCurrentBooking() + 1);
        slot.setAvailableBooking(slot.getAvailableBooking() - 1);
        if (slot.getAvailableBooking() == 0) {
            slot.setStatus(SlotStatus.FULL);
        }
        consultantSlotRepository.save(slot);
        AppointmentDetail detail = new AppointmentDetail();
        detail.setAppointment(appointment);
        detail.setConsultant(consultant);
        detail.setService(subService);
        detail.setSlotTime(LocalDateTime.of(request.getPreferredDate(), request.getSlot()));
        appointmentDetailRepository.save(detail);

        AppointmentDetailDTO dto = AppointmentDetailDTO.builder()
                .id(detail.getId())
                .serviceName(subService.getName())
                .consultantName(consultant.getFullname())
                .startTime(request.getSlot())
                .status(AppointmentStatus.PENDING)
                .build();

        return new AppointmentDetailData(dto, slot);
    }

    private record AppointmentDetailData(AppointmentDetailDTO dto, ConsultantSlot slot) {
    }

    private void updateServiceSlotPool(ServiceSlotPool slotPool, List<ConsultantSlot> slots) {
        int max = slots.stream().mapToInt(ConsultantSlot::getMaxBooking).sum();
        int current = slots.stream().mapToInt(ConsultantSlot::getCurrentBooking).sum();
        int available = Math.max(0, max - current);

        slotPool.setMaxBooking(max);
        slotPool.setCurrentBooking(current);
        slotPool.setAvailableBooking(available);
        slotPool.setIsActive(available > 0);
    }

//    @Transactional
//    public BookingResponse bookingService(BookingRequest request, long customerId) {
//        com.S_Health.GenderHealthCare.entity.Service service = serviceRepository.findById(request.getService_id())
//                .orElseThrow(() -> new BadRequestException("Không tìm thấy dịch vụ!"));
//        ServiceSlotPool slotPool = serviceSlotPoolRepository.findById(request.getSlot_id())
//                .orElseThrow(() -> new BadRequestException("Không tìm thấy khung giờ này!"));
//        User customer = authenticationRepository.findById(customerId)
//                .orElseThrow(() -> new BadRequestException("Không tìm thấy khách hàng!"));
//        LocalDateTime slotDateTime = LocalDateTime.of(request.getPreferredDate(), request.getSlot());
//        boolean conflict = appointmentDetailRepository.existsByAppointment_Customer_IdAndSlotTime(customerId, slotDateTime);
//        if (conflict) {
//            throw new BadRequestException("Bạn đã có lịch hẹn vào khung giờ này");
//        }
//        // Tạo Appointment gốc
//        Appointment appointment = new Appointment();
//        appointment.setCustomer(customer);
//        appointment.setNote(request.getNote());
//        appointment.setStatus(AppointmentStatus.PENDING);
//        appointment.setCreated_at(LocalDateTime.now());
//        appointment.setService(service);
//        appointment.setServiceSlotPool(slotPool);
//        appointment.setPreferredDate(request.getPreferredDate());
//        appointmentRepository.save(appointment);
//        List<AppointmentDetailDTO> appointmentDetailDTOS = new ArrayList<>();
//
//        List<com.S_Health.GenderHealthCare.entity.Service> subServices = service.getIsCombo()
//                ? service.getSubServices().stream()
//                .map(ComboItem::getSubService)
//                .collect(Collectors.toList())
//                : List.of(service);
//        Double totalPrice = 0d;
//        List<ConsultantSlot> slots  = new ArrayList<>();
//        for (com.S_Health.GenderHealthCare.entity.Service sub : subServices) {
//            List<User> consultantsForSub = serviceSlotPoolService.getConsultantInSpecialization(sub.getId());
//            User availableConsultant = findAvailableConsultant(request, consultantsForSub);
//            if (availableConsultant == null) {
//                throw new BadRequestException("Không có tư vấn viên rảnh cho dịch vụ: " + sub.getName());
//            }
//            ConsultantSlot slot = consultantSlotRepository
//                    .findByConsultantAndDateAndStartTimeAndStatus(availableConsultant, request.getPreferredDate(), request.getSlot(), SlotStatus.ACTIVE)
//                    .orElseThrow(() -> new BadRequestException("Khung giờ này không tồn tại!"));
//            int current = slot.getCurrentBooking() + 1;
//            int available = slot.getAvailableBooking() - 1;
//            slot.setAvailableBooking(available);
//            slot.setCurrentBooking(current);
//            slots.add(slot);
//            totalPrice += sub.getPrice();
//            // Lưu AppointmentDetail
//            AppointmentDetail detail = new AppointmentDetail();
//            detail.setAppointment(appointment);
//            detail.setConsultant(availableConsultant);
//            detail.setService(sub);
//            detail.setSlotTime(LocalDateTime.of(request.getPreferredDate(), request.getSlot())); // dùng chung slot cho tất cả
//            appointmentDetailRepository.save(detail);
//
//            appointmentDetailDTOS.add(AppointmentDetailDTO.builder()
//                    .serviceName(sub.getName())
//                    .consultantName(availableConsultant.getFullname())
//                    .startTime(request.getSlot())
//                    .status(appointment.getStatus())
//                    .build());
//        }
//            int max = slots.stream().mapToInt(ConsultantSlot::getMaxBooking).sum();
//            int currentBookingSV = slots.stream().mapToInt(ConsultantSlot::getCurrentBooking).sum();
//            int availableSloSV = Math.max(0, max - currentBookingSV);
//            slotPool.setMaxBooking(max);
//            slotPool.setCurrentBooking(currentBookingSV);
//            slotPool.setAvailableBooking(availableSloSV);
//        appointment.setPrice(totalPrice * (1 - service.getDiscountPercent()));
//        appointmentRepository.save(appointment);
//        return BookingResponse.builder()
//                .appointmentId(appointment.getId())
//                .customerName(customer.getFullname())
//                .date(request.getPreferredDate())
//                .time(request.getSlot())
//                .note(request.getNote())
//                .status(AppointmentStatus.PENDING)
//                .details(appointmentDetailDTOS)
//                .build();
//    }

    public User findAvailableConsultant(BookingRequest request, List<User> consultants) {
        for (User consultant : consultants) {
            try {
                ConsultantSlot slot = consultantSlotRepository
                        .findByConsultantAndDateAndStartTimeAndStatus(
                                consultant,
                                request.getPreferredDate(),
                                request.getSlot(),
                                SlotStatus.ACTIVE
                        ).orElse(null);
                if (slot != null && slot.getAvailableBooking() > 0) {
                    return consultant;
                }
            } catch (Exception e) {
                System.out.println("Lỗi khi kiểm tra tư vấn viên " + consultant.getFullname() + ": " + e.getMessage());
            }
        }
        throw new BadRequestException("Không tìm thấy tư vấn viên nào khả dụng cho thời gian đã chọn!");
    }

}
