package com.S_Health.GenderHealthCare.service.schedule;

import com.S_Health.GenderHealthCare.dto.ServiceDTO;
import com.S_Health.GenderHealthCare.dto.request.schedule.ScheduleServiceRequest;
import com.S_Health.GenderHealthCare.dto.response.ScheduleConsultantResponse;
import com.S_Health.GenderHealthCare.dto.response.ScheduleServiceResponse;
import com.S_Health.GenderHealthCare.dto.response.TimeSlotDTO;
import com.S_Health.GenderHealthCare.entity.Schedule;
import com.S_Health.GenderHealthCare.entity.ServiceSlotPool;
import com.S_Health.GenderHealthCare.entity.Specialization;
import com.S_Health.GenderHealthCare.entity.User;
import com.S_Health.GenderHealthCare.repository.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
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

    public ScheduleServiceResponse getSlotFreeService(ScheduleServiceRequest request) {
        List<Specialization> specializations = specializationRepository.findByServiceId(request.getService_id());
        //lấy ra những bác sĩ có chuyên sâu đó
        //Thống kê các specializtion id có trong list trên
        List<Long> specializationIds = specializations.stream().map(Specialization::getId).toList();
        List<User> consultants = userRepository.findBySpecializations_IdIn(specializationIds);
        // thống kê lịch của những consultant đó
        //tạo List consultantIds
        List<Long> consultantIds = consultants.stream().map(User::getId).toList();
        List<Schedule> schedules = scheduleRepository.findByConsultantIdInAndWorkDateBetween(consultantIds, request.getRangeDate().getFrom(), request.getRangeDate().getTo());
        //tạo date map
        Map<LocalDate, List<TimeSlotDTO>> dateTimeSlots = schedules.stream()
                .collect(Collectors.groupingBy(Schedule::getWorkDate,
                        Collectors.mapping(schedule -> new TimeSlotDTO(schedule.getStartTime(), schedule.getEndTime()),
                                Collectors.toList())));
        List<ScheduleConsultantResponse> scheduleResponses = dateTimeSlots.entrySet().stream()
                .map(entry -> new ScheduleConsultantResponse(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(ScheduleConsultantResponse::getWorkDate))
                .toList();
        Optional<com.S_Health.GenderHealthCare.entity.Service> service = serviceRepository.findById(request.getService_id());
        ServiceDTO serviceDTO = modelMapper.map(service, ServiceDTO.class);
        //lưu lịch rảnh của service vào serviceSlotPool
        for (ScheduleConsultantResponse daily : scheduleResponses) {
            for (TimeSlotDTO timeSlot : daily.getTimeSlotDTOs()) {
                // Kiểm tra đã tồn tại chưa (tránh duplicate)
                boolean exists = serviceSlotPoolRepository
                        .findByService_idAndDateAndStartTime(request.getService_id(), daily.getWorkDate(), timeSlot.getStartTime())
                        .isPresent();
                if (!exists) {
                    ServiceSlotPool pool = ServiceSlotPool.builder()
                            .service(service.get())
                            .date(daily.getWorkDate())
                            .startTime(timeSlot.getStartTime())
                            .endTime(timeSlot.getEndTime())
                            .maxBooking(consultants.size() * 6)
                            .currentBooking(0)
                            .isActive(true)
                            .build();
                    serviceSlotPoolRepository.save(pool);
                }
            }
        }
        return new ScheduleServiceResponse(serviceDTO, scheduleResponses);
    }
}