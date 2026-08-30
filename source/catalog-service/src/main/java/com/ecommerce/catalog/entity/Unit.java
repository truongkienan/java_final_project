package com.ecommerce.catalog.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "units")
@Data
public class Unit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Short unitId;

    @Column(nullable = false, length = 50)
    private String unitName;

    // Moi Unit gan voi dung 1 Category - dropdown chon Unit o form Product van
    // hien phang (khong loc theo category dang chon) nhung ten Category duoc
    // hien kem de phan biet cac Unit trung ten o category khac nhau (vi du "Pack").
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
}
