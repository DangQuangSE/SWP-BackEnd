package com.S_Health.GenderHealthCare.service.MedicalService;

import com.S_Health.GenderHealthCare.dto.request.service.BookingRequest;
import com.S_Health.GenderHealthCare.dto.AppointmentDetailDTO;
import com.S_Health.GenderHealthCare.dto.BasicMedicalProfileDTO;
import com.S_Health.GenderHealthCare.dto.SimpleRoomDTO;
import com.S_Health.GenderHealthCare.dto.response.BookingResponse;
import com.S_Health.GenderHealthCare.entity.*;
import com.S_Health.GenderHealthCare.enums.AppointmentStatus;
import com.S_Health.GenderHealthCare.enums.ServiceType;
import com.S_Health.GenderHealthCare.enums.SlotStatus;
import com.S_Health.GenderHealthCare.exception.exceptions.AppException;
import com.S_Health.GenderHealthCare.repository.*;
import com.S_Health.GenderHealthCare.service.medicalProfile.MedicalProfileService;
import com.S_Health.GenderHealthCare.service.schedule.ServiceSlotPoolService;
import com.S_Health.GenderHealthCare.utils.AuthUtil;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    MedicalProfileService medicalProfileService;
    @Autowired
    MedicalProfileRepository medicalProfileRepository;
    @Autowired
    AuthUtil authUtil;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    RoomRepository roomRepository;
    @Autowired
    RoomConsultantRepository roomConsultantRepository;

    @Transactional
    public BookingResponse bookingService(BookingRequest request) {
        // 1. Validate & lấy dữ liệu cần thiết
        Long customerId = authUtil.getCurrentUserId();
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
        for (com.S_Health.GenderHealthCare.entity.Service sub : context.services()) {
            AppointmentDetailData result = createAppointmentDetail(request, appointment, sub);
            appointmentDetails.add(result.dto());
            updatedSlots.add(result.slot());
        }

        // 4. Cập nhật lại slot pool tổng
        updateServiceSlotPool(context.slotPool(), updatedSlots);
        appointment.setPrice(context.service.getPrice());
        appointmentRepository.save(appointment);
        consultantSlotRepository.saveAll(updatedSlots);
        //6. Tạo medical profile theo service và add appointment vào.
        medicalProfileService.createMedicalProfile(appointment);
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
    public BookingContext validateAndFetchBookingEntities(BookingRequest request, long customerId) {
        com.S_Health.GenderHealthCare.entity.Service service = serviceRepository.findById(request.getService_id())
                .orElseThrow(() -> new AppException("Không tìm thấy dịch vụ!"));

        ServiceSlotPool slotPool = serviceSlotPoolRepository.findById(request.getSlot_id())
                .orElseThrow(() -> new AppException("Không tìm thấy khung giờ này!"));

        if (!slotPool.getDate().equals(request.getPreferredDate()) ||
                !slotPool.getStartTime().equals(request.getSlot())) {
            throw new AppException("Khung giờ không khớp với ngày/giờ yêu cầu!");
        }

        User customer = authenticationRepository.findById(customerId)
                .orElseThrow(() -> new AppException("Không tìm thấy khách hàng!"));

        LocalDateTime slotTime = LocalDateTime.of(request.getPreferredDate(), request.getSlot());
        if (appointmentDetailRepository.existsByAppointment_Customer_IdAndSlotTime(customerId, slotTime)) {
            throw new AppException("Bạn đã có lịch hẹn vào khung giờ này");
        }
        List<com.S_Health.GenderHealthCare.entity.Service> services = service.getIsCombo()
                ? service.getComboItems().stream().map(ComboItem::getSubService).toList()
                : List.of(service);
        return new BookingContext(service, slotPool, customer, services);
    }

    public static record BookingContext(
            com.S_Health.GenderHealthCare.entity.Service service,
            ServiceSlotPool slotPool,
            User customer,
            List<com.S_Health.GenderHealthCare.entity.Service> services
    ) {
    }

    //tạo appointmentDetail và cập nhật consultantSlot
    public AppointmentDetailData createAppointmentDetail(BookingRequest request, Appointment appointment, com.S_Health.GenderHealthCare.entity.Service subService) {
        List<User> consultants = serviceSlotPoolService.getConsultantInSpecialization(subService.getId());

        User consultant;
        if (request.getConsultantId() != null) {
            // Direct booking - tìm bác sĩ cụ thể
            consultant = consultants.stream()
                    .filter(c -> c.getId() == (request.getConsultantId()))
                    .findFirst()
                    .orElseThrow(() -> new AppException("Bác sĩ được chọn không phù hợp với dịch vụ này!"));

            // Kiểm tra bác sĩ có slot trống không
            ConsultantSlot checkSlot = consultantSlotRepository
                    .findByConsultantAndDateAndStartTimeAndStatus(consultant, request.getPreferredDate(), request.getSlot(), SlotStatus.ACTIVE)
                    .orElseThrow(() -> new AppException("Bác sĩ không có lịch trống vào thời gian này!"));

            if (checkSlot.getAvailableBooking() <= 0) {
                throw new AppException("Bác sĩ đã hết slot trong khung giờ này!");
            }
        } else {
            // Auto assign - dùng logic phân bổ đều
            consultant = findAvailableConsultant(request, consultants);
        }

        ConsultantSlot slot = consultantSlotRepository
                .findByConsultantAndDateAndStartTimeAndStatus(consultant, request.getPreferredDate(), request.getSlot(), SlotStatus.ACTIVE)
                .orElseThrow(() -> new AppException("Khung giờ không tồn tại"));
        slot.setCurrentBooking(slot.getCurrentBooking() + 1);
        slot.setAvailableBooking(slot.getAvailableBooking() - 1);
        if (slot.getAvailableBooking() == 0) {
            slot.setStatus(SlotStatus.FULL);
        }
        consultantSlotRepository.save(slot);
        // Auto-assign room chỉ khi cần (không phải consulting online)
        Room assignedRoom = null;
        if (subService.getType() != ServiceType.CONSULTING_ON) {
            assignedRoom = autoAssignRoomForConsultant(consultant, request.getPreferredDate(), request.getSlot());
        }

        AppointmentDetail detail = new AppointmentDetail();
        detail.setAppointment(appointment);
        detail.setConsultant(consultant);
        detail.setService(subService);
        detail.setRoom(assignedRoom);
        detail.setSlotTime(LocalDateTime.of(request.getPreferredDate(), request.getSlot()));
        appointmentDetailRepository.save(detail);

        AppointmentDetailDTO dto = modelMapper.map(detail, AppointmentDetailDTO.class);
        dto.setConsultantName(consultant.getFullname());
        dto.setServiceName(subService.getName());

        // Map Room information if available
        dto.setRoom(mapRoomToSimpleDTO(assignedRoom));

        return new AppointmentDetailData(dto, slot);
    }

    public static record AppointmentDetailData(AppointmentDetailDTO dto, ConsultantSlot slot) {
    }

    public void updateServiceSlotPool(ServiceSlotPool slotPool, List<ConsultantSlot> slots) {
        int max = slots.stream().mapToInt(ConsultantSlot::getMaxBooking).sum();
        int current = slots.stream().mapToInt(ConsultantSlot::getCurrentBooking).sum();
        int available = Math.max(0, max - current);

        slotPool.setMaxBooking(max);
        slotPool.setCurrentBooking(current);
        slotPool.setAvailableBooking(available);
        slotPool.setIsActive(available > 0);
        serviceSlotPoolRepository.save(slotPool);
    }

    public User findAvailableConsultant(BookingRequest request, List<User> consultants) {
        User bestConsultant = null;
        int lowestCurrentBooking = Integer.MAX_VALUE;

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
                    // Chọn bác sĩ có currentBooking thấp nhất để phân bổ đều bệnh nhân
                    if (slot.getCurrentBooking() < lowestCurrentBooking) {
                        lowestCurrentBooking = slot.getCurrentBooking();
                        bestConsultant = consultant;
                    }
                }
            } catch (Exception e) {
                throw new AppException("Lỗi khi kiểm tra tư vấn viên " + consultant.getFullname() + ": " + e.getMessage());
            }
        }

        if (bestConsultant == null) {
            throw new AppException("Không tìm thấy tư vấn viên nào khả dụng cho thời gian đã chọn!");
        }

        return bestConsultant;
    }

    private Room autoAssignRoomForConsultant(User consultant,
                                             java.time.LocalDate date,
                                             java.time.LocalTime timeSlot) {
        try {
            // Tìm tất cả phòng mà bác sĩ này được phân công
            List<RoomConsultant> consultantRooms = roomConsultantRepository
                    .findByConsultantAndIsActiveTrue(consultant);

            if (consultantRooms.isEmpty()) {
                // Fallback: Nếu bác sĩ chưa được phân phòng, assign theo specialization
                return fallbackAssignRoomBySpecialization(consultant);
            }

            // Tìm phòng phù hợp với thời gian làm việc
            for (RoomConsultant roomConsultant : consultantRooms) {
                Room room = roomConsultant.getRoom();

                // Kiểm tra phòng còn active
                if (!room.isActive()) {
                    continue;
                }

                // Kiểm tra thời gian làm việc
                if (isTimeSlotInWorkingHours(timeSlot, roomConsultant.getStartTime(), roomConsultant.getEndTime())) {
                    return room; // Tìm thấy phòng phù hợp
                }
            }

            // Nếu không có phòng nào phù hợp với thời gian, lấy phòng đầu tiên
            return consultantRooms.get(0).getRoom();

        } catch (Exception e) {
            throw new AppException("Có lỗi xảy ra khi auto-assign phòng: " + e.getMessage());
        }
    }

    /**
     * Fallback: Assign room theo specialization nếu bác sĩ chưa được phân phòng
     */
    private Room fallbackAssignRoomBySpecialization(User consultant) {
        try {
            if (consultant.getSpecializations() == null || consultant.getSpecializations().isEmpty()) {
                return null;
            }

            Specialization specialization = consultant.getSpecializations().get(0);

            // Tìm phòng theo specialization
            List<Room> availableRooms = roomRepository.findAll().stream()
                    .filter(room -> room.isActive())
                    .filter(room -> room.getSpecialization() != null &&
                            room.getSpecialization().getId().equals(specialization.getId()))
                    .toList();

            return availableRooms.isEmpty() ? null : availableRooms.get(0);

        } catch (Exception e) {
            throw new AppException("Có lỗi xảy ra khi auto-assign phòng: " + e.getMessage());
        }
    }

    /**
     * Kiểm tra thời gian slot có nằm trong giờ làm việc không
     */
    private boolean isTimeSlotInWorkingHours(java.time.LocalTime timeSlot,
                                             java.time.LocalTime startTime,
                                             java.time.LocalTime endTime) {
        return !timeSlot.isBefore(startTime) && !timeSlot.isAfter(endTime);
    }

    /**
     * Helper method to map Room to SimpleRoomDTO using ModelMapper
     */
    private SimpleRoomDTO mapRoomToSimpleDTO(Room room) {
        if (room == null) return null;

        SimpleRoomDTO roomDTO = modelMapper.map(room, SimpleRoomDTO.class);
        // Set specialization name manually since it's nested
        if (room.getSpecialization() != null) {
            roomDTO.setSpecializationName(room.getSpecialization().getName());
        }
        return roomDTO;
    }
}
