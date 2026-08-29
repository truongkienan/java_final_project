package com.ecommerce.frontend.controller.client;

import com.ecommerce.frontend.dto.AddressDTO;
import com.ecommerce.frontend.dto.MemberDTO;
import com.ecommerce.frontend.service.CustomerApiService;
import com.ecommerce.frontend.service.OrderApiService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/account")
public class AccountController {

    @Autowired
    private OrderApiService orderApiService;

    @Autowired
    private CustomerApiService customerApiService;

    @GetMapping
    public String showAccount(HttpSession session, Model model) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "redirect:/login";
        }
        model.addAttribute("username", username);
        model.addAttribute("orders", orderApiService.getOrdersByMember(username));

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
            redirectAttributes.addFlashAttribute("passwordError", "Không tìm thấy tài khoản!");
            return "redirect:/account";
        }
        boolean success = customerApiService.changeMemberPassword(member.getMemberId(), oldPassword, newPassword);
        if (success) {
            redirectAttributes.addFlashAttribute("passwordMessage", "Đổi mật khẩu thành công!");
        } else {
            redirectAttributes.addFlashAttribute("passwordError", "Mật khẩu cũ không đúng!");
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
                success ? "Đã hủy đơn hàng." : "Không thể hủy đơn hàng này.");
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
                success ? "Đã gửi yêu cầu hoàn tiền, vui lòng chờ Admin duyệt." : "Không thể gửi yêu cầu hoàn tiền.");
        return "redirect:/account";
    }
}