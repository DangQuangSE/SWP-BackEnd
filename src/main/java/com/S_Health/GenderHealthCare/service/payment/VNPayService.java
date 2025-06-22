package com.S_Health.GenderHealthCare.service.payment;

import com.S_Health.GenderHealthCare.config.paymentConfig.VNPayConfig;
import com.S_Health.GenderHealthCare.dto.response.payment.VNPayResponse;
import com.S_Health.GenderHealthCare.entity.Appointment;
import com.S_Health.GenderHealthCare.entity.Payment;
import com.S_Health.GenderHealthCare.entity.Transaction;
import com.S_Health.GenderHealthCare.enums.PaymentMethod;
import com.S_Health.GenderHealthCare.enums.PaymentStatus;
import com.S_Health.GenderHealthCare.exception.exceptions.AuthenticationException;
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


    public VNPayResponse createOrder(Long appointmentId){

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AuthenticationException("Cuộc hẹn không tồn tại"));

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
                .URL(payUrl)
                .build();

    }

//    public int orderReturn(HttpServletRequest request){
//        Map fields = new HashMap();
//        for (Enumeration params = request.getParameterNames(); params.hasMoreElements();) {
//            String fieldName = null;
//            String fieldValue = null;
//            try {
//                fieldName = URLEncoder.encode((String) params.nextElement(), StandardCharsets.US_ASCII.toString());
//                fieldValue = URLEncoder.encode(request.getParameter(fieldName), StandardCharsets.US_ASCII.toString());
//            } catch (UnsupportedEncodingException e) {
//                e.printStackTrace();
//            }
//            if ((fieldValue != null) && (fieldValue.length() > 0)) {
//                fields.put(fieldName, fieldValue);
//            }
//        }
//
//        String vnp_SecureHash = request.getParameter("vnp_SecureHash");
//        if (fields.containsKey("vnp_SecureHashType")) {
//            fields.remove("vnp_SecureHashType");
//        }
//        if (fields.containsKey("vnp_SecureHash")) {
//            fields.remove("vnp_SecureHash");
//        }
//        String signValue = VNPayConfig.hashAllFields(fields);
//        if (signValue.equals(vnp_SecureHash)) {
//            if ("00".equals(request.getParameter("vnp_TransactionStatus"))) {
//                return 1;
//            } else {
//                return 0;
//            }
//        } else {
//            return -1;
//        }
//    }

}
