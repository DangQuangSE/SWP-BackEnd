package com.S_Health.GenderHealthCare.dto.request.payment;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.checkerframework.checker.units.qual.A;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MomoRequest {

    String partnerCode;
    String requestId;
    Long amount;
    String orderId;
    String orderInfo;
    String redirectUrl;
    String ipnUrl;
    String requestType;
    String extraData;
    String lang;
    String signature;
}
