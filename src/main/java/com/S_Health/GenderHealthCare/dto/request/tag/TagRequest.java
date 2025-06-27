package com.S_Health.GenderHealthCare.dto.request.tag;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TagRequest {
    @NotBlank(message = "Tag name is required")
    private String name;

    private String description;
}
