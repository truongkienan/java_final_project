package com.ecommerce.frontend.controller.dashboard;

import com.ecommerce.frontend.dto.CategoryDTO;
import com.ecommerce.frontend.dto.UnitDTO;
import com.ecommerce.frontend.service.CatalogApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/dashboard/units")
public class UnitController {

    @Autowired
    private CatalogApiService catalogApiService;

    @GetMapping
    public String manageUnits(Model model) {
        model.addAttribute("units", catalogApiService.getUnits());
        return "dashboard/units";
    }

    @GetMapping("/new")
    public String createUnitForm(Model model) {
        model.addAttribute("unit", new UnitDTO());
        model.addAttribute("allCategories", catalogApiService.getAllCategories());
        return "dashboard/unit-form";
    }

    @GetMapping("/edit/{id}")
    public String editUnitForm(@PathVariable("id") Short id, Model model) {
        model.addAttribute("unit", catalogApiService.getUnitById(id));
        model.addAttribute("allCategories", catalogApiService.getAllCategories());
        return "dashboard/unit-form";
    }

    // Nhan categoryId rieng tu dropdown (giong ProductController) roi tu gan
    // vao UnitDTO.category truoc khi goi sang catalog-service.
    @PostMapping("/save")
    public String saveUnit(@ModelAttribute UnitDTO unitDTO, @RequestParam("categoryId") Short categoryId) {
        CategoryDTO category = new CategoryDTO();
        category.setCategoryId(categoryId);
        unitDTO.setCategory(category);
        catalogApiService.saveUnit(unitDTO);
        return "redirect:/dashboard/units";
    }

    @PostMapping("/delete/{id}")
    public String deleteUnit(@PathVariable("id") Short id) {
        catalogApiService.deleteUnit(id);
        return "redirect:/dashboard/units";
    }
}
