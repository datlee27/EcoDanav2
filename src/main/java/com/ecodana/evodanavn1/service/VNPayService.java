package com.ecodana.evodanavn1.service;

import com.ecodana.evodanavn1.config.VNPayConfig;
import com.ecodana.evodanavn1.util.VNPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class VNPayService {

    @Autowired
    private VNPayConfig vnPayConfig;

    /**
     * Tạo URL thanh toán VNPay
     * @param amount Số tiền thanh toán (VND)
     * @param orderInfo Thông tin đơn hàng
     * @param bookingId ID của booking
     * @param request HttpServletRequest để lấy IP
     * @return URL thanh toán
     */
    public String createPaymentUrl(long amount, String orderInfo, String bookingId, HttpServletRequest request) 
            throws UnsupportedEncodingException {
        
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_TxnRef = VNPayUtil.getRandomNumber(8);
        String vnp_IpAddr = VNPayUtil.getIpAddress(request);
        String vnp_TmnCode = vnPayConfig.getTmnCode();
        String orderType = "other";

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount * 100)); // VNPay yêu cầu nhân 100
        vnp_Params.put("vnp_CurrCode", "VND");
        
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        // Thêm bookingId vào OrderInfo để tracking (format: "OrderInfo|BookingId")
        vnp_Params.put("vnp_OrderInfo", orderInfo + "|" + bookingId);
        vnp_Params.put("vnp_OrderType", orderType);

        String locate = "vn";
        vnp_Params.put("vnp_Locale", locate);

        vnp_Params.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15); // Thời gian hết hạn: 15 phút
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                // Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                // Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String queryUrl = query.toString();
        String vnp_SecureHash = VNPayUtil.hmacSHA512(vnPayConfig.getHashSecret(), hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        String paymentUrl = vnPayConfig.getPayUrl() + "?" + queryUrl;
        
        return paymentUrl;
    }

    /**
     * Xác thực chữ ký từ VNPay callback
     * @param vnpParams Tham số từ VNPay
     * @return true nếu hợp lệ
     */
    public boolean verifyPaymentSignature(Map<String, String> vnpParams) {
        String vnp_SecureHash = vnpParams.get("vnp_SecureHash");
        
        // Create a copy to avoid modifying original
        Map<String, String> paramsCopy = new HashMap<>(vnpParams);
        paramsCopy.remove("vnp_SecureHashType");
        paramsCopy.remove("vnp_SecureHash");
        
        String signValue = VNPayUtil.hashAllFields(paramsCopy);
        String calculatedHash = VNPayUtil.hmacSHA512(vnPayConfig.getHashSecret(), signValue);
        
        // Debug logging
        System.out.println("=== VNPay Signature Verification ===");
        System.out.println("Hash Secret: " + vnPayConfig.getHashSecret());
        System.out.println("Sign Value: " + signValue);
        System.out.println("Calculated Hash: " + calculatedHash);
        System.out.println("VNPay Hash: " + vnp_SecureHash);
        System.out.println("Match: " + calculatedHash.equals(vnp_SecureHash));
        
        return calculatedHash.equals(vnp_SecureHash);
    }

    /**
     * Kiểm tra trạng thái thanh toán từ response code
     * @param responseCode Mã phản hồi từ VNPay
     * @return true nếu thanh toán thành công
     */
    public boolean isPaymentSuccess(String responseCode) {
        return "00".equals(responseCode);
    }
}
