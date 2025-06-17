package com.S_Health.GenderHealthCare.dto.request.payment;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MomoNotifiRequest {

    String partnerCode;
    String orderId;
    String requestId;
    Long amount;
    String orderInfo;
    String orderType;
    String transId;
    int resultCode;
    String message;
    String payType;
    Long responseTime;
    String extraData;
    String signature;
}
