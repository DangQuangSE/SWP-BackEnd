package com.S_Health.GenderHealthCare.api;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api")
@RestController
@SecurityRequirement(name = "api")
public class BookingAPI {
}
