package com.ecommerce.inventory.controller;

import com.ecommerce.inventory.entity.Stock;
import com.ecommerce.inventory.repository.StockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("/api/stocks")
public class StockController {

    @Autowired
    private StockRepository stockRepository;

    @GetMapping
    public ResponseEntity<?> getAllStocks() {
        return ResponseEntity.ok(stockRepository.findAll());
    }

    // Admin nhập/điều chỉnh số lượng tồn kho tay - tạo mới nếu sản phẩm chưa có dòng tồn kho (upsert)
    @PutMapping("/{productId}")
    public ResponseEntity<?> setStock(@PathVariable("productId") String productId, @RequestBody Map<String, Object> body) {
        Integer quantity = Integer.valueOf(body.get("quantity").toString());
        Stock stock = stockRepository.findById(productId).orElse(new Stock());
        stock.setProductId(productId);
        stock.setQuantity(quantity);
        stock.setUpdatedAt(new Date());
        return ResponseEntity.ok(stockRepository.save(stock));
    }
}