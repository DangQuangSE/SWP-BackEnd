package com.S_Health.GenderHealthCare.service.payment;

import com.S_Health.GenderHealthCare.config.paymentConfig.VNPayConfig;
import com.S_Health.GenderHealthCare.dto.response.payment.VNPayResponse;
import com.S_Health.GenderHealthCare.entity.Appointment;
import com.S_Health.GenderHealthCare.entity.AppointmentDetail;
import com.S_Health.GenderHealthCare.entity.Payment;
import com.S_Health.GenderHealthCare.entity.Transaction;
import com.S_Health.GenderHealthCare.enums.AppointmentStatus;
import com.S_Health.GenderHealthCare.enums.PaymentMethod;
import com.S_Health.GenderHealthCare.enums.PaymentStatus;
import com.S_Health.GenderHealthCare.exception.exceptions.AuthenticationException;
import com.S_Health.GenderHealthCare.repository.AppointmentDetailRepository;
import com.S_Health.GenderHealthCare.repository.AppointmentRepository;
import com.S_Health.GenderHealthCare.repository.PaymentRepository;
import com.S_Health.GenderHealthCare.repository.TransactionRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Getter
public class VNPayService {
    @Autowired
    AppointmentRepository appointmentRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    AppointmentDetailRepository appointmentDetailRepository;


    public VNPayResponse createOrder(Long appointmentId){

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AuthenticationException("Cuộc hẹn không tồn tại"));

        AppointmentDetail appointmentDetail = appointmentDetailRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new AuthenticationException("Cuộc hẹn không có chi tiết"));

        Optional<Payment> paid = paymentRepository.findByAppointmentIdAndStatus(appointmentId, PaymentStatus.SUCCESS);
        if (paid.isPresent()) {
            throw new AuthenticationException("Cuộc hẹn đã được thanh toán.");
        }

        // Tìm giao dịch thanh toán thất bại
        Optional<Payment> failed = paymentRepository.findByAppointmentIdAndStatus(appointmentId, PaymentStatus.FAILED);
        if (failed.isPresent()) {
            throw new AuthenticationException("Cuộc hẹn đã huỷ.");
        }

        BigDecimal price = BigDecimal.valueOf(appointment.getService().getPrice());

        long amount = price.longValue();// Số tiền thanh toán, ví dụ 10.000 VND
        String orderInfo = "Thanh toan don hang: " + appointment.getService().getName();

        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_TxnRef =  UUID.randomUUID().toString().replace("-", "").substring(0, 20);
        String vnp_IpAddr = "127.0.0.1";
        String vnp_TmnCode = VNPayConfig.vnp_TmnCode;
        String orderType = "order-type";
        String vnp_CreateDate = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        cld.add(Calendar.MINUTE, 5);
        String vnp_ExpireDate = formatter.format(cld.getTime());

        Map<String, String> params = new HashMap<>();
//        params.put("vnp_BankCode", "NCB");
        params.put("vnp_Version", vnp_Version);
        params.put("vnp_Command", vnp_Command);
        params.put("vnp_TmnCode", vnp_TmnCode);
        params.put("vnp_Amount", String.valueOf(amount * 100));
        params.put("vnp_CreateDate", vnp_CreateDate);
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_IpAddr", vnp_IpAddr);
        params.put("vnp_Locale", "vn");
        params.put("vnp_OrderInfo", orderInfo);
        params.put("vnp_OrderType", "other");
        params.put("vnp_ReturnUrl", VNPayConfig.vnp_ReturnUrl);
        params.put("vnp_ExpireDate", vnp_ExpireDate);
        params.put("vnp_TxnRef", vnp_TxnRef);

        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        for (String field : fieldNames) {
            String value = params.get(field);
            hashData.append(field).append('=').append(URLEncoder.encode(value, StandardCharsets.US_ASCII)).append('&');
            query.append(field).append('=').append(URLEncoder.encode(value, StandardCharsets.US_ASCII)).append('&');
        }

        hashData.setLength(hashData.length() - 1);
        query.setLength(query.length() - 1);

        String secureHash = VNPayConfig.hmacSHA512(VNPayConfig.vnp_HashSecret, hashData.toString());
        query.append("&vnp_SecureHash=").append(secureHash);

        String payUrl = VNPayConfig.vnp_PayUrl + "?" + query;

        Payment payment = Payment.builder()
                .amount(BigDecimal.valueOf(amount))
                .status(PaymentStatus.PENDING)
                .method(PaymentMethod.VN_PAY)
                .appointment(appointment)
                .paidBy(appointment.getCustomer())
                .build();
        payment = paymentRepository.save(payment);

        Transaction transaction = Transaction.builder()
                .orderId(vnp_TxnRef)
                .requestId(vnp_TxnRef) // VNPay không có requestId
