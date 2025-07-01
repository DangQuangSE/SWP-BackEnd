package com.S_Health.GenderHealthCare.api.paymentAPI;

import com.S_Health.GenderHealthCare.config.paymentConfig.VNPayConfig;
import com.S_Health.GenderHealthCare.dto.response.payment.VNPayResponse;
import com.S_Health.GenderHealthCare.exception.exceptions.AuthenticationException;
import com.S_Health.GenderHealthCare.service.payment.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/api/payment/vnpay")
public class VNPayAPI {

    @Autowired
    private VNPayService vnPayService;

    @GetMapping("/create")
    public ResponseEntity<VNPayResponse> createPayment(@RequestParam long appointmentId,
                                                       HttpServletRequest request) throws Exception {
        System.out.println(request.getRemoteAddr());
        VNPayResponse response = vnPayService.createOrder(appointmentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/createOff")
    public ResponseEntity<VNPayResponse> createPaymentOff(@RequestParam long appointmentId,
                                                       HttpServletRequest request) throws Exception {
        System.out.println(request.getRemoteAddr());
        VNPayResponse response = vnPayService.createOrderOff(appointmentId);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/vnpay-return")
    public ResponseEntity<VNPayResponse> handleReturn(HttpServletRequest request) {
        try {
            VNPayResponse response = vnPayService.processReturn(request);
            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            return ResponseEntity.badRequest()
                    .body(VNPayResponse.builder().message(e.getMessage()).build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(VNPayResponse.builder().message("Lỗi xử lý thanh toán").build());
        }
    }


}
