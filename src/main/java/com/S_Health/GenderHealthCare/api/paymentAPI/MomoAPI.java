package com.S_Health.GenderHealthCare.api.paymentAPI;

import com.S_Health.GenderHealthCare.dto.request.payment.MomoNotifiRequest;
import com.S_Health.GenderHealthCare.dto.response.payment.MomoResponse;
import com.S_Health.GenderHealthCare.repository.PaymentRepository;
import com.S_Health.GenderHealthCare.repository.TransactionRepository;
import com.S_Health.GenderHealthCare.service.payment.MomoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/payment/momo")
public class MomoAPI {

    @Autowired
    private MomoService momoService;

    @GetMapping("/create")
    public MomoResponse createPayment(@RequestParam long appointmentId) throws Exception, RuntimeException {
        return momoService.createMomoPaymentUrl(appointmentId);
    }


//    @GetMapping("/return")
//    public ResponseEntity<String> handleReturn(@RequestParam Map<String, String> queryParams) {
//        return ResponseEntity.ok("Khách hàng đã thanh toán (returnUrl)");
//    }

    @PostMapping("/return")
    public ResponseEntity<String> handleNotify(@RequestBody MomoNotifiRequest notify) {
        return momoService.handleMomoNotify(notify);
    }
}
