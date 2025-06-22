package com.S_Health.GenderHealthCare.api.paymentAPI;

import com.S_Health.GenderHealthCare.config.paymentConfig.VNPayConfig;
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

    @PostMapping("/vn-pay")
    public ResponseEntity<String> createPayment(@RequestParam long appointmentId,
                                                HttpServletRequest request) throws Exception {
        System.out.println(request.getRemoteAddr());
        String paymentUrl = String.valueOf(vnPayService.createOrder(appointmentId));
        return ResponseEntity.ok(paymentUrl);
    }


//    @GetMapping("/vnpay-payment")
//    public String GetMapping(HttpServletRequest request, Model model){
//        int paymentStatus =vnPayService.orderReturn(request);
//
//        String orderInfo = request.getParameter("vnp_OrderInfo");
//        String paymentTime = request.getParameter("vnp_PayDate");
//        String transactionId = request.getParameter("vnp_TransactionNo");
//        String totalPrice = request.getParameter("vnp_Amount");
//
//        model.addAttribute("orderId", orderInfo);
//        model.addAttribute("totalPrice", totalPrice);
//        model.addAttribute("paymentTime", paymentTime);
//        model.addAttribute("transactionId", transactionId);
//
//        return paymentStatus == 1 ? "ordersuccess" : "orderfail";
//    }

}
