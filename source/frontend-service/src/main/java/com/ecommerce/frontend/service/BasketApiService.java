package com.ecommerce.frontend.service;

import com.ecommerce.frontend.dto.CartDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class BasketApiService {

    @Autowired
    private RestTemplate restTemplate;

    // Giả sử API Basket đang chạy ở port 8082
    @org.springframework.beans.factory.annotation.Value("${BASKET_SERVICE_URL:http://localhost:8083/api/baskets}")
    private String basketServiceUrl;

    public CartDTO updateBasket(CartDTO cartDTO) {
        HttpHeaders headers = new HttpHeaders();
        // Nhớ set Content-Type là JSON vì chúng ta sẽ gửi data đi
        headers.set("Content-Type", "application/json");

        // Đóng gói giỏ hàng vào Body của Request
        HttpEntity<CartDTO> requestEntity = new HttpEntity<>(cartDTO, headers);

        // Bắn request POST sang Basket Service
        ResponseEntity<CartDTO> response = restTemplate.exchange(
                basketServiceUrl,
                HttpMethod.POST,
                requestEntity,
                CartDTO.class);

        return response.getBody();
    }

    public CartDTO getBasket(String memberId) {
        try {
            return restTemplate.getForObject(basketServiceUrl + "/" + memberId, CartDTO.class);
        } catch (org.springframework.web.client.RestClientException e) {
            System.err.println("Lỗi lấy giỏ hàng: " + e.getMessage());
            return null;
        }
    }

    public void deleteBasket(String memberId) {
        try {
            restTemplate.delete(basketServiceUrl + "/" + memberId);
        } catch (org.springframework.web.client.RestClientException e) {
            System.err.println("Lỗi xóa giỏ hàng: " + e.getMessage());
        }
    }

}
