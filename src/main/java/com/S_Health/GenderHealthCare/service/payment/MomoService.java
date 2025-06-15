package com.S_Health.GenderHealthCare.service.payment;

import com.S_Health.GenderHealthCare.config.paymentConfig.MomoConfig;
import com.S_Health.GenderHealthCare.dto.request.payment.MomoRequest;
import com.S_Health.GenderHealthCare.dto.response.payment.MomoResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class MomoService {

    @Value("${momo.partner.code}")
    private String partnerCode;
    @Value("${momo.access.key}")
    private String accessKey;
    @Value("${momo.secret.key}")
    private String secretKey;
    @Value("${momo.endpoint}")
    private String endpoint;
    @Value("${momo.redirect.url}")
    private String returnUrl;
    @Value("${momo.ipn.url}")
    private String ipnUrl;
    @Value("${momo.notify.url}")
    private String notifyUrl;

    private String requestType = "captureWallet";

    public MomoResponse createMomoPaymentUrl() throws Exception {
       String orderId = UUID.randomUUID().toString();
        String requestId = UUID.randomUUID().toString();
        String orderInfo = "Thanh toán đơn hàng: 1" ;
        long amount = 5000000; // Số tiền thanh toán, ví dụ 10.000 VND

        String rawHash = String.format(
                "accessKey=%s&amount=%d&extraData=%s&ipnUrl=%s&orderId=%s&orderInfo=%s&partnerCode=%s&redirectUrl=%s&requestId=%s&requestType=%s",
                accessKey, amount, "", ipnUrl, orderId, orderInfo, partnerCode, returnUrl, requestId, requestType
        );

        String signature = "";
        try {
            signature = MomoConfig.signSHA256(rawHash, secretKey);
        } catch (Exception e) {
            log.error("Error signing Momo request:", e);
            return null;
        }

        if(signature.isBlank()) {
            log.error("Signature is blank, cannot create Momo payment URL");
            return null;
        }


        MomoRequest request = MomoRequest.builder()
                .partnerCode(partnerCode)
                .requestId(requestId)
                .amount(amount)
                .orderId(orderId)
                .orderInfo(orderInfo)
                .redirectUrl(returnUrl)
                .ipnUrl(ipnUrl)
                .requestType(requestType)
                .extraData("")
                .lang("vi")
                .signature(signature)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<MomoRequest> httpRequest = new HttpEntity<>(request, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<MomoResponse> response = restTemplate.postForEntity(endpoint, httpRequest, MomoResponse.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return response.getBody();
        } else {
            log.error("Không nhận được phản hồi hợp lệ từ MoMo");
            return null;
        }


    }
}
