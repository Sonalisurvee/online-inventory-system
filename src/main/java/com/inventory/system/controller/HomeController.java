package com.inventory.system.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "home";
    }

    // REMOVE THIS METHOD - it conflicts with LoginController
    // @GetMapping("/login")
    // public String login() {
    //     return "login";
    // }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }
}