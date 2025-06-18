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


//    @GetMapping("/create-url")
//    public ResponseEntity<?> createVNPayUrl() {
//        try {
////            String clientIp = VNPayUtils.getIpAddress(request);
//            String paymentUrl = vnPayService.createPaymentUrl();
//            return ResponseEntity.ok().body(paymentUrl);
//        } catch (Exception e) {
//            return ResponseEntity.internalServerError().body(e.getMessage());
//        }
//    }

//    @GetMapping("/create-url")
//    public String getPay() throws UnsupportedEncodingException {
//        String vnp_Version = "2.1.0";
//        String vnp_Command = "pay";
//        String orderType = "other";
//        long amount = 10000*100;
//        String bankCode = "NCB";
//
//        String vnp_TxnRef = VNPayConfig.getRandomNumber(8);
//        String vnp_IpAddr = "127.0.0.1";
//
//        String vnp_TmnCode = VNPayConfig.vnp_TmnCode;
//
//        Map<String, String> vnp_Params = new HashMap<>();
//        vnp_Params.put("vnp_Version", vnp_Version);
//        vnp_Params.put("vnp_Command", vnp_Command);
//        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
//        vnp_Params.put("vnp_Amount", String.valueOf(amount));
//        vnp_Params.put("vnp_CurrCode", "VND");
//        vnp_Params.put("vnp_BankCode", bankCode);
//        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
//        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang:" + vnp_TxnRef);
//        vnp_Params.put("vnp_OrderType", orderType);
//
//        vnp_Params.put("vnp_Locale", "vn");
//        vnp_Params.put("vnp_ReturnUrl", VNPayConfig.vnp_ReturnUrl);
//        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);
//
//        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
//        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
//        String vnp_CreateDate = formatter.format(cld.getTime());
//        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
//
//        cld.add(Calendar.MINUTE, 15);
//        String vnp_ExpireDate = formatter.format(cld.getTime());
//        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

//        List fieldNames = new ArrayList(vnp_Params.keySet());
//        Collections.sort(fieldNames);
//        StringBuilder hashData = new StringBuilder();
//        StringBuilder query = new StringBuilder();
//        Iterator itr = fieldNames.iterator();
//        while (itr.hasNext()) {
//            String fieldName = (String) itr.next();
//            String fieldValue = (String) vnp_Params.get(fieldName);
//            if ((fieldValue != null) && (fieldValue.length() > 0)) {
//                //Build hash data
//                hashData.append(fieldName);
//                hashData.append('=');
//                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
//                //Build query
//                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
//                query.append('=');
//                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
//                if (itr.hasNext()) {
//                    query.append('&');
//                    hashData.append('&');
//                }
//            }
//        }

//        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
//        Collections.sort(fieldNames);
//
//        StringBuilder hashData = new StringBuilder();
//        StringBuilder query = new StringBuilder();
//
//        for (int i = 0; i < fieldNames.size(); i++) {
//            String fieldName = fieldNames.get(i);
//            String fieldValue = vnp_Params.get(fieldName);
//            if (fieldValue != null && !fieldValue.isEmpty()) {
//                hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
//                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII))
//                        .append('=')
//                        .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
//                if (i < fieldNames.size() - 1) {
//                    hashData.append('&');
//                    query.append('&');
//                }
//            }
//        }
//        String queryUrl = query.toString();
//        String vnp_SecureHash = VNPayConfig.hmacSHA512(VNPayConfig.secretKey, hashData.toString());
//        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
//        String paymentUrl = VNPayConfig.vnp_PayUrl + "?" + queryUrl;
//
//        System.out.println("HashData = " + hashData);
//        System.out.println("SecureHash = " + vnp_SecureHash);
//        return paymentUrl;
//    }

    @GetMapping("")
    public String home(){
        return "index";
    }

    @PostMapping("/submitOrder")
    public String submidOrder(@RequestParam("amount") int orderTotal,
                              @RequestParam("orderInfo") String orderInfo,
                              HttpServletRequest request){
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        String vnpayUrl = vnPayService.createOrder(orderTotal, orderInfo, baseUrl);
        return "redirect:" + vnpayUrl;
    }

    @GetMapping("/vnpay-payment")
    public String GetMapping(HttpServletRequest request, Model model){
        int paymentStatus =vnPayService.orderReturn(request);

        String orderInfo = request.getParameter("vnp_OrderInfo");
        String paymentTime = request.getParameter("vnp_PayDate");
        String transactionId = request.getParameter("vnp_TransactionNo");
        String totalPrice = request.getParameter("vnp_Amount");

        model.addAttribute("orderId", orderInfo);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("paymentTime", paymentTime);
        model.addAttribute("transactionId", transactionId);

        return paymentStatus == 1 ? "ordersuccess" : "orderfail";
    }

}
