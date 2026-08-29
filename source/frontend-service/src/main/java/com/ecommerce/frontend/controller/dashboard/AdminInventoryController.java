package com.ecommerce.frontend.controller.dashboard;

import com.ecommerce.frontend.dto.ProductDTO;
import com.ecommerce.frontend.dto.StockDTO;
import com.ecommerce.frontend.service.CatalogApiService;
import com.ecommerce.frontend.service.InventoryApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/dashboard/inventory")
public class AdminInventoryController {

    @Autowired
    private CatalogApiService catalogApiService;
    @Autowired
    private InventoryApiService inventoryApiService;

    // Hien thi danh sach Ton kho - lay toan bo san pham tu catalog-service, join so luong
    // ton kho tu inventory-service (san pham chua co dong ton kho thi mac dinh hien 0)
    @GetMapping
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
        return "dashboard/inventory";
    }

    @PostMapping("/save")
    public String saveInventory(@RequestParam("productId") String productId,
                                @RequestParam("quantity") Integer quantity,
                                RedirectAttributes redirectAttributes) {
        boolean success = inventoryApiService.updateStock(productId, quantity);
        redirectAttributes.addFlashAttribute(success ? "inventoryMessage" : "inventoryError",
                success ? "Đã cập nhật tồn kho." : "Không thể cập nhật tồn kho.");
        return "redirect:/dashboard/inventory";
    }
}