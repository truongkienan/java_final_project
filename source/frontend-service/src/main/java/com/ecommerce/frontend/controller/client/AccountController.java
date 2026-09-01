package com.ecommerce.frontend.controller.client;

import com.ecommerce.frontend.dto.AddressDTO;
import com.ecommerce.frontend.dto.MemberDTO;
import com.ecommerce.frontend.dto.OrderDTO;
import com.ecommerce.frontend.service.CustomerApiService;
import com.ecommerce.frontend.service.OrderApiService;
import com.ecommerce.frontend.service.PaymentApiService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/account")
public class AccountController {

    @Autowired
    private OrderApiService orderApiService;

    @Autowired
    private CustomerApiService customerApiService;

    @Autowired
    private PaymentApiService paymentApiService;

    // So don hang hien thi tren 1 trang cua Order History.
    private static final int ORDERS_PAGE_SIZE = 5;

    @GetMapping
    public String showAccount(@RequestParam(value = "page", defaultValue = "0") int page,
                              HttpSession session, Model model) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "redirect:/login";
        }
        model.addAttribute("username", username);

        // order-service da tra ve san danh sach sap xep moi nhat truoc (findAllByMemberIdOrderByOrderDateDesc).
        // Phan trang ngay tai day (in-memory) thay vi sua them API co Pageable ben order-service - so luong
        // don hang cua 1 khach hang o quy mo du an nay khong lon, khong can toi uu truy van phan trang that su.
        List<OrderDTO> allOrders = orderApiService.getOrdersByMember(username);
        int totalPages = Math.max(1, (int) Math.ceil((double) allOrders.size() / ORDERS_PAGE_SIZE));
        int currentPage = Math.max(0, Math.min(page, totalPages - 1));
        int fromIndex = Math.min(currentPage * ORDERS_PAGE_SIZE, allOrders.size());
        int toIndex = Math.min(fromIndex + ORDERS_PAGE_SIZE, allOrders.size());

        model.addAttribute("orders", allOrders.subList(fromIndex, toIndex));
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);

        MemberDTO member = customerApiService.getMemberByUsername(username);
        if (member != null) {
            model.addAttribute("addresses", customerApiService.getAddressesByMember(member.getMemberId()));
        } else {
            model.addAttribute("addresses", List.of());
        }
        return "client/account";
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam("oldPassword") String oldPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 HttpSession session, RedirectAttributes redirectAttributes) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "redirect:/login";
        }
        // Đổi mật khẩu bên Members (customer-service) - trước đây gọi nhầm sang auth-service (users),
        // giờ khách hàng không còn dòng trong users nữa nên bắt buộc phải đổi bên này.
        MemberDTO member = customerApiService.getMemberByUsername(username);
        if (member == null) {
            redirectAttributes.addFlashAttribute("passwordError", "Account not found!");
            return "redirect:/account";
        }
        boolean success = customerApiService.changeMemberPassword(member.getMemberId(), oldPassword, newPassword);
        if (success) {
            redirectAttributes.addFlashAttribute("passwordMessage", "Password changed successfully!");
        } else {
            redirectAttributes.addFlashAttribute("passwordError", "Old password is incorrect!");
        }
        return "redirect:/account";
    }

    @PostMapping("/addresses/add")
    public String addAddress(@RequestParam("addressName") String addressName,
                             @RequestParam("phone") String phone,
                             HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "redirect:/login";
        }
        MemberDTO member = customerApiService.getMemberByUsername(username);
        if (member != null) {
            AddressDTO address = new AddressDTO();
            address.setMemberId(member.getMemberId());
            address.setAddressName(addressName);
            address.setPhone(phone);
            customerApiService.addAddress(address);
        }
        return "redirect:/account";
    }

    @PostMapping("/addresses/delete/{id}")
    public String deleteAddress(@PathVariable("id") Integer id) {
        customerApiService.deleteAddress(id);
        return "redirect:/account";
    }

    // Xem chi tiết 1 đơn hàng của chính mình - lấy trực tiếp từ getOrdersByMember() (đã có sẵn
    // details/items) thay vì gọi thêm API getOrderById(), đồng thời kiêm luôn việc chặn khách sửa
    // URL để xem đơn của người khác (đơn không nằm trong danh sách của chính họ -> redirect).
    @GetMapping("/orders/{id}")
    public String viewOrder(@PathVariable("id") String id, HttpSession session,
                            RedirectAttributes redirectAttributes, Model model) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "redirect:/login";
        }
        Optional<OrderDTO> orderOpt = orderApiService.getOrdersByMember(username).stream()
                .filter(o -> o.getId().equals(id))
                .findFirst();
        if (orderOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("orderError", "Order not found.");
            return "redirect:/account";
        }
        model.addAttribute("order", orderOpt.get());
        model.addAttribute("payment", paymentApiService.getPaymentByInvoiceId(id));
        return "client/order-detail";
    }

    // Hủy đơn hàng của chính mình (chỉ hợp lệ khi đơn đang PENDING - order-service tự validate)
    @PostMapping("/orders/{id}/cancel")
    public String cancelOrder(@PathVariable("id") String id, HttpSession session,
                              RedirectAttributes redirectAttributes) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "redirect:/login";
        }
        // Chặn khách sửa URL để hủy đơn của người khác - chỉ cho phép nếu đơn thuộc về chính họ
        boolean owns = orderApiService.getOrdersByMember(username).stream()
                .anyMatch(o -> o.getId().equals(id));
        if (!owns) {
            return "redirect:/account";
        }
        boolean success = orderApiService.cancelOrder(id);
        redirectAttributes.addFlashAttribute(success ? "orderMessage" : "orderError",
                success ? "Order cancelled." : "Unable to cancel this order.");
        return "redirect:/account";
    }

    // Yêu cầu hoàn tiền cho đơn đã PAID - chờ Admin duyệt
    @PostMapping("/orders/{id}/request-refund")
    public String requestRefund(@PathVariable("id") String id, HttpSession session,
                                RedirectAttributes redirectAttributes) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "redirect:/login";
        }
        boolean owns = orderApiService.getOrdersByMember(username).stream()
                .anyMatch(o -> o.getId().equals(id));
        if (!owns) {
            return "redirect:/account";
        }
        boolean success = orderApiService.requestRefund(id);
        redirectAttributes.addFlashAttribute(success ? "orderMessage" : "orderError",
                success ? "Refund request submitted, please wait for Admin approval." : "Unable to submit refund request.");
        return "redirect:/account";
    }
}