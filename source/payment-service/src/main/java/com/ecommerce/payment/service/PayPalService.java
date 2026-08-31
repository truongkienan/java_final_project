package com.ecommerce.payment.service;

import com.ecommerce.payment.entity.Payment;
import com.ecommerce.payment.repository.PaymentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class PayPalService {
    private final PaymentRepository paymentRepository;
    // BufferingClientHttpRequestFactory bat buoc phai co: RestTemplate mac dinh (Simple...) khong
    // luu lai response body cho cac ma loi HTTP, khien getResponseBodyAsString() tra ve chuoi rong
    // khi bat exception ben duoi - day chinh la ly do failure_reason luon bi NULL truoc do.
    private final RestTemplate restTemplate = new RestTemplate(
            new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));

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

    // Negative testing: khi khác rỗng, gắn header PayPal-Mock-Response vào request capture để
    // PayPal Sandbox trả lỗi giả lập (VD "INSUFFICIENT_FUNDS") thay vì capture thật. Chỉ hoạt
    // động khi tài khoản Business Sandbox đã bật Negative Testing trên developer.paypal.com.
    @Value("${paypal.mock-capture-error:}")
    private String mockCaptureError;

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
        if (!mockCaptureError.isBlank()) {
            headers.set("PayPal-Mock-Response", "{\"mock_application_codes\": \"" + mockCaptureError + "\"}");
        }

        Map<String, Object> response;
        try {
            response = restTemplate.postForEntity(
                    getBaseUrl() + "/v2/checkout/orders/" + orderId + "/capture",
                    new HttpEntity<>("{}", headers),
                    Map.class).getBody();
        } catch (RestClientResponseException e) {
            // PayPal trả lỗi HTTP thật (VD 422 kèm details[0].issue="INSUFFICIENT_FUNDS") - đọc
            // thẳng response body của lỗi để lấy lý do cụ thể, thay vì chỉ biết chung chung "thất bại".
            String rawBody = e.getResponseBodyAsString();
            System.out.println("[PAYPAL-CAPTURE-ERROR] exceptionClass=" + e.getClass().getName()
                    + " httpStatus=" + e.getStatusCode() + " bodyLength=" + (rawBody == null ? -1 : rawBody.length())
                    + " rawBody=" + rawBody);
            payment.setStatus("FAILED");
            // 403 rong (khong co body) khi dang goi voi header PayPal-Mock-Response = tai khoan
            // Business Sandbox gan voi client-id nay CHUA bat "Negative Testing" - PayPal tu choi
            // thang request tu buoc dau, khong phai loi nghiep vu (VD INSUFFICIENT_FUNDS) thuc su.
            if (e.getStatusCode().value() == 403 && !mockCaptureError.isBlank() && (rawBody == null || rawBody.isBlank())) {
                payment.setFailureReason("PayPal tu choi yeu cau mock (403 Forbidden, rong) - tai khoan "
                        + "Business Sandbox gan voi client-id hien tai chua bat Negative Testing tren "
                        + "developer.paypal.com > Sandbox > Accounts.");
            } else {
                payment.setFailureReason(extractFailureReason(rawBody));
            }
            payment.setUpdatedAt(new Date());
            return paymentRepository.save(payment);
        } catch (Exception e) {
            // Lỗi không phải từ response HTTP của PayPal (VD mất kết nối) - không có gì để parse.
            payment.setStatus("FAILED");
            payment.setFailureReason(e.getMessage());
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
            // Không phải lúc nào PayPal cũng trả lỗi HTTP (nhánh catch phía trên) - đôi khi HTTP vẫn
            // 200/201 nhưng chính order/capture bên trong lại ở trạng thái DECLINED. In nguyên response
            // ra log để lần sau biết chính xác cấu trúc thật (đừng đoán), đồng thời cố trích lý do.
            System.err.println("--> [PayPal] Capture không COMPLETED, response đầy đủ: " + response);
            payment.setStatus("FAILED");
            payment.setFailureReason(extractFailureReasonFromResponse(response));
        }
        payment.setUpdatedAt(new Date());
        return paymentRepository.save(payment);
    }

    // Trích lý do thất bại khi PayPal trả HTTP thành công nhưng nội dung order/capture báo DECLINED
    // (khác với extractFailureReason ở trên, dùng cho trường hợp PayPal trả lỗi HTTP thật sự).
    @SuppressWarnings("unchecked")
    private String extractFailureReasonFromResponse(Map<String, Object> response) {
        try {
            List<Map<String, Object>> purchaseUnits = (List<Map<String, Object>>) response.get("purchase_units");
            if (purchaseUnits != null && !purchaseUnits.isEmpty()) {
                Map<String, Object> payments = (Map<String, Object>) purchaseUnits.get(0).get("payments");
                List<Map<String, Object>> captures = payments != null
                        ? (List<Map<String, Object>>) payments.get("captures") : null;
                if (captures != null && !captures.isEmpty()) {
                    Map<String, Object> capture = captures.get(0);
                    Object captureStatus = capture.get("status");
                    Map<String, Object> statusDetails = (Map<String, Object>) capture.get("status_details");
                    Object reason = statusDetails != null ? statusDetails.get("reason") : null;
                    if (reason != null) return captureStatus + ": " + reason;
                    if (captureStatus != null) return captureStatus.toString();
                }
            }
        } catch (Exception ignored) {
            // response không đúng cấu trúc dự đoán - rơi xuống fallback bên dưới.
        }
        Object status = response.get("status");
        return status != null ? "Order status: " + status : String.valueOf(response);
    }

    // Parse response lỗi thật của PayPal, dạng: {"name":"UNPROCESSABLE_ENTITY","details":[{"issue":
    // "INSUFFICIENT_FUNDS","description":"..."}],"message":"..."}. Trả về "ISSUE: description" nếu
    // parse được, hoặc nguyên văn response body nếu không đúng cấu trúc này (vẫn còn hữu ích để debug).
    @SuppressWarnings("unchecked")
    private String extractFailureReason(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) return null;
        try {
            Map<String, Object> body = new ObjectMapper().readValue(responseBody, Map.class);
            List<Map<String, Object>> details = (List<Map<String, Object>>) body.get("details");
            if (details != null && !details.isEmpty()) {
                Object issue = details.get(0).get("issue");
                Object description = details.get(0).get("description");
                return description != null ? issue + ": " + description : String.valueOf(issue);
            }
            Object message = body.get("message");
            return message != null ? message.toString() : responseBody;
        } catch (Exception parseEx) {
            return responseBody;
        }
    }

    // Lấy thông tin thanh toán theo invoiceId - dùng cho trang chi tiết đơn hàng bên dashboard
    // (hiển thị failureReason khi đơn ở trạng thái FAILED). Trả về null nếu chưa từng có payment
    // nào cho invoice này (VD đơn OUT_OF_STOCK, chưa từng đi tới bước tạo PayPal order).
    public Payment getPaymentByInvoiceId(String invoiceId) {
        return paymentRepository.findByInvoiceId(invoiceId).orElse(null);
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