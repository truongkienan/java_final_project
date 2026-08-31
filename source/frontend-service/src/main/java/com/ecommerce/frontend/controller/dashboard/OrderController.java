package com.ecommerce.frontend.controller.dashboard;

import com.ecommerce.frontend.service.OrderApiService;
import com.ecommerce.frontend.service.PaymentApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/dashboard/orders")
public class OrderController {

    @Autowired
    private OrderApiService orderApiService;
    @Autowired
    private PaymentApiService paymentApiService;

    @GetMapping
    public String manageOrders(Model model) {
        model.addAttribute("orders", orderApiService.getAllOrders());
        return "dashboard/orders";
    }

    // Xem chi tiết 1 đơn hàng - gồm cả thông tin thanh toán (lý do thất bại nếu có) lấy riêng
    // từ payment-service, vì order-service không lưu dữ liệu này.
    @GetMapping("/{id}")
    public String viewOrder(@PathVariable("id") String id, Model model, RedirectAttributes redirectAttributes) {
        Map<String, Object> order = orderApiService.getOrderById(id);
        if (order == null) {
            redirectAttributes.addFlashAttribute("orderError", "Order not found.");
            return "redirect:/dashboard/orders";
        }
        model.addAttribute("order", order);
        model.addAttribute("payment", paymentApiService.getPaymentByInvoiceId(id));
        return "dashboard/order-detail";
    }

    // Admin xóa hẳn đơn hàng - xóa cứng khỏi order-service, không thể khôi phục.
    @PostMapping("/{id}/delete")
    public String deleteOrder(@PathVariable("id") String id, RedirectAttributes redirectAttributes) {
        boolean success = orderApiService.deleteOrder(id);
        redirectAttributes.addFlashAttribute(success ? "orderMessage" : "orderError",
                success ? "Order deleted." : "Unable to delete this order.");
        return "redirect:/dashboard/orders";
    }

    // Admin huy don hang dang PENDING
    @PostMapping("/{id}/cancel")
    public String cancelOrder(@PathVariable("id") String id, RedirectAttributes redirectAttributes) {
        boolean success = orderApiService.cancelOrder(id);
        redirectAttributes.addFlashAttribute(success ? "orderMessage" : "orderError",
                success ? "Order cancelled." : "Unable to cancel this order.");
        return "redirect:/dashboard/orders";
    }

    // Admin duyet hoan tien (don PAID hoac REFUND_REQUESTED) - goi PayPal refund that,
    // Invoice.status tu chuyen REFUNDED qua RabbitMQ sau khi payment-service publish su kien.
    @PostMapping("/{id}/refund")
    public String refundOrder(@PathVariable("id") String id, RedirectAttributes redirectAttributes) {
        boolean success = paymentApiService.refundPaypalOrder(id);

        // payment-service publish "payment.success=REFUNDED" xong là trả HTTP 200 ngay - nhưng
        // order-service cập nhật Invoice.status trên 1 thread RabbitMQ riêng, bất đồng bộ. Nếu
        // redirect ngay, trang /dashboard/orders có thể render TRƯỚC khi DB kịp cập nhật, buộc
        // admin phải F5 thủ công mới thấy REFUNDED. Poll tối đa 3s để tránh race condition này -
        // cùng pattern đã dùng ở CheckoutController khi chờ Saga tồn kho xử lý xong.
        if (success) {
            long deadline = System.currentTimeMillis() + 3000;
            while (System.currentTimeMillis() < deadline) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
                Map<String, Object> latest = orderApiService.getOrderById(id);
                if (latest != null && "REFUNDED".equals(latest.get("status"))) {
                    break;
                }
            }
        }

        redirectAttributes.addFlashAttribute(success ? "orderMessage" : "orderError",
                success ? "Refunded." : "Unable to refund this order.");
        return "redirect:/dashboard/orders";
    }

    // Admin tu choi yeu cau hoan tien - tra don ve lai PAID
    @PostMapping("/{id}/reject-refund")
    public String rejectRefund(@PathVariable("id") String id, RedirectAttributes redirectAttributes) {
        boolean success = orderApiService.rejectRefund(id);
        redirectAttributes.addFlashAttribute(success ? "orderMessage" : "orderError",
                success ? "Refund request rejected." : "Unable to reject this request.");
        return "redirect:/dashboard/orders";
    }
}