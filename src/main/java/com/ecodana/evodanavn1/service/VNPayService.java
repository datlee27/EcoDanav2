package com.ecodana.evodanavn1.service;

import com.ecodana.evodanavn1.config.VNPayConfig;
import com.ecodana.evodanavn1.model.Booking;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class VNPayService {

    @Autowired
    private VNPayConfig vnPayConfig;

    /**
     * Create payment URL for VNPay
     */
    public String createPaymentUrl(Booking booking, HttpServletRequest request) {
        try {
            // Get client IP
            String vnpIpAddr = VNPayConfig.getIpAddress(request);
            
            // Generate transaction reference
            String vnpTxnRef = booking.getBookingCode();
            
            // Amount in VND (multiply by 100 to remove decimal)
            long amount = booking.getTotalAmount().multiply(new BigDecimal(100)).longValue();
            
            // Create date format
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
            String vnpCreateDate = formatter.format(calendar.getTime());
            
            // Set expiration time (15 minutes)
            calendar.add(Calendar.MINUTE, 15);
            String vnpExpireDate = formatter.format(calendar.getTime());
            
            // Build parameters
            Map<String, String> vnpParams = new HashMap<>();
            vnpParams.put("vnp_Version", vnPayConfig.getVnpVersion());
            vnpParams.put("vnp_Command", vnPayConfig.getVnpCommand());
            vnpParams.put("vnp_TmnCode", vnPayConfig.getVnpTmnCode());
            vnpParams.put("vnp_Amount", String.valueOf(amount));
            vnpParams.put("vnp_CurrCode", vnPayConfig.getVnpCurrencyCode());
            vnpParams.put("vnp_TxnRef", vnpTxnRef);
            vnpParams.put("vnp_OrderInfo", "Thanh toan dat xe " + booking.getBookingCode());
            vnpParams.put("vnp_OrderType", "other");
            vnpParams.put("vnp_Locale", "vn");
            vnpParams.put("vnp_ReturnUrl", vnPayConfig.getVnpReturnUrl());
            vnpParams.put("vnp_IpAddr", vnpIpAddr);
            vnpParams.put("vnp_CreateDate", vnpCreateDate);
            vnpParams.put("vnp_ExpireDate", vnpExpireDate);
            
            // Build query string
            String queryUrl = VNPayConfig.buildQueryString(vnpParams);
            
            // Build hash data
            String hashData = VNPayConfig.buildHashData(vnpParams);
            
            // Generate secure hash
            String vnpSecureHash = VNPayConfig.hmacSHA512(vnPayConfig.getVnpHashSecret(), hashData);
            
            // Build final payment URL
            queryUrl += "&vnp_SecureHash=" + vnpSecureHash;
            String paymentUrl = vnPayConfig.getVnpPayUrl() + "?" + queryUrl;
            
            return paymentUrl;
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Validate VNPay callback signature
     */
    public boolean validateSignature(Map<String, String> params, String secureHash) {
        try {
            // Remove secure hash from params
            Map<String, String> fields = new HashMap<>(params);
            fields.remove("vnp_SecureHash");
            fields.remove("vnp_SecureHashType");
            
            // Build hash data
            String hashData = VNPayConfig.buildHashData(fields);
            
            // Generate hash
            String calculatedHash = VNPayConfig.hmacSHA512(vnPayConfig.getVnpHashSecret(), hashData);
            
            return calculatedHash.equals(secureHash);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Extract parameters from request
     */
    public Map<String, String> getParamsFromRequest(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        Enumeration<String> parameterNames = request.getParameterNames();
        
        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            String paramValue = request.getParameter(paramName);
            if (paramValue != null && !paramValue.isEmpty()) {
                params.put(paramName, paramValue);
            }
        }
        
        return params;
    }

    /**
     * Get transaction status description
     */
    public String getTransactionStatusDescription(String responseCode) {
        switch (responseCode) {
            case "00":
                return "Giao dịch thành công";
            case "07":
                return "Trừ tiền thành công. Giao dịch bị nghi ngờ (liên quan tới lừa đảo, giao dịch bất thường)";
            case "09":
                return "Giao dịch không thành công do: Thẻ/Tài khoản của khách hàng chưa đăng ký dịch vụ InternetBanking tại ngân hàng";
            case "10":
                return "Giao dịch không thành công do: Khách hàng xác thực thông tin thẻ/tài khoản không đúng quá 3 lần";
            case "11":
                return "Giao dịch không thành công do: Đã hết hạn chờ thanh toán. Xin quý khách vui lòng thực hiện lại giao dịch";
            case "12":
                return "Giao dịch không thành công do: Thẻ/Tài khoản của khách hàng bị khóa";
            case "13":
                return "Giao dịch không thành công do Quý khách nhập sai mật khẩu xác thực giao dịch (OTP)";
            case "24":
                return "Giao dịch không thành công do: Khách hàng hủy giao dịch";
            case "51":
                return "Giao dịch không thành công do: Tài khoản của quý khách không đủ số dư để thực hiện giao dịch";
            case "65":
                return "Giao dịch không thành công do: Tài khoản của Quý khách đã vượt quá hạn mức giao dịch trong ngày";
            case "75":
                return "Ngân hàng thanh toán đang bảo trì";
            case "79":
                return "Giao dịch không thành công do: KH nhập sai mật khẩu thanh toán quá số lần quy định";
            default:
                return "Giao dịch không thành công";
        }
    }
}