//                .payUrl(payUrl)
                .payment(payment)
                .build();
        transactionRepository.save(transaction);

        return VNPayResponse.builder()
                .amount(amount)
                .URL(payUrl)
                .build();

    }

    public VNPayResponse createOrderOff(Long appointmentId){

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AuthenticationException("Cuộc hẹn không tồn tại"));

        AppointmentDetail appointmentDetail = appointmentDetailRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new AuthenticationException("Cuộc hẹn không có chi tiết"));

        Optional<Payment> paid = paymentRepository.findByAppointmentIdAndStatus(appointmentId, PaymentStatus.SUCCESS);
        if (paid.isPresent()) {
            throw new AuthenticationException("Cuộc hẹn đã được thanh toán.");
        }

        // Tìm giao dịch thanh toán thất bại
        Optional<Payment> failed = paymentRepository.findByAppointmentIdAndStatus(appointmentId, PaymentStatus.FAILED);
        if (failed.isPresent()) {
            throw new AuthenticationException("Cuộc hẹn đã huỷ.");
        }

        BigDecimal price = BigDecimal.valueOf(appointment.getService().getPrice());

        long amount = price.longValue();// Số tiền thanh toán, ví dụ 10.000 VND
        String orderInfo = "Thanh toan don hang: " + appointment.getService().getName();

        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_TxnRef =  UUID.randomUUID().toString().replace("-", "").substring(0, 20);
        String vnp_IpAddr = "127.0.0.1";
        String vnp_TmnCode = VNPayConfig.vnp_TmnCode;
        String orderType = "order-type";
        String vnp_CreateDate = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        cld.add(Calendar.MINUTE, 1);
        String vnp_ExpireDate = formatter.format(cld.getTime());

        Map<String, String> params = new HashMap<>();
//        params.put("vnp_BankCode", "NCB");
        params.put("vnp_Version", vnp_Version);
        params.put("vnp_Command", vnp_Command);
        params.put("vnp_TmnCode", vnp_TmnCode);
        params.put("vnp_Amount", String.valueOf(amount * 100 * 0.2));
        params.put("vnp_CreateDate", vnp_CreateDate);
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_IpAddr", vnp_IpAddr);
        params.put("vnp_Locale", "vn");
        params.put("vnp_OrderInfo", orderInfo);
        params.put("vnp_OrderType", "other");
        params.put("vnp_ReturnUrl", VNPayConfig.vnp_ReturnUrl);
        params.put("vnp_ExpireDate", vnp_ExpireDate);
        params.put("vnp_TxnRef", vnp_TxnRef);

        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        for (String field : fieldNames) {
            String value = params.get(field);
            hashData.append(field).append('=').append(URLEncoder.encode(value, StandardCharsets.US_ASCII)).append('&');
            query.append(field).append('=').append(URLEncoder.encode(value, StandardCharsets.US_ASCII)).append('&');
        }

        hashData.setLength(hashData.length() - 1);
        query.setLength(query.length() - 1);

        String secureHash = VNPayConfig.hmacSHA512(VNPayConfig.vnp_HashSecret, hashData.toString());
        query.append("&vnp_SecureHash=").append(secureHash);

        String payUrl = VNPayConfig.vnp_PayUrl + "?" + query;

        Payment payment = Payment.builder()
                .amount(BigDecimal.valueOf(amount))
                .status(PaymentStatus.PENDING)
                .method(PaymentMethod.PAY_OFF)
                .appointment(appointment)
                .paidBy(appointment.getCustomer())
                .build();
        payment = paymentRepository.save(payment);

        Transaction transaction = Transaction.builder()
                .orderId(vnp_TxnRef)
                .requestId(vnp_TxnRef) // VNPay không có requestId
