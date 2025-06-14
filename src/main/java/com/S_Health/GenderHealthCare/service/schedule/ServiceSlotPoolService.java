package com.S_Health.GenderHealthCare.service.schedule;

import com.S_Health.GenderHealthCare.dto.ServiceDTO;
import com.S_Health.GenderHealthCare.dto.request.schedule.ScheduleServiceRequest;
import com.S_Health.GenderHealthCare.dto.response.ScheduleConsultantResponse;
import com.S_Health.GenderHealthCare.dto.response.ScheduleServiceResponse;
import com.S_Health.GenderHealthCare.dto.response.TimeSlotDTO;
import com.S_Health.GenderHealthCare.entity.*;
import com.S_Health.GenderHealthCare.repository.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;

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
    AppointmentDetailRepository appointmentDetailRepository;

    public ScheduleServiceResponse getSlotFreeService(ScheduleServiceRequest request) {
        List<User> consultants = getConsultantInSpecialization(request.getService_id());
        // thống kê lịch của những consultant đó
        //tạo List consultantIds
        List<Long> consultantIds = consultants.stream().map(User::getId).toList();
        List<Schedule> schedules = scheduleRepository.findByConsultantIdInAndWorkDateBetween(consultantIds, request.getRangeDate().getFrom(), request.getRangeDate().getTo());
        //tạo date map
        Optional<Service> service = serviceRepository.findById(request.getService_id());
        Map<LocalDate, List<TimeSlotDTO>> dateTimeSlots = new HashMap<>();
        for (Schedule schedule : schedules) {
            Long consultantId = schedule.getConsultant().getId();
            LocalDate date = schedule.getWorkDate();
            LocalTime startTime = schedule.getStartTime();
            LocalTime endTime = schedule.getEndTime();
            LocalDateTime slotTime = LocalDateTime.of(date, startTime);

            int booked = appointmentDetailRepository.countByConsultant_idAndSlotTime(consultantId, slotTime);
            int available = Math.max(0, 6 - booked);
            // Cộng dồn available vào slot đó
            List<TimeSlotDTO> slots = dateTimeSlots.computeIfAbsent(date, k -> new ArrayList<>());
            Optional<TimeSlotDTO> existingSlot = slots.stream()
                    .filter(ts -> ts.getStartTime().equals(startTime) && ts.getEndTime().equals(endTime))
                    .findFirst();
            if (existingSlot.isPresent()) {
                existingSlot.get().setAvailable(existingSlot.get().getAvailable() + available);
            } else {
                slots.add(new TimeSlotDTO(startTime, endTime, available));
            }
        }
        for (Map.Entry<LocalDate, List<TimeSlotDTO>> entry : dateTimeSlots.entrySet()) {
            for (TimeSlotDTO slot : entry.getValue()) {
                boolean exists = serviceSlotPoolRepository.findByService_idAndDateAndStartTime(
                        request.getService_id(), entry.getKey(), slot.getStartTime()
                ).isPresent();
                if (!exists) {
                    int max = consultants.size()*6;
                    int current = max - slot.getAvailable();
                    ServiceSlotPool pool = ServiceSlotPool.builder()
                            .service(service.get())
                            .date(entry.getKey())
                            .startTime(slot.getStartTime())
                            .endTime(slot.getEndTime())
                            .maxBooking(consultants.size() * 6)
                            .currentBooking(current)
                            .availableBooking(slot.getAvailable())
                            .isActive(true)
                            .build();
                    serviceSlotPoolRepository.save(pool);
                }
            }
        }

        List<ScheduleConsultantResponse> scheduleResponses = dateTimeSlots.entrySet().stream()
                .map(entry -> new ScheduleConsultantResponse(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(ScheduleConsultantResponse::getWorkDate))
                .toList();

        ServiceDTO serviceDTO = modelMapper.map(service.get(), ServiceDTO.class);
        return new ScheduleServiceResponse(serviceDTO, scheduleResponses);
    }
    public void updateAvailableBookingSlot(long service_id, LocalDate date, LocalTime start){
        LocalDateTime slotTime = LocalDateTime.of(date, start);
        List<User> consultants = getConsultantInSpecialization(service_id);
        int maxBookingPer = 6;
        int availableTotal = 0;
        for(User consultant : consultants){
            Boolean hasSchedule = scheduleRepository.existsByConsultantIdAndWorkDateAndStartTime(consultant.getId(), date, start);
            if(!hasSchedule) continue;
            int booked = appointmentDetailRepository.countByConsultant_idAndSlotTime(consultant.getId(), slotTime);
            int available = Math.max(0, maxBookingPer - booked);
            availableTotal += available;
        }
        Optional<ServiceSlotPool> serviceSlotPool = serviceSlotPoolRepository.findByService_idAndDateAndStartTime(service_id, date, start);
        int finalAvailableTotal = availableTotal;
        serviceSlotPool.ifPresent(pool ->{
           pool.setAvailableBooking(finalAvailableTotal);
           serviceSlotPoolRepository.save(pool);
        });
    }
    public List<User> getConsultantInSpecialization(long service_id){
        List<Specialization> specializations = specializationRepository.findByServiceId(service_id);
        List<Long> specializationIds = specializations.stream().map(Specialization::getId).toList();
        List<User> consultants = userRepository.findBySpecializations_IdIn(specializationIds);
        return consultants;
    }
}