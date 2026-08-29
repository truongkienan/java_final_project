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