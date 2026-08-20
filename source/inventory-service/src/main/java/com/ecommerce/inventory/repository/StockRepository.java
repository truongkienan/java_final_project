package com.ecommerce.inventory.repository;

import com.ecommerce.inventory.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StockRepository extends JpaRepository<Stock, String> {

    // Trừ kho có điều kiện (atomic) - tránh race condition khi nhiều đơn cùng mua 1 sản phẩm.
    // Trả về số dòng bị ảnh hưởng: 1 = trừ thành công, 0 = không đủ hàng (hoặc không tồn tại productId).
    @Modifying
    @Query("UPDATE Stock s SET s.quantity = s.quantity - :qty, s.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE s.productId = :productId AND s.quantity >= :qty")
    int deductIfAvailable(@Param("productId") String productId, @Param("qty") Integer qty);

    // Hoàn kho (compensating transaction của Saga) - luôn an toàn, không cần điều kiện.
    @Modifying
    @Query("UPDATE Stock s SET s.quantity = s.quantity + :qty, s.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE s.productId = :productId")
    int restore(@Param("productId") String productId, @Param("qty") Integer qty);
}