package com.S_Health.GenderHealthCare.dto.response;

import com.S_Health.GenderHealthCare.dto.SpecializationDTO;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
public class RoomDTO {
    private long id;
    private String name;
    private String description;
    private SpecializationDTO specialization;
    private List<RoomConsultantDTO> consultants;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