//                .payUrl(payUrl)
                .payment(payment)
                .build();
        transactionRepository.save(transaction);

        return VNPayResponse.builder()
                .amount(amount)
                .URL(payUrl)
                .build();

    }

    public VNPayResponse processReturn(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((key, values) -> {
            if (!key.equals("vnp_SecureHash") && !key.equals("vnp_SecureHashType")) {
                params.put(key, values[0]);
            }
        });

        String receivedHash = request.getParameter("vnp_SecureHash");

        List<String> sortedKeys = new ArrayList<>(params.keySet());
        Collections.sort(sortedKeys);

        StringBuilder hashData = new StringBuilder();
        for (int i = 0; i < sortedKeys.size(); i++) {
            String key = sortedKeys.get(i);
            String value = params.get(key);
            hashData.append(key).append('=').append(URLEncoder.encode(value, StandardCharsets.US_ASCII));
            if (i < sortedKeys.size() - 1) hashData.append('&');
        }

        String calculatedHash = VNPayConfig.hmacSHA512(VNPayConfig.vnp_HashSecret, hashData.toString());

        if (!calculatedHash.equals(receivedHash)) {
            throw new AuthenticationException("Chữ ký không hợp lệ.");
        }

        String vnp_ResponseCode = params.get("vnp_ResponseCode");
        String vnp_TxnRef = params.get("vnp_TxnRef");
        String vnp_TransactionNo = params.get("vnp_TransactionNo");
        String vnp_Message = params.get("vnp_TransactionStatus"); // hoặc vnp_ResponseCode nếu không có message rõ
        String vnp_PayDate = params.get("vnp_PayDate"); // yyyyMMddHHmmss

        Transaction transaction = transactionRepository.findByOrderId(vnp_TxnRef)
                .orElseThrow(() -> new AuthenticationException("Không tìm thấy giao dịch."));

        Payment payment = transaction.getPayment();

        LocalDateTime payTime = LocalDateTime.now();
        if (vnp_PayDate != null && vnp_PayDate.matches("\\d{14}")) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
                payTime = LocalDateTime.parse(vnp_PayDate, formatter);
            } catch (Exception ignored) {}
        }

        if ("00".equals(vnp_ResponseCode)) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setPaidAt(payTime);
            Appointment appointment = payment.getAppointment();
            appointment.setStatus(AppointmentStatus.CONFIRMED);
            appointmentRepository.save(appointment);

            AppointmentDetail appointmentDetail = appointmentDetailRepository.findByAppointmentId(appointment.getId())
                    .orElseThrow(() -> new AuthenticationException("Không tìm thấy chi tiết cuộc hẹn."));
            appointmentDetail.setStatus(AppointmentStatus.CONFIRMED);
            appointmentDetailRepository.save(appointmentDetail);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setPaidAt(LocalDateTime.now());
            Appointment appointment = payment.getAppointment();
            appointment.setStatus(AppointmentStatus.CANCELED);
            appointmentRepository.save(appointment);

            AppointmentDetail appointmentDetail = appointmentDetailRepository.findByAppointmentId(appointment.getId())
                    .orElseThrow(() -> new AuthenticationException("Không tìm thấy chi tiết cuộc hẹn."));
            appointmentDetail.setStatus(AppointmentStatus.CANCELED);
            appointmentDetailRepository.save(appointmentDetail);
        }

        paymentRepository.save(payment);

        transaction.setTransactionCode(vnp_TransactionNo);
        transaction.setResultCode(Integer.parseInt(vnp_ResponseCode));
        transaction.setResponseMessage(vnp_Message);
        transaction.setResponseTime(LocalDateTime.now());
        transactionRepository.save(transaction);

        return VNPayResponse.builder()
                .amount(payment.getAmount().longValue())
                .URL(null)
                .message(payment.getStatus() == PaymentStatus.SUCCESS ? "Thanh toán thành công" : "Thanh toán thất bại")
                .build();
    }



}
