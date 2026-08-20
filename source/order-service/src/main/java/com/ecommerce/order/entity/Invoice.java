package com.ecommerce.order.entity;
import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "Invoices")
@Data
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String memberId; // Lưu ID của khách hàng mua

    private Date orderDate = new Date(); // Ngày đặt tự động lấy giờ hiện tại
    private Double totalAmount; // Tổng tiền
    private String status = "PENDING"; // Mặc định khi khách đặt là PENDING
    // 1 Hóa đơn có nhiều Chi tiết hóa đơn
    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL)
    private List<InvoiceDetail> details;
}