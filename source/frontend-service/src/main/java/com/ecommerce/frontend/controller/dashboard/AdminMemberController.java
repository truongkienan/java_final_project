package com.ecommerce.frontend.controller.dashboard;

import com.ecommerce.frontend.service.CustomerApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/dashboard/members")
public class AdminMemberController {

    @Autowired
    private CustomerApiService customerApiService;

    @GetMapping
    public String manageMembers(Model model) {
        model.addAttribute("members", customerApiService.getAllMembers());
        return "dashboard/members";
    }
}