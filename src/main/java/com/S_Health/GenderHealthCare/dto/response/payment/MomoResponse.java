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
    String partnerCode;
    String requestId;
    String orderId;
    Long amount;
    Long responseTime;
    String message;
    int resultCode;
    String payUrl;
    String signature;



//    String partnerCode;
//    String orderId;
//    String requestId;
//    Long amount;
//    String orderInfo;
//    String orderType;
//    String transId;
//    int resultCode;
//    String message;
//    String payType;
//    Long responseTime;
//    String extraData;
//    String signature;
}
