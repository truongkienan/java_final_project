package com.ecommerce.frontend.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.ecommerce.frontend.service.CatalogApiService;
import com.ecommerce.frontend.service.OrderApiService;
import com.ecommerce.frontend.service.PaymentApiService;
import com.ecommerce.frontend.service.InventoryApiService;
import com.ecommerce.frontend.service.CustomerApiService;
import com.ecommerce.frontend.service.RoleApiService;
import com.ecommerce.frontend.service.UserApiService;
import com.ecommerce.frontend.service.AuthApiService;
import com.ecommerce.frontend.dto.CategoryDTO;
import com.ecommerce.frontend.dto.OrderDTO;
import com.ecommerce.frontend.dto.PermissionDTO;
import com.ecommerce.frontend.dto.RoleDTO;
import com.ecommerce.frontend.dto.StockDTO;
import com.ecommerce.frontend.dto.UserDto;
import com.ecommerce.frontend.dto.UserSummaryDTO;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.ecommerce.frontend.dto.ProductDTO;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private CatalogApiService catalogApiService;
    @Autowired
    private OrderApiService orderApiService;
    @Autowired
    private PaymentApiService paymentApiService;
    @Autowired
    private InventoryApiService inventoryApiService;
    @Autowired
    private CustomerApiService customerApiService;
    @Autowired
    private RoleApiService roleApiService;
    @Autowired
    private UserApiService userApiService;
    @Autowired
    private AuthApiService authApiService;

    @GetMapping("/orders")
    public String manageOrders(Model model) {
        model.addAttribute("orders", orderApiService.getAllOrders());
        return "admin/orders";
    }

    @GetMapping("/members")
    public String manageMembers(Model model) {
        model.addAttribute("members", customerApiService.getAllMembers());
        return "admin/members";
    }

    // Ánh xạ đường dẫn /admin trả về trang chủ Dashboard - kèm thống kê doanh thu 6 tháng gần nhất
    // (chỉ tính đơn PAID) và danh sách 10 đơn hàng gần đây nhất. Dữ liệu lấy từ order-service qua
    // API đã có sẵn (getAllOrders() đã sort sẵn desc theo ngày) - không cần thêm service/endpoint mới.
    @GetMapping
    public String dashboard(Model model) {
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
        return "admin/index";
    }

    // Ánh xạ /admin/products trả về trang danh sách sản phẩm
    @GetMapping("/products")
    public String manageProducts(Model model) {
        model.addAttribute("products", catalogApiService.getAllProducts());
        return "admin/products";
    }


    // Hiển thị danh sách Danh mục theo đúng thứ tự cây (cấp 1 rồi tới các con của nó ngay sau), kèm tên Danh mục cha
    @GetMapping("/categories")
    public String manageCategories(Model model) {
        List<CategoryDTO> flat = catalogApiService.getAllCategories();

        Map<Short, String> nameById = flat.stream()
                .collect(Collectors.toMap(CategoryDTO::getCategoryId, CategoryDTO::getCategoryName));

        List<CategoryDTO> roots = flat.stream()
                .filter(c -> c.getParentId() == null)
                .sorted(Comparator.comparing(c -> c.getPosition() == null ? 0 : c.getPosition()))
                .collect(Collectors.toList());

        List<CategoryDTO> sorted = new ArrayList<>();
        for (CategoryDTO root : roots) {
            sorted.add(root);
            flat.stream()
                    .filter(c -> root.getCategoryId().equals(c.getParentId()))
                    .sorted(Comparator.comparing(c -> c.getPosition() == null ? 0 : c.getPosition()))
                    .forEach(child -> {
                        child.setParentName(nameById.get(child.getParentId()));
                        sorted.add(child);
                    });
        }

        model.addAttribute("categories", sorted);
        return "admin/categories";
    }

    // Mở giao diện Thêm mới (Truyền 1 Object rỗng)
    @GetMapping("/categories/new")
    public String createCategoryForm(Model model) {
        CategoryDTO newCategory = new CategoryDTO();
        newCategory.setActive(true);
        newCategory.setPosition(0);
        model.addAttribute("category", newCategory);
        model.addAttribute("parentCategories", getTopLevelCategories());
        return "admin/category-form";
    }

    // Mở giao diện Sửa (Truyền Object lấy từ DB)
    @GetMapping("/categories/edit/{id}")
    public String editCategoryForm(@PathVariable("id") Short id, Model model) {
        model.addAttribute("category", catalogApiService.getCategoryById(id));
        model.addAttribute("parentCategories", getTopLevelCategories());
        return "admin/category-form";
    }

    // Chỉ lấy danh mục cấp 1 (parentId null) để làm danh sách lựa chọn "Danh mục cha"
    private List<CategoryDTO> getTopLevelCategories() {
        return catalogApiService.getAllCategories().stream()
                .filter(c -> c.getParentId() == null)
                .sorted(Comparator.comparing(c -> c.getPosition() == null ? 0 : c.getPosition()))
                .collect(Collectors.toList());
    }


    // Nhận dữ liệu từ Form gửi lên để Lưu, sau đó quay về trang danh sách
    @PostMapping("/categories/save")
    public String saveCategory(@ModelAttribute CategoryDTO categoryDTO) {
        catalogApiService.saveCategory(categoryDTO);
        return "redirect:/admin/categories";
    }

    @GetMapping("/products/new")
    public String createProductForm(Model model) {
        model.addAttribute("product", new ProductDTO());
        return "admin/product-form";
    }

    @GetMapping("/products/edit/{id}")
    public String editProductForm(@PathVariable("id") Integer id, Model model) {
        model.addAttribute("product", catalogApiService.getProductById(id));
        return "admin/product-form";
    }

    @PostMapping("/products/save")
    public String saveProduct(@ModelAttribute ProductDTO productDTO) {
        catalogApiService.saveProduct(productDTO);
        return "redirect:/admin/products";
    }
    @PostMapping("/categories/delete/{id}")
    public String deleteCategory(@PathVariable("id") Short id) {
        catalogApiService.deleteCategory(id);
        return "redirect:/admin/categories";
    }

    @PostMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable("id") Integer id) {
        catalogApiService.deleteProduct(id);
        return "redirect:/admin/products";
    }

    // Hiển thị danh sách Role kèm permission đã gán
    @GetMapping("/roles")
    public String manageRoles(Model model) {
        model.addAttribute("roles", roleApiService.getAllRoles());
        return "admin/roles";
    }

    @GetMapping("/roles/new")
    public String createRoleForm(Model model) {
        model.addAttribute("role", new RoleDTO());
        model.addAttribute("allPermissions", roleApiService.getAllPermissions());
        return "admin/role-form";
    }

    @GetMapping("/roles/edit/{id}")
    public String editRoleForm(@PathVariable("id") Short id, Model model) {
        RoleDTO role = roleApiService.getRoleById(id);
        // Suy ra permissionIds từ danh sách permissions đầy đủ để checkbox trên form tick đúng
        if (role.getPermissions() != null) {
            role.setPermissionIds(role.getPermissions().stream()
                    .map(PermissionDTO::getPermissionId)
                    .collect(Collectors.toList()));
        }
        model.addAttribute("role", role);
        model.addAttribute("allPermissions", roleApiService.getAllPermissions());
        return "admin/role-form";
    }

    @PostMapping("/roles/save")
    public String saveRole(@ModelAttribute RoleDTO roleDTO) {
        roleApiService.saveRole(roleDTO);
        return "redirect:/admin/roles";
    }

    @PostMapping("/roles/delete/{id}")
    public String deleteRole(@PathVariable("id") Short id) {
        roleApiService.deleteRole(id);
        return "redirect:/admin/roles";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "admin/401";
    }

    // Hiển thị danh sách tài khoản Admin/Staff kèm Role hiện tại
    @GetMapping("/users")
    public String manageUsers(Model model) {
        model.addAttribute("users", userApiService.getAllUsers());
        return "admin/users";
    }

    @GetMapping("/users/edit/{id}")
    public String editUserRoleForm(@PathVariable("id") Long id, Model model) {
        model.addAttribute("user", userApiService.getUserById(id));
        model.addAttribute("allRoles", roleApiService.getAllRoles());
        return "admin/user-role-form";
    }

    @PostMapping("/users/save")
    public String saveUserRole(@ModelAttribute UserSummaryDTO userSummaryDTO) {
        userApiService.updateUserRole(userSummaryDTO.getId(), userSummaryDTO.getRoleId());
        userApiService.updateActive(userSummaryDTO.getId(), Boolean.TRUE.equals(userSummaryDTO.getActive()));
        return "redirect:/admin/users";
    }

    // Mở form tạo tài khoản mới (username/password/role) - tái sử dụng UserDto và AuthApiService.register()
    // đã có sẵn từ luồng /admin/register, khác chỗ Role được chọn tự do thay vì hardcode ROLE_ADMIN.
    @GetMapping("/users/new")
    public String createUserForm(Model model) {
        model.addAttribute("newUser", new UserDto());
        model.addAttribute("allRoles", roleApiService.getAllRoles());
        return "admin/user-form";
    }

    @PostMapping("/users/create")
    public String createUser(@ModelAttribute("newUser") UserDto userDto, Model model) {
        boolean success = authApiService.register(userDto);
        if (!success) {
            model.addAttribute("error", "Tạo tài khoản thất bại, username có thể đã tồn tại!");
            model.addAttribute("allRoles", roleApiService.getAllRoles());
            return "admin/user-form";
        }
        return "redirect:/admin/users";
    }

    // Admin hủy đơn hàng đang PENDING
    @PostMapping("/orders/{id}/cancel")
    public String cancelOrder(@PathVariable("id") String id, RedirectAttributes redirectAttributes) {
        boolean success = orderApiService.cancelOrder(id);
        redirectAttributes.addFlashAttribute(success ? "orderMessage" : "orderError",
                success ? "Đã hủy đơn hàng." : "Không thể hủy đơn hàng này.");
        return "redirect:/admin/orders";
    }

    // Admin duyệt hoàn tiền (đơn PAID hoặc REFUND_REQUESTED) - gọi PayPal refund thật,
    // Invoice.status tự chuyển REFUNDED qua RabbitMQ sau khi payment-service publish sự kiện.
    @PostMapping("/orders/{id}/refund")
    public String refundOrder(@PathVariable("id") String id, RedirectAttributes redirectAttributes) {
        boolean success = paymentApiService.refundPaypalOrder(id);
        redirectAttributes.addFlashAttribute(success ? "orderMessage" : "orderError",
                success ? "Đã hoàn tiền." : "Không thể hoàn tiền cho đơn hàng này.");
        return "redirect:/admin/orders";
    }

    // Admin từ chối yêu cầu hoàn tiền - trả đơn về lại PAID
    @PostMapping("/orders/{id}/reject-refund")
    public String rejectRefund(@PathVariable("id") String id, RedirectAttributes redirectAttributes) {
        boolean success = orderApiService.rejectRefund(id);
        redirectAttributes.addFlashAttribute(success ? "orderMessage" : "orderError",
                success ? "Đã từ chối yêu cầu hoàn tiền." : "Không thể từ chối yêu cầu này.");
        return "redirect:/admin/orders";
    }

    // Hiển thị danh sách Tồn kho - lấy toàn bộ sản phẩm từ catalog-service, join số lượng
    // tồn kho từ inventory-service (sản phẩm chưa có dòng tồn kho thì mặc định hiện 0)
    @GetMapping("/inventory")
    public String manageInventory(Model model) {
        List<ProductDTO> products = catalogApiService.getAllProducts();
        List<StockDTO> stocks = inventoryApiService.getAllStocks();
        Map<String, Integer> quantityByProduct = stocks.stream()
                .collect(Collectors.toMap(StockDTO::getProductId, StockDTO::getQuantity, (a, b) -> a));

        List<StockDTO> rows = products.stream().map(p -> {
            StockDTO row = new StockDTO();
            row.setProductId(p.getProductId().toString());
            row.setProductName(p.getProductName());
            row.setQuantity(quantityByProduct.getOrDefault(p.getProductId().toString(), 0));
            return row;
        }).collect(Collectors.toList());

        model.addAttribute("stocks", rows);
        return "admin/inventory";
    }

    @PostMapping("/inventory/save")
    public String saveInventory(@RequestParam("productId") String productId,
                                @RequestParam("quantity") Integer quantity,
                                RedirectAttributes redirectAttributes) {
        boolean success = inventoryApiService.updateStock(productId, quantity);
        redirectAttributes.addFlashAttribute(success ? "inventoryMessage" : "inventoryError",
                success ? "Đã cập nhật tồn kho." : "Không thể cập nhật tồn kho.");
        return "redirect:/admin/inventory";
    }

    // Form đổi mật khẩu cho chính tài khoản Admin/Staff đang đăng nhập - tái sử dụng
    // AuthApiService.changePassword() (gọi auth-service, xác thực bằng username trong session "adminUsername").
    @GetMapping("/change-password")
    public String showChangePasswordForm() {
        return "admin/change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam("oldPassword") String oldPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 HttpSession session, RedirectAttributes redirectAttributes) {
        String username = (String) session.getAttribute("adminUsername");
        boolean success = authApiService.changePassword(username, oldPassword, newPassword);
        redirectAttributes.addFlashAttribute(success ? "passwordMessage" : "passwordError",
                success ? "Đổi mật khẩu thành công!" : "Mật khẩu cũ không đúng!");
        return "redirect:/admin/change-password";
    }
}