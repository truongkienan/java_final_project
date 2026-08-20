package com.ecommerce.order.entity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

@Entity
@Table(name = "InvoiceDetails")
@Data
public class InvoiceDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String productId;
    private String productName;
    private Double price;
    private Integer quantity;
    @ManyToOne
    @JoinColumn(name = "invoiceId")
    @JsonIgnore // Tránh bị lặp vô hạn khi parse JSON
    private Invoice invoice;
}