package com.inventory.system.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "home"; // This will look for home.html in templates folder
    }

    @GetMapping("/login")
    public String login() {
        return "login"; // This will look for login.html in templates folder
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard"; // This will look for dashboard.html in templates folder
    }
}