package com.ecommerce.frontend.controller.dashboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.ecommerce.frontend.service.OrderApiService;
import com.ecommerce.frontend.dto.OrderDTO;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {
    @Autowired
    private OrderApiService orderApiService;

    // Ánh xạ đường dẫn /dashboard trả về trang chủ Dashboard - kèm thống kê doanh thu 6 tháng gần nhất
    // (chỉ tính đơn PAID) và danh sách 10 đơn hàng gần đây nhất. Dữ liệu lấy từ order-service qua
    // API đã có sẵn (getAllOrders() đã sort sẵn desc theo ngày) - không cần thêm service/endpoint mới.
    @GetMapping
    public String index(Model model) {
        List<OrderDTO> orders = orderApiService.getAllOrders();

        YearMonth currentMonth = YearMonth.now();
        List<String> monthLabels = new ArrayList<>();
        List<Double> monthRevenue = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            YearMonth month = currentMonth.minusMonths(i);
            String monthKey = month.toString(); // "yyyy-MM"
            double revenue = orders.stream()
                    .filter(o -> "PAID".equals(o.getStatus()) && o.getOrderDate() != null && o.getOrderDate().startsWith(monthKey))
                    .mapToDouble(OrderDTO::getTotalAmount)
                    .sum();
            monthLabels.add(month.getMonthValue() + "/" + month.getYear());
            monthRevenue.add(revenue);
        }

        List<OrderDTO> recentOrders = orders.stream().limit(10).collect(Collectors.toList());

        model.addAttribute("revenueMonthLabels", monthLabels);
        model.addAttribute("revenueMonthData", monthRevenue);
        model.addAttribute("recentOrders", recentOrders);
        return "dashboard/index";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "dashboard/401";
    }

}