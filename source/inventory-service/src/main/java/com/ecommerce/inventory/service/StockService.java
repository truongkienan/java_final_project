package com.ecommerce.inventory.service;

import com.ecommerce.inventory.dto.StockEventDTO;
import com.ecommerce.inventory.dto.StockItemDTO;
import com.ecommerce.inventory.repository.StockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockService {

    @Autowired
    private StockRepository stockRepository;

    // Trừ kho cho toàn bộ sản phẩm trong 1 đơn hàng - tất cả hoặc không gì cả (all-or-nothing).
    // Chỉ cần 1 sản phẩm không đủ hàng là toàn bộ transaction rollback (không trừ dở dang).
    @Transactional
    public void reserveStock(StockEventDTO event) {
        for (StockItemDTO item : event.getItems()) {
            int updated = stockRepository.deductIfAvailable(item.getProductId(), item.getQuantity());
            if (updated == 0) {
                throw new RuntimeException("Không đủ tồn kho cho sản phẩm ID: " + item.getProductId());
            }
        }
    }

    // Hoàn kho (compensating transaction) - luôn thành công, không cần kiểm tra điều kiện.
    @Transactional
    public void restoreStock(StockEventDTO event) {
        for (StockItemDTO item : event.getItems()) {
            stockRepository.restore(item.getProductId(), item.getQuantity());
        }
    }
}