package com.ecommerce.payment.service;

import com.ecommerce.payment.entity.Payment;
import com.ecommerce.payment.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class PayPalService {
    private final PaymentRepository paymentRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${paypal.client-id}")
    private String clientId;

    @Value("${paypal.client-secret}")
    private String clientSecret;

    @Value("${paypal.mode}")
    private String mode;

    // Tỷ giá quy đổi VNĐ -> USD (catalog-service lưu giá bằng VNĐ, PayPal Sandbox yêu cầu USD)
    @Value("${exchange.rate.vnd-usd:25000}")
    private double vndToUsdRate;

    // URL của frontend-service, dùng để PayPal biết đường quay về sau khi thanh toán
    @Value("${frontend.base-url:http://localhost:8888}")
    private String frontendBaseUrl;

    public PayPalService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    private String getBaseUrl() {
        return "sandbox".equals(mode) ? "https://api-m.sandbox.paypal.com" : "https://api-m.paypal.com";
    }

    private String getAccessToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(clientId, clientSecret);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        Map<String, Object> response = restTemplate.postForEntity(
                getBaseUrl() + "/v1/oauth2/token",
                new HttpEntity<>("grant_type=client_credentials", headers),
                Map.class).getBody();
        return (String) response.get("access_token");
    }

    public Payment createPayment(String invoiceId, Double totalVnd, String currency) {
        String usdValue = String.format(Locale.US, "%.2f", totalVnd / vndToUsdRate);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> amount = Map.of(
                "currency_code", currency,
                "value", usdValue);
        Map<String, Object> applicationContext = Map.of(
                "return_url", frontendBaseUrl + "/checkout/success",
                "cancel_url", frontendBaseUrl + "/checkout/cancel",
                "user_action", "PAY_NOW");
        Map<String, Object> body = Map.of(
                "intent", "CAPTURE",
                "purchase_units", List.of(Map.of("amount", amount)),
                "application_context", applicationContext);

        Map<String, Object> response = restTemplate.postForEntity(
                getBaseUrl() + "/v2/checkout/orders",
                new HttpEntity<>(body, headers),
                Map.class).getBody();

        Payment payment = new Payment();
        payment.setInvoiceId(invoiceId);
        payment.setPaypalOrderId((String) response.get("id"));
        payment.setAmount(Double.parseDouble(usdValue));
        payment.setCurrency(currency);
        payment.setStatus("CREATED");
        return paymentRepository.save(payment);
    }

    @SuppressWarnings("unchecked")
    public Payment capturePayment(String orderId) {
        Payment payment = paymentRepository.findByPaypalOrderId(orderId).orElseThrow();
        if ("COMPLETED".equals(payment.getStatus())) return payment;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> response;
        try {
            response = restTemplate.postForEntity(
                    getBaseUrl() + "/v2/checkout/orders/" + orderId + "/capture",
                    new HttpEntity<>("{}", headers),
                    Map.class).getBody();
        } catch (Exception e) {
            // PayPal trả lỗi (VD: 422 ORDER_NOT_APPROVED, thẻ bị từ chối...) - coi là capture thất bại
            payment.setStatus("FAILED");
            payment.setUpdatedAt(new Date());
            return paymentRepository.save(payment);
        }

        if ("COMPLETED".equals(response.get("status"))) {
            List<Map<String, Object>> purchaseUnits = (List<Map<String, Object>>) response.get("purchase_units");
            Map<String, Object> payments = (Map<String, Object>) purchaseUnits.get(0).get("payments");
            List<Map<String, Object>> captures = (List<Map<String, Object>>) payments.get("captures");
            payment.setStatus("COMPLETED");
            payment.setCaptureId((String) captures.get(0).get("id"));
        } else {
            payment.setStatus("FAILED");
        }
        payment.setUpdatedAt(new Date());
        return paymentRepository.save(payment);
    }

    public Payment cancelPayment(String orderId) {
        Payment payment = paymentRepository.findByPaypalOrderId(orderId).orElseThrow();
        payment.setStatus("CANCELLED");
        payment.setUpdatedAt(new Date());
        return paymentRepository.save(payment);
    }

    public Payment refundPayment(String invoiceId) {
        Payment payment = paymentRepository.findByInvoiceId(invoiceId).orElseThrow();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        restTemplate.postForEntity(
                getBaseUrl() + "/v2/payments/captures/" + payment.getCaptureId() + "/refund",
                new HttpEntity<>("{}", headers),
                Map.class);

        payment.setStatus("REFUNDED");
        payment.setUpdatedAt(new Date());
        return paymentRepository.save(payment);
    }
}