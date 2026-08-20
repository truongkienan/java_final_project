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

    private Date createdAt = new Date();
    private Date updatedAt = new Date();
}
