package com.ecommerce.payment.controller;

import com.ecommerce.payment.entity.Payment;
import com.ecommerce.payment.publisher.PaymentEventPublisher;
import com.ecommerce.payment.service.PayPalService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final PayPalService payPalService;
    private final PaymentEventPublisher paymentEventPublisher;

    public PaymentController(PayPalService payPalService, PaymentEventPublisher paymentEventPublisher) {
        this.payPalService = payPalService;
        this.paymentEventPublisher = paymentEventPublisher;
    }

    @PostMapping("/orders")
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> payload) throws Exception {
        Double amount = Double.parseDouble(payload.getOrDefault("amount", "100.0").toString());
        String invoiceId = payload.getOrDefault("invoiceId", "UNKNOWN").toString();
        Payment payment = payPalService.createPayment(invoiceId, amount, "USD");
        return ResponseEntity.ok(Map.of("id", payment.getPaypalOrderId()));
    }

    @PostMapping("/orders/{orderId}/capture")
    public ResponseEntity<?> captureOrder(@PathVariable("orderId") String orderId) throws Exception {
        Payment payment = payPalService.capturePayment(orderId);
        if ("COMPLETED".equals(payment.getStatus())) {
            paymentEventPublisher.publishPaymentStatus(payment.getInvoiceId(), "PAID");
            return ResponseEntity.ok(Map.of("status", "COMPLETED", "paypalOrderId", orderId));
        }
        paymentEventPublisher.publishPaymentStatus(payment.getInvoiceId(), "FAILED");
        // HashMap (không phải Map.of) vì failureReason có thể null khi PayPal không trả chi tiết lỗi.
        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("error", "Thanh toán thất bại");
        errorBody.put("reason", payment.getFailureReason());
        return ResponseEntity.badRequest().body(errorBody);
    }

    // Lấy thông tin thanh toán (bao gồm lý do thất bại nếu có) theo invoiceId - dùng cho trang
    // chi tiết đơn hàng bên dashboard.
    @GetMapping("/invoice/{invoiceId}")
    public ResponseEntity<?> getPaymentByInvoice(@PathVariable("invoiceId") String invoiceId) {
        Payment payment = payPalService.getPaymentByInvoiceId(invoiceId);
        if (payment == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(payment);
    }

    @PostMapping("/orders/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable("orderId") String orderId) {
        Payment payment = payPalService.cancelPayment(orderId);
        paymentEventPublisher.publishPaymentStatus(payment.getInvoiceId(), "CANCELLED");
        return ResponseEntity.ok(Map.of("message", "Đã hủy giao dịch"));
    }

    @PostMapping("/invoice/{invoiceId}/refund")
    public ResponseEntity<?> refundPayment(@PathVariable("invoiceId") String invoiceId) {
        Payment payment = payPalService.refundPayment(invoiceId);
        paymentEventPublisher.publishPaymentStatus(payment.getInvoiceId(), "REFUNDED");
        return ResponseEntity.ok(Map.of("message", "Đã hoàn tiền"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }
}