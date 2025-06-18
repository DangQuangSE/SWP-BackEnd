package com.S_Health.GenderHealthCare.service.schedule;

import com.S_Health.GenderHealthCare.dto.ServiceDTO;
import com.S_Health.GenderHealthCare.dto.request.schedule.ScheduleServiceRequest;
import com.S_Health.GenderHealthCare.dto.response.ScheduleConsultantResponse;
import com.S_Health.GenderHealthCare.dto.response.ScheduleServiceResponse;
import com.S_Health.GenderHealthCare.dto.TimeSlotDTO;
import com.S_Health.GenderHealthCare.entity.*;
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
        // 1. Lấy danh sách bác sĩ theo chuyên khoa
        List<User> consultants = getConsultantInSpecialization(request.getService_id());
        List<Long> consultantIds = consultants.stream().map(User::getId).toList();

        // 2. Lấy toàn bộ lịch làm việc của các bác sĩ trong khoảng ngày
        List<Schedule> schedules = scheduleRepository.findByConsultantIdInAndWorkDateBetween(
                consultantIds, request.getRangeDate().getFrom(), request.getRangeDate().getTo()
        );

        Optional<Service> serviceOpt = serviceRepository.findById(request.getService_id());
        if (serviceOpt.isEmpty()) throw new RuntimeException("Không tìm thấy dịch vụ");
        Service service = serviceOpt.get();

        Map<LocalDate, List<TimeSlotDTO>> dateTimeSlots = new HashMap<>();

        for (Schedule schedule : schedules) {
            Long consultantId = schedule.getConsultant().getId();
            LocalDate date = schedule.getWorkDate();
            List<LocalTime> slotStarts = TimeSlotUtils.generateSlots(schedule.getStartTime(), schedule.getEndTime(), Duration.ofMinutes(90));

            for (LocalTime slotStart : slotStarts) {
                LocalDateTime localTime =  LocalDateTime.of(date, slotStart);
                int booked = appointmentDetailRepository.countByConsultant_idAndSlotTime(consultantId, localTime);
                int available = Math.max(0, 6 - booked); // mỗi bác sĩ tối đa 6 slot

                // Thêm hoặc cập nhật vào map
                List<TimeSlotDTO> slotList = dateTimeSlots.computeIfAbsent(date, k -> new ArrayList<>());
                LocalTime slotEnd = slotStart.plusMinutes(90);

                Optional<TimeSlotDTO> existingSlot = slotList.stream()
                        .filter(ts -> ts.getStartTime().equals(slotStart) && ts.getEndTime().equals(slotEnd))
                        .findFirst();

                if (existingSlot.isPresent()) {
                    existingSlot.get().setAvailableBooking(existingSlot.get().getAvailableBooking() + available);
                } else {
                    slotList.add(new TimeSlotDTO(slotStart, slotEnd, available));
                }
            }
        }

        // 3. Ghi vào bảng ServiceSlotPool nếu slot chưa tồn tại
        for (Map.Entry<LocalDate, List<TimeSlotDTO>> entry : dateTimeSlots.entrySet()) {
            LocalDate date = entry.getKey();
            for (TimeSlotDTO slot : entry.getValue()) {
                boolean exists = serviceSlotPoolRepository.findByService_idAndDateAndStartTime(
                        request.getService_id(), date, slot.getStartTime()
                ).isPresent();

                if (!exists) {
                    int max = consultants.size() * 6;
                    int current = max - slot.getAvailableBooking();

                    ServiceSlotPool pool = ServiceSlotPool.builder()
                            .service(service)
                            .date(date)
                            .startTime(slot.getStartTime())
                            .endTime(slot.getEndTime())
                            .maxBooking(max)
                            .currentBooking(current)
                            .availableBooking(slot.getAvailableBooking())
                            .isActive(true)
                            .build();

                    serviceSlotPoolRepository.save(pool);
                }
            }
        }

        // 4. Tạo response
        List<ScheduleConsultantResponse> scheduleResponses = dateTimeSlots.entrySet().stream()
                .map(entry -> new ScheduleConsultantResponse(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(ScheduleConsultantResponse::getWorkDate))
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