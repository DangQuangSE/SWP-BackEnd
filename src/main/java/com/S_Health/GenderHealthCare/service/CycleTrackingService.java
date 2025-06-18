package com.S_Health.GenderHealthCare.service;

import com.S_Health.GenderHealthCare.dto.request.service.CycleTrackingRequest;
import com.S_Health.GenderHealthCare.entity.CycleTracking;
import com.S_Health.GenderHealthCare.entity.User;
import com.S_Health.GenderHealthCare.enums.Symptoms;
import com.S_Health.GenderHealthCare.repository.CycleTrackingRepository;
import com.S_Health.GenderHealthCare.repository.UserRepository;
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

    public void saveDailyLog(CycleTrackingRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        CycleTracking log = cycleTrackingRepository.findByUser_IdAndStartDate(request.getUserId(),
                        request.getStartDate())
                .orElse(new CycleTracking());

        List<Symptoms> symptoms = Optional.ofNullable(request.getSymptoms()).orElse(List.of());

        String sym = symptoms.stream()
                .map(Enum::name)
                .collect(Collectors.joining(","));// lấy enum các triệu chứng


        log.setUser(user);
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
