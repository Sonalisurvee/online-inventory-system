package com.inventory.system.controller;

import com.inventory.system.model.Product;
import com.inventory.system.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/expiry")
public class ExpiryController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public String expiryReport(@RequestParam(defaultValue = "30") int days, Model model) {
        List<Product> expiringProducts = productService.getExpiringProducts(days);
        LocalDate today = LocalDate.now();

        // Compute days left for each product
        List<Integer> daysLeftList = new ArrayList<>();
        for (Product p : expiringProducts) {
            if (p.getExpiryDate() != null) {
                long daysLeft = ChronoUnit.DAYS.between(today, p.getExpiryDate());
                daysLeftList.add((int) daysLeft);
            } else {
                daysLeftList.add(null);
            }
        }

        model.addAttribute("products", expiringProducts);
        model.addAttribute("daysLeftList", daysLeftList);
        model.addAttribute("days", days);
        model.addAttribute("title", "Expiry Alerts");
        return "expiry/list";
    }
}