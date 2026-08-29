package com.ecommerce.frontend.controller.client;

import com.ecommerce.frontend.dto.CartDTO;
import com.ecommerce.frontend.service.BasketApiService;
import com.ecommerce.frontend.service.OrderApiService;
import com.ecommerce.frontend.service.PaymentApiService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    @Autowired
    private PaymentApiService paymentApiService;

    @Autowired
    private BasketApiService basketApiService;

    @Autowired
    private OrderApiService orderApiService;

    // 1. Hiển thị trang checkout — lấy giỏ hàng từ Redis
    @GetMapping
    public String showCheckout(HttpSession session, Model model) {
        String memberId = (String) session.getAttribute("memberId");
        if (memberId == null) memberId = "guest";

        CartDTO cart = basketApiService.getBasket(memberId);
        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            return "redirect:/";
        }

        double total = cart.getItems().stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();

        model.addAttribute("cart", cart);
        model.addAttribute("total", total);
        return "client/checkout";
    }

    // 2. User nhấn "Pay with PayPal"
    // Bước A: Tạo Invoice (PENDING_INVENTORY) thật bên order-service để lấy UUID + tổng tiền đã xác thực giá
    // Bước B: Poll ngầm chờ Saga Tồn kho (inventory-service) xử lý xong (trừ kho) - khách vẫn chỉ thấy
    //         1 request duy nhất, không cần đổi gì ở JS phía client
    // Bước C: Nếu đủ hàng (PENDING) mới tạo PayPal order → redirect user sang PayPal
    @PostMapping("/pay")
    public String initiatePayment(HttpSession session) {
        String memberId = (String) session.getAttribute("memberId");
        if (memberId == null) return "redirect:/";

        CartDTO cart = basketApiService.getBasket(memberId);
        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            return "redirect:/";
        }

        Map<String, Object> invoiceResult = orderApiService.createOrder(memberId, cart);
        if (invoiceResult == null || invoiceResult.get("id") == null) {
            return "redirect:/checkout?error=order_failed";
        }

        String invoiceId = invoiceResult.get("id").toString();

        // Saga Tồn kho: Invoice vừa tạo đang ở PENDING_INVENTORY (chờ inventory-service trừ kho bất
        // đồng bộ qua RabbitMQ). Poll ngầm tối đa ~5s cho tới khi có kết quả.
        String orderStatus = "PENDING_INVENTORY";
        long deadline = System.currentTimeMillis() + 5000;
        while ("PENDING_INVENTORY".equals(orderStatus) && System.currentTimeMillis() < deadline) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
            Map<String, Object> latest = orderApiService.getOrderById(invoiceId);
            if (latest != null && latest.get("status") != null) {
                orderStatus = latest.get("status").toString();
            }
        }

        if (!"PENDING".equals(orderStatus)) {
            // OUT_OF_STOCK hoặc hết timeout (vẫn PENDING_INVENTORY) - đều không cho qua bước thanh toán
            return "redirect:/checkout?error=out_of_stock";
        }

        double total = Double.parseDouble(invoiceResult.get("totalAmount").toString());

        String paypalOrderId = paymentApiService.createPaypalOrder(total, invoiceId);

        if (paypalOrderId == null) {
            return "redirect:/checkout?error=payment_failed";
        }

        return "redirect:https://www.sandbox.paypal.com/checkoutnow?token=" + paypalOrderId;
    }

    // 3. PayPal redirect về đây khi user approve
    @GetMapping("/success")
    public String paymentSuccess(@RequestParam("token") String paypalOrderId,
                                 HttpSession session, Model model) {
        boolean success = paymentApiService.capturePaypalOrder(paypalOrderId);

        if (success) {
            String memberId = (String) session.getAttribute("memberId");
            if (memberId != null) {
                basketApiService.deleteBasket(memberId);
            }
            model.addAttribute("message", "Đặt hàng thành công! Cảm ơn bạn đã mua hàng.");
            return "client/order-success";
        }

        model.addAttribute("message", "Có lỗi trong quá trình thanh toán. Vui lòng thử lại.");
        return "client/order-cancel";
    }

    // 4. User hủy trên PayPal
    @GetMapping("/cancel")
    public String paymentCancel(@RequestParam(value = "token", required = false) String paypalOrderId,
                                Model model) {
        if (paypalOrderId != null) {
            paymentApiService.cancelPaypalOrder(paypalOrderId);
        }
        model.addAttribute("message", "Bạn đã hủy thanh toán. Giỏ hàng vẫn được giữ nguyên.");
        return "client/order-cancel";
    }
}