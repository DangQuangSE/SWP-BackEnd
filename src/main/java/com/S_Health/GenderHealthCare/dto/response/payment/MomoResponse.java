package com.S_Health.GenderHealthCare.dto.response.payment;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MomoResponse {
    int resultCode;
    String message;
    String payUrl;
    String orderId;
    String requestId;

}
