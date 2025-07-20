package com.S_Health.GenderHealthCare.service.payment;

import com.S_Health.GenderHealthCare.config.paymentConfig.MomoConfig;
import com.S_Health.GenderHealthCare.dto.request.payment.MomoNotifiRequest;
import com.S_Health.GenderHealthCare.dto.request.payment.MomoRequest;
import com.S_Health.GenderHealthCare.dto.response.payment.MomoResponse;
import com.S_Health.GenderHealthCare.entity.Appointment;
import com.S_Health.GenderHealthCare.entity.Payment;
import com.S_Health.GenderHealthCare.entity.Transaction;
import com.S_Health.GenderHealthCare.enums.AppointmentStatus;
import com.S_Health.GenderHealthCare.enums.PaymentMethod;
import com.S_Health.GenderHealthCare.enums.PaymentStatus;
import com.S_Health.GenderHealthCare.exception.exceptions.AppException;
import com.S_Health.GenderHealthCare.repository.AppointmentRepository;
import com.S_Health.GenderHealthCare.repository.PaymentRepository;
import com.S_Health.GenderHealthCare.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


@Service
@RequiredArgsConstructor
@Slf4j
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

    @Autowired
    private AppointmentRepository appointmentRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private TransactionRepository transactionRepository;

    private final ScheduledExecutorService scheduledExecutorService;
    private final String requestType = "captureWallet";

    public MomoResponse createMomoPaymentUrl(Long appointmentId) throws Exception {
        String orderId = UUID.randomUUID().toString();
        String requestId = UUID.randomUUID().toString();

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppException("Cuộc hẹn không tồn tại"));

        Optional<Payment> paid = paymentRepository.findByAppointmentIdAndStatus(appointmentId, PaymentStatus.SUCCESS);
        if (paid.isPresent()) {
            throw new AppException("Cuộc hẹn đã được thanh toán.");
        }

        // Tìm giao dịch thanh toán thất bại
        Optional<Payment> failed = paymentRepository.findByAppointmentIdAndStatus(appointmentId, PaymentStatus.FAILED);
        if (failed.isPresent()) {
            throw new AppException("Cuộc hẹn đã huỷ.");
        }

//        BigDecimal price = BigDecimal.valueOf(appointment.getService().getPrice());
        BigDecimal price = BigDecimal.valueOf(appointment.getService().getPrice());

        long amount = price.longValue();// Số tiền thanh toán, ví dụ 10.000 VND
        String orderInfo = "Thanh toán đơn hàng: " + appointment.getService().getName();

        String rawHash = String.format(
                "accessKey=%s&amount=%d&extraData=%s&ipnUrl=%s&orderId=%s&orderInfo=%s&partnerCode=%s&redirectUrl=%s&requestId=%s&requestType=%s",
                accessKey, amount, "", ipnUrl, orderId, orderInfo, partnerCode, returnUrl, requestId, requestType);

        String signature = MomoConfig.signSHA256(rawHash, secretKey);

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
        HttpEntity<MomoRequest> entity = new HttpEntity<>(request, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<MomoResponse> response = restTemplate.postForEntity(endpoint, entity, MomoResponse.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            Payment payment = Payment.builder()
                    .amount(BigDecimal.valueOf(amount))
                    .status(PaymentStatus.PENDING)
                    .method(PaymentMethod.MOMO)
                    .appointment(appointment)
                    .paidBy(appointment.getCustomer())
                    .build();
            paymentRepository.save(payment);

            Transaction transaction = Transaction.builder()
                    .orderId(orderId)
                    .requestId(requestId)
//                    .payUrl(response.getBody().getPayUrl())
                    .payment(payment)
                    .build();
            transactionRepository.save(transaction);
            schedulePaymentTimeout(payment.getId());

            return response.getBody();
        }

        throw new RuntimeException("Tạo thanh toán thất bại.");
    }


    public ResponseEntity<String> handleMomoNotify(MomoNotifiRequest notify) {
        try {
            String rawHash = String.format(
                    "accessKey=%s&amount=%d&extraData=%s&message=%s&orderId=%s&orderInfo=%s&orderType=%s&partnerCode=%s&payType=%s&requestId=%s&responseTime=%s&resultCode=%d&transId=%s",
                    accessKey,
                    notify.getAmount(),
                    notify.getExtraData(),
                    notify.getMessage(),
                    notify.getOrderId(),
                    notify.getOrderInfo(),
                    notify.getOrderType(),
                    notify.getPartnerCode(),
                    notify.getPayType(),
                    notify.getRequestId(),
                    notify.getResponseTime(),
                    notify.getResultCode(),
                    notify.getTransId()
            );

            String expectedSignature = MomoConfig.signSHA256(rawHash, secretKey);
            if (!expectedSignature.equals(notify.getSignature())) {
                return ResponseEntity.badRequest().body("Invalid signature");
            }

            Transaction transaction = transactionRepository.findByOrderId(notify.getOrderId())
                    .orElseThrow(() -> new IllegalStateException("Không tìm thấy giao dịch"));

            Payment payment1 = transaction.getPayment();

            if (notify.getResultCode() != 0) {
                Payment payment = transaction.getPayment();
                payment.setStatus(PaymentStatus.FAILED);
                payment.setPaidAt(LocalDateTime.now());
                paymentRepository.save(payment);

                Appointment appointment = payment.getAppointment();
                appointment.setStatus(AppointmentStatus.CANCELED);
                appointmentRepository.save(appointment);

                return ResponseEntity.ok("Giao dịch thất bại");
            }

            Payment payment = transaction.getPayment();
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setPaidAt(LocalDateTime.now());
            paymentRepository.save(payment);

            transaction.setTransactionCode(notify.getTransId());
            transaction.setResultCode(notify.getResultCode());
            transaction.setResponseMessage(notify.getMessage());
            transaction.setResponseTime(LocalDateTime.now());
            transactionRepository.save(transaction);

            Appointment appointment = payment.getAppointment();
            appointment.setStatus(AppointmentStatus.CONFIRMED);
            appointmentRepository.save(appointment);

            return ResponseEntity.ok("Cập nhật thanh toán thành công");

        } catch (Exception e) {
            log.error("Lỗi xử lý IPN:", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi xử lí");
        }
    }

    private void schedulePaymentTimeout(Long paymentId) {
        scheduledExecutorService.schedule(() -> {
            Optional<Payment> optionalPayment = paymentRepository.findById(paymentId);
            if (optionalPayment.isPresent()) {
                Payment payment = optionalPayment.get();
                if (payment.getStatus() == PaymentStatus.PENDING) {
                    payment.setStatus(PaymentStatus.FAILED);
                    payment.setPaidAt(LocalDateTime.now());

                    Appointment appointment = payment.getAppointment();
                    appointment.setStatus(AppointmentStatus.CANCELED);

                    appointmentRepository.save(appointment);
                    paymentRepository.save(payment);
                    System.out.println("Payment " + paymentId + " bị huỷ do timeout sau 5 phút.");
                }
            }
        }, 5, TimeUnit.MINUTES);
    }
}
