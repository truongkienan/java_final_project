package com.ecommerce.frontend.service;

import com.ecommerce.frontend.dto.CartDTO;
import com.ecommerce.frontend.dto.CartItemDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class OrderApiService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${ORDER_SERVICE_URL:http://localhost:8084/api/orders}")
    private String orderServiceUrl;

    // Gọi Order Service tạo Invoice (PENDING) trước khi thanh toán
    // Trả về Invoice đã lưu (có id thật là UUID + totalAmount đã xác thực giá qua gRPC)
    public Map<String, Object> createOrder(String memberId, CartDTO cart) {
        try {
            List<Map<String, Object>> details = new ArrayList<>();
            for (CartItemDTO item : cart.getItems()) {
                details.add(Map.of(
                        "productId", item.getProductId(),
                        "quantity", item.getQuantity()));
            }

            Map<String, Object> body = Map.of(
                    "memberId", memberId,
                    "details", details);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    orderServiceUrl + "/checkout",
                    HttpMethod.POST, request, Map.class);

            return response.getBody();

        } catch (Exception e) {
            System.err.println("Lỗi tạo Order: " + e.getMessage());
            return null;
        }
    }

    public java.util.List<com.ecommerce.frontend.dto.OrderDTO> getOrders() {
        try {
            com.ecommerce.frontend.dto.OrderDTO[] orders = restTemplate.getForObject(
                    orderServiceUrl, com.ecommerce.frontend.dto.OrderDTO[].class);
            return orders != null ? java.util.Arrays.asList(orders) : java.util.List.of();
        } catch (Exception e) {
            System.err.println("Lỗi lấy danh sách đơn hàng: " + e.getMessage());
            return java.util.List.of();
        }
    }

    public java.util.List<com.ecommerce.frontend.dto.OrderDTO> getOrdersByMember(String memberId) {
        try {
            com.ecommerce.frontend.dto.OrderDTO[] orders = restTemplate.getForObject(
                    orderServiceUrl + "/member/" + memberId, com.ecommerce.frontend.dto.OrderDTO[].class);
            return orders != null ? java.util.Arrays.asList(orders) : java.util.List.of();
        } catch (Exception e) {
            System.err.println("Lỗi lấy đơn hàng của thành viên: " + e.getMessage());
            return java.util.List.of();
        }
    }

    // Hủy đơn hàng (chỉ hợp lệ khi đơn đang PENDING - order-service tự validate)
    public boolean cancelOrder(String orderId) {
        try {
            restTemplate.exchange(orderServiceUrl + "/" + orderId + "/cancel",
                    HttpMethod.PUT, HttpEntity.EMPTY, Map.class);
            return true;
        } catch (Exception e) {
            System.err.println("Lỗi hủy đơn hàng: " + e.getMessage());
            return false;
        }
    }

    // Khách yêu cầu hoàn tiền (chỉ hợp lệ khi đơn đang PAID)
    public boolean requestRefund(String orderId) {
        try {
            restTemplate.exchange(orderServiceUrl + "/" + orderId + "/request-refund",
                    HttpMethod.PUT, HttpEntity.EMPTY, Map.class);
            return true;
        } catch (Exception e) {
            System.err.println("Lỗi yêu cầu hoàn tiền: " + e.getMessage());
            return false;
        }
    }

    // Admin từ chối yêu cầu hoàn tiền - trả đơn về lại PAID
    public boolean rejectRefund(String orderId) {
        try {
            restTemplate.exchange(orderServiceUrl + "/" + orderId + "/reject-refund",
                    HttpMethod.PUT, HttpEntity.EMPTY, Map.class);
            return true;
        } catch (Exception e) {
            System.err.println("Lỗi từ chối hoàn tiền: " + e.getMessage());
            return false;
        }
    }

    // Lấy 1 đơn hàng theo ID - dùng để poll trạng thái sau khi checkout, chờ Saga Tồn kho
    // (inventory-service) xử lý xong PENDING_INVENTORY -> PENDING/OUT_OF_STOCK.
    public Map<String, Object> getOrderById(String orderId) {
        try {
            return restTemplate.getForObject(orderServiceUrl + "/" + orderId, Map.class);
        } catch (Exception e) {
            System.err.println("Lỗi lấy đơn hàng: " + e.getMessage());
            return null;
        }
    }

    // Admin xóa hẳn đơn hàng khỏi hệ thống - xóa cứng (không khôi phục được).
    public boolean deleteOrder(String orderId) {
        try {
            restTemplate.exchange(orderServiceUrl + "/" + orderId, HttpMethod.DELETE, HttpEntity.EMPTY, Void.class);
            return true;
        } catch (Exception e) {
            System.err.println("Lỗi xóa đơn hàng: " + e.getMessage());
            return false;
        }
    }
}