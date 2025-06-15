package com.S_Health.GenderHealthCare.api.paymentAPI;

import com.S_Health.GenderHealthCare.dto.response.payment.MomoResponse;
import com.S_Health.GenderHealthCare.service.payment.MomoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payment/momo")
public class MomoAPI {

    @Autowired
    private MomoService momoService;

    @GetMapping("/create")
    public MomoResponse createPayment() throws Exception {
        return momoService.createMomoPaymentUrl();
    }

    @GetMapping("/return")
    public ResponseEntity<String> handleReturn(@RequestParam Map<String, String> queryParams) {
        return ResponseEntity.ok("Khách hàng đã thanh toán (returnUrl)");
    }

    @PostMapping("/notify")
    public ResponseEntity<String> handleNotify(@RequestBody Map<String, String> body) {
        System.out.println("IPN từ MoMo gửi về: " + body);
        // TODO: kiểm tra chữ ký và cập nhật trạng thái thanh toán
        return ResponseEntity.ok("IPN OK");
    }
}
