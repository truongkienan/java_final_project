package com.ecommerce.frontend.service;

import com.ecommerce.frontend.dto.StockDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class InventoryApiService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${INVENTORY_SERVICE_URL:http://localhost:8086/api/stocks}")
    private String inventoryServiceUrl;

    public List<StockDTO> getAllStocks() {
        try {
            StockDTO[] stocks = restTemplate.getForObject(inventoryServiceUrl, StockDTO[].class);
            return stocks != null ? Arrays.asList(stocks) : List.of();
        } catch (Exception e) {
            System.err.println("Lỗi lấy danh sách tồn kho: " + e.getMessage());
            return List.of();
        }
    }

    public boolean updateStock(String productId, Integer quantity) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(Map.of("quantity", quantity), headers);
            restTemplate.exchange(inventoryServiceUrl + "/" + productId, HttpMethod.PUT, request, Map.class);
            return true;
        } catch (Exception e) {
            System.err.println("Lỗi cập nhật tồn kho: " + e.getMessage());
            return false;
        }
    }
}