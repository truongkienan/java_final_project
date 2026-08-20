package com.ecommerce.frontend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class PaymentApiService {

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

    // Capture PayPal Order sau khi user approve → trả về true nếu COMPLETED
    public boolean capturePaypalOrder(String paypalOrderId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    paymentServiceUrl + "/orders/" + paypalOrderId + "/capture",
                    HttpMethod.POST, request, Map.class);

            Map<String, Object> result = response.getBody();
            return result != null && "COMPLETED".equals(result.get("status"));

        } catch (Exception e) {
            System.err.println("Lỗi capture PayPal order: " + e.getMessage());
            return false;
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