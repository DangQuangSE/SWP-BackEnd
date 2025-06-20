package com.S_Health.GenderHealthCare.service.schedule;

import com.S_Health.GenderHealthCare.dto.ServiceDTO;
import com.S_Health.GenderHealthCare.dto.SlotDTO;
import com.S_Health.GenderHealthCare.dto.request.schedule.ScheduleServiceRequest;
import com.S_Health.GenderHealthCare.dto.response.WorkDateSlotResponse;
import com.S_Health.GenderHealthCare.dto.response.ScheduleServiceResponse;
import com.S_Health.GenderHealthCare.entity.*;
import com.S_Health.GenderHealthCare.repository.*;
import com.S_Health.GenderHealthCare.utils.TimeSlotUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

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
        List<Long> consultantIds = consultants.stream().map(User::getId).toList();

        List<Schedule> schedules = scheduleRepository.findByConsultantIdInAndWorkDateBetween(
                consultantIds, request.getRangeDate().getFrom(), request.getRangeDate().getTo()
        );

        Service service = serviceRepository.findById(request.getService_id())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy dịch vụ"));

        Map<LocalDate, List<SlotDTO>> dateTimeSlots = new HashMap<>();

        for (Schedule schedule : schedules) {
            LocalDate date = schedule.getWorkDate();
            List<LocalTime> slotStarts = TimeSlotUtils.generateSlots(schedule.getStartTime(), schedule.getEndTime(), Duration.ofMinutes(90));

            for (LocalTime slotStart : slotStarts) {
                LocalTime slotEnd = slotStart.plusMinutes(90);

                // kiểm tra xem slot đã tồn tại
                Optional<ServiceSlotPool> existingSlotOpt = serviceSlotPoolRepository
                        .findByService_idAndDateAndStartTime(service.getId(), date, slotStart);

                ServiceSlotPool slotEntity;
                if (existingSlotOpt.isPresent()) {
                    slotEntity = existingSlotOpt.get();
                } else {
                    int booked = appointmentDetailRepository
                            .countByServiceIdAndDateAndStartTime(service.getId(), LocalDateTime.of(date, slotStart));

                    int max = consultants.size(); // mỗi bác sĩ có thể đảm nhận 1 ca trong cùng khung giờ
                    int available = Math.max(0, max - booked);

                    slotEntity = ServiceSlotPool.builder()
                            .service(service)
                            .date(date)
                            .startTime(slotStart)
                            .endTime(slotEnd)
                            .maxBooking(max)
                            .currentBooking(booked)
                            .availableBooking(available)
                            .isActive(true)
                            .build();

                    serviceSlotPoolRepository.save(slotEntity);
                }

                SlotDTO dto = new SlotDTO(
                        slotEntity.getId(),
                        slotEntity.getDate(),
                        slotEntity.getStartTime(),
                        slotEntity.getEndTime(),
                        slotEntity.getMaxBooking(),
                        slotEntity.getCurrentBooking(),
                        slotEntity.getAvailableBooking()
                );

                dateTimeSlots.computeIfAbsent(date, d -> new ArrayList<>()).add(dto);
            }
        }

        List<WorkDateSlotResponse> scheduleResponses = dateTimeSlots.entrySet().stream()
                .map(entry -> new WorkDateSlotResponse(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(WorkDateSlotResponse::getWorkDate))
                .toList();

        ServiceDTO serviceDTO = modelMapper.map(service, ServiceDTO.class);
        return new ScheduleServiceResponse(serviceDTO, scheduleResponses);
    }

//    public void updateAvailableBookingSlot(long service_id, LocalDate date, LocalTime start){
//        LocalTime slotTime =  start;
//        List<User> consultants = getConsultantInSpecialization(service_id);
//        int maxBookingPer = 6;
//        int availableTotal = 0;
//        for(User consultant : consultants){
//            Boolean hasSchedule = scheduleRepository.existsByConsultantIdAndWorkDateAndStartTime(consultant.getId(), date, start);
//            if(!hasSchedule) continue;
//            int booked = appointmentDetailRepository.countByConsultant_idAndSlotTime(consultant.getId(), slotTime);
//            int available = Math.max(0, maxBookingPer - booked);
//            availableTotal += available;
//        }
//        Optional<ServiceSlotPool> serviceSlotPool = serviceSlotPoolRepository.findByService_idAndDateAndStartTime(service_id, date, start);
//        int finalAvailableTotal = availableTotal;
//        serviceSlotPool.ifPresent(pool ->{
//           pool.setAvailableBooking(finalAvailableTotal);
//           serviceSlotPoolRepository.save(pool);
//        });
//    }
    public List<User> getConsultantInSpecialization(long service_id){
        List<Specialization> specializations = specializationRepository.findByServiceId(service_id);
        List<Long> specializationIds = specializations.stream().map(Specialization::getId).toList();
        List<User> consultants = userRepository.findBySpecializations_IdIn(specializationIds);
        return consultants;
    }
}