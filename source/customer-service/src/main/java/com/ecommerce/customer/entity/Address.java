package com.ecommerce.customer.entity;
import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

@Entity
@Table(name = "Addresses")
@Data
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer addressId;
    @Column(nullable = false, columnDefinition = "NVARCHAR(255)")
    private String addressName;
    // Giữ khóa ngoại dạng reference đơn giản thay vì Relation mapping lúc này
    private UUID memberId;
    private Integer wardId;

    @Column(columnDefinition = "NVARCHAR(100)")
    private String wardName;

    private Short districtId;

    @Column(columnDefinition = "NVARCHAR(100)")
    private String districtName;

    private Byte provinceId;

    @Column(columnDefinition = "NVARCHAR(100)")
    private String provinceName;
    @Column(nullable = false, length = 16)
    private String phone;
    private Boolean isDefault;
}
