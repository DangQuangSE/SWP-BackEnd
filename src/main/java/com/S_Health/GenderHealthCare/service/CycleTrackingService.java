package com.S_Health.GenderHealthCare.service;

import com.S_Health.GenderHealthCare.dto.request.service.CycleTrackingRequest;
import com.S_Health.GenderHealthCare.entity.CycleTracking;
import com.S_Health.GenderHealthCare.entity.User;
import com.S_Health.GenderHealthCare.enums.Symptoms;
import com.S_Health.GenderHealthCare.repository.CycleTrackingRepository;
import com.S_Health.GenderHealthCare.repository.UserRepository;
import com.S_Health.GenderHealthCare.utils.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CycleTrackingService {
    @Autowired
    private CycleTrackingRepository cycleTrackingRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AuthUtil authUtil;

    public void saveDailyLog(CycleTrackingRequest request) {


        Long userId = authUtil.getCurrentUserId();
        User userTracId = new User();
        userTracId.setId(userId);

        CycleTracking log = cycleTrackingRepository.findByUser_IdAndStartDate(userId, request.getStartDate())
                .orElse(new CycleTracking());

        List<Symptoms> symptoms = Optional.ofNullable(request.getSymptoms()).orElse(List.of());

        String sym = symptoms.stream()
                .map(Enum::name)
                .collect(Collectors.joining(","));// lấy enum các triệu chứng


        log.setUser(userTracId);
        log.setStartDate(request.getStartDate());
        log.setIsPeriodStart(request.getIsPeriodStart());
        log.setSymptoms(sym);
        log.setCreatedAt(LocalDateTime.now());

        cycleTrackingRepository.save(log);
    }

    public List<CycleTrackingRequest> getLogsByUser(Long userId) {
        return cycleTrackingRepository.findAllByUserId(userId).stream().map(log -> {
            CycleTrackingRequest dto = new CycleTrackingRequest();
            dto.setUserId(userId);
            dto.setStartDate(log.getStartDate());
            dto.setIsPeriodStart(log.getIsPeriodStart());

            List<Symptoms> symptoms = Optional.ofNullable(log.getSymptoms())
                    .filter(s -> !s.isBlank())
                    .map(s -> Arrays.stream(s.split(","))
                            .map(String::trim)
                            .map(Symptoms::valueOf)
                            .collect(Collectors.toList()))
                    .orElse(List.of());

            dto.setSymptoms(symptoms);
            return dto;
        }).collect(Collectors.toList());
    }
}
