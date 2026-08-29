package com.ecommerce.frontend.controller.dashboard;

import com.ecommerce.frontend.dto.CategoryDTO;
import com.ecommerce.frontend.service.CatalogApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/dashboard/categories")
public class CategoryController {

    @Autowired
    private CatalogApiService catalogApiService;

    // Hien thi danh sach Danh muc theo dung thu tu cay (cap 1 roi toi cac con cua no ngay sau), kem ten Danh muc cha
    @GetMapping
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
        return "dashboard/categories";
    }

    // Mo giao dien Them moi (Truyen 1 Object rong)
    @GetMapping("/new")
    public String createCategoryForm(Model model) {
        CategoryDTO newCategory = new CategoryDTO();
        newCategory.setActive(true);
        newCategory.setPosition(0);
        model.addAttribute("category", newCategory);
        model.addAttribute("parentCategories", getTopLevelCategories());
        return "dashboard/category-form";
    }

    // Mo giao dien Sua (Truyen Object lay tu DB)
    @GetMapping("/edit/{id}")
    public String editCategoryForm(@PathVariable("id") Short id, Model model) {
        model.addAttribute("category", catalogApiService.getCategoryById(id));
        model.addAttribute("parentCategories", getTopLevelCategories());
        return "dashboard/category-form";
    }

    // Chi lay danh muc cap 1 (parentId null) de lam danh sach lua chon "Danh muc cha"
    private List<CategoryDTO> getTopLevelCategories() {
        return catalogApiService.getAllCategories().stream()
                .filter(c -> c.getParentId() == null)
                .sorted(Comparator.comparing(c -> c.getPosition() == null ? 0 : c.getPosition()))
                .collect(Collectors.toList());
    }

    // Nhan du lieu tu Form gui len de Luu, sau do quay ve trang danh sach
    @PostMapping("/save")
    public String saveCategory(@ModelAttribute CategoryDTO categoryDTO) {
        catalogApiService.saveCategory(categoryDTO);
        return "redirect:/dashboard/categories";
    }

    @PostMapping("/delete/{id}")
    public String deleteCategory(@PathVariable("id") Short id) {
        catalogApiService.deleteCategory(id);
        return "redirect:/dashboard/categories";
    }
}