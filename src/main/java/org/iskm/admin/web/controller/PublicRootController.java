package org.iskm.admin.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PublicRootController {

    @GetMapping("/")
    public String root() {
        return "redirect:login";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "/public/login.html";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "/public/dashboard.html";
    }
}
