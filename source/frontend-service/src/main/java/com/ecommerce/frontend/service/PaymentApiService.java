package com.ecommerce.frontend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class PaymentApiService {

    // Kết quả capture: success + lý do thất bại thật lấy từ payment-service (nếu có), để hiển thị
    // cho khách thay vì thông báo chung chung "An error occurred during payment".
    public static class CaptureResult {
        public final boolean success;
        public final String reason;

        public CaptureResult(boolean success, String reason) {
            this.success = success;
            this.reason = reason;
        }
    }

    @Autowired
    private RestTemplate restTemplate;

    @Value("${PAYMENT_SERVICE_URL:http://localhost:8080/api/payments}")
    private String paymentServiceUrl;

    // Gọi Payment Service tạo PayPal Order → trả về paypalOrderId
    public String createPaypalOrder(double amount, String invoiceId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = Map.of(
                    "amount", amount,
                    "invoiceId", invoiceId);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                    paymentServiceUrl + "/orders",
                    HttpMethod.POST, request, Map.class);

            Map<String, Object> result = response.getBody();
            return result != null ? result.get("id").toString() : null;

        } catch (Exception e) {
            System.err.println("Lỗi tạo PayPal order: " + e.getMessage());
            return null;
        }
    }

    // Capture PayPal Order sau khi user approve → trả về CaptureResult (success + lý do nếu thất bại)
    public CaptureResult capturePaypalOrder(String paypalOrderId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    paymentServiceUrl + "/orders/" + paypalOrderId + "/capture",
                    HttpMethod.POST, request, Map.class);

            Map<String, Object> result = response.getBody();
            boolean ok = result != null && "COMPLETED".equals(result.get("status"));
            return new CaptureResult(ok, null);

        } catch (RestClientResponseException e) {
            // payment-service trả 400 kèm {"error":..., "reason":...} khi capture thất bại - đọc
            // "reason" ra để hiển thị lý do thật (VD "INSUFFICIENT_FUNDS: ...") cho khách.
            String reason = null;
            try {
                Map<String, Object> body = new ObjectMapper().readValue(e.getResponseBodyAsString(), Map.class);
                Object r = body.get("reason");
                reason = r != null ? r.toString() : null;
            } catch (Exception parseEx) {
                System.err.println("Không parse được response lỗi từ payment-service: " + parseEx.getMessage());
            }
            return new CaptureResult(false, reason);

        } catch (Exception e) {
            System.err.println("Lỗi capture PayPal order: " + e.getMessage());
            return new CaptureResult(false, null);
        }
    }

    // Hủy PayPal Order khi user bấm Cancel trên trang PayPal → đồng bộ trạng thái CANCELLED
    public boolean cancelPaypalOrder(String paypalOrderId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            restTemplate.exchange(
                    paymentServiceUrl + "/orders/" + paypalOrderId + "/cancel",
                    HttpMethod.POST, request, Map.class);

            return true;

        } catch (Exception e) {
            System.err.println("Lỗi hủy PayPal order: " + e.getMessage());
            return false;
        }
    }

    // Lấy thông tin thanh toán (bao gồm lý do thất bại nếu có) theo invoiceId - dùng cho trang
    // chi tiết đơn hàng. Trả về null nếu chưa từng có payment nào (VD đơn OUT_OF_STOCK) hoặc lỗi.
    public Map<String, Object> getPaymentByInvoiceId(String invoiceId) {
        try {
            return restTemplate.getForObject(paymentServiceUrl + "/invoice/" + invoiceId, Map.class);
        } catch (Exception e) {
            return null;
        }
    }

    // Admin duyệt hoàn tiền (hoặc hoàn trực tiếp) - gọi PayPal refund thật qua invoiceId
    public boolean refundPaypalOrder(String invoiceId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            restTemplate.exchange(
                    paymentServiceUrl + "/invoice/" + invoiceId + "/refund",
                    HttpMethod.POST, request, Map.class);

            return true;

        } catch (Exception e) {
            System.err.println("Lỗi hoàn tiền: " + e.getMessage());
            return false;
        }
    }
}