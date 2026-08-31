package com.ecommerce.payment.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "Payments")
@Data
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String invoiceId;
    private String paypalOrderId;
    private String captureId;
    private Double amount;
    private String currency;
    private String status; // CREATED, COMPLETED, CANCELLED, REFUNDED, FAILED

    // Lý do capture thất bại lấy từ response lỗi thật của PayPal (VD "INSUFFICIENT_FUNDS: ...") -
    // null nếu status khác FAILED, hoặc nếu PayPal không trả chi tiết lỗi.
    private String failureReason;

    private Date createdAt = new Date();
    private Date updatedAt = new Date();
}
