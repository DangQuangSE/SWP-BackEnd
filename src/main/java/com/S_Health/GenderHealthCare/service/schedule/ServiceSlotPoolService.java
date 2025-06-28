package com.S_Health.GenderHealthCare.service.schedule;

import com.S_Health.GenderHealthCare.dto.ServiceDTO;
import com.S_Health.GenderHealthCare.dto.SlotDTO;
import com.S_Health.GenderHealthCare.dto.request.schedule.ScheduleServiceRequest;
import com.S_Health.GenderHealthCare.dto.response.WorkDateSlotResponse;
import com.S_Health.GenderHealthCare.dto.response.ScheduleServiceResponse;
import com.S_Health.GenderHealthCare.entity.*;
import com.S_Health.GenderHealthCare.enums.SlotStatus;
import com.S_Health.GenderHealthCare.repository.*;
import com.S_Health.GenderHealthCare.utils.TimeSlotUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
public class ServiceSlotPoolService {
    @Autowired
    SpecializationRepository specializationRepository;
    @Autowired
    AuthenticationRepository userRepository;
    @Autowired
    ScheduleRepository scheduleRepository;
    @Autowired
    ServiceRepository serviceRepository;
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    ServiceSlotPoolRepository serviceSlotPoolRepository;
    @Autowired
    ConsultantSlotRepository consultantSlotRepository;

    public ScheduleServiceResponse getSlotFreeService(ScheduleServiceRequest request) {
        Service service = serviceRepository.findById(request.getService_id())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy dịch vụ!"));
        //lấy ra các consultant liên quan tới chuyên môm đó
        List<User> consultants = getConsultantInSpecialization(request.getService_id());
        //lấy ra ConsultantSlot của tất cả consultant
        List<ConsultantSlot> consultantSlots = consultantSlotRepository
                .findByConsultantInAndDateBetweenAndStatus(consultants, request.getRangeDate().getFrom(), request.getRangeDate().getTo(), SlotStatus.ACTIVE);
        //gom nhóm theo data và startTime
        Map<LocalDateTime, List<ConsultantSlot>> slotMap = consultantSlots.stream()
                .collect(Collectors.groupingBy(slot -> LocalDateTime.of(slot.getDate(), slot.getStartTime())));
        Map<LocalDate, List<SlotDTO>> dailySlotMap = new HashMap<>();
        for (Map.Entry<LocalDateTime, List<ConsultantSlot>> entry : slotMap.entrySet()) {
            LocalDateTime dt = entry.getKey();
            LocalDate date = dt.toLocalDate();
            LocalTime start = dt.toLocalTime();
            LocalTime end = start.plusMinutes(90);
            List<ConsultantSlot> slots = entry.getValue();
            // Tổng hợp thông tin booking
            int max = slots.stream().mapToInt(ConsultantSlot::getMaxBooking).sum();
            int current = slots.stream().mapToInt(ConsultantSlot::getCurrentBooking).sum();
            int available = Math.max(0, max - current);

            // 6. Tìm hoặc tạo ServiceSlotPool
            ServiceSlotPool serviceSlotPool = serviceSlotPoolRepository
                    .findByService_idAndDateAndStartTime(service.getId(), date, start)
                    .orElseGet(() -> ServiceSlotPool.builder()
                            .service(service)
                            .date(date)
                            .startTime(start)
                            .endTime(end)
                            .isActive(true)
                            .slotStatus(SlotStatus.ACTIVE)
                            .build());
            serviceSlotPool.setMaxBooking(max);
            serviceSlotPool.setCurrentBooking(current);
            serviceSlotPool.setAvailableBooking(available);
            serviceSlotPool.setIsActive(available > 0);
            serviceSlotPoolRepository.save(serviceSlotPool);
            // 7. Build DTO trả ra
            SlotDTO slotDTO = new SlotDTO(
                    serviceSlotPool.getId(),
                    date,
                    start,
                    end,
                    max,
                    current,
                    available
            );
            dailySlotMap.computeIfAbsent(date, d -> new ArrayList<>()).add(slotDTO);
        }
        // Chuyển về dạng ScheduleServiceResponse
        List<WorkDateSlotResponse> schedule = dailySlotMap.entrySet().stream()
                .map(e -> new WorkDateSlotResponse(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(WorkDateSlotResponse::getWorkDate))
                .toList();
        ServiceDTO serviceDTO = modelMapper.map(service, ServiceDTO.class);
        return new ScheduleServiceResponse(serviceDTO, schedule);
    }

    public List<User> getConsultantInSpecialization(long service_id) {
        List<Specialization> specializations = specializationRepository.findByServicesIdAndIsActiveTrue(service_id);
        List<Long> specializationIds = specializations.stream().map(Specialization::getId).toList();
        List<User> consultants = userRepository.findBySpecializations_IdIn(specializationIds);
        return consultants;
    }
}