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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/expiry")
public class ExpiryController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public String expiryReport(@RequestParam(defaultValue = "30") int days, Model model) {
        List<Product> expiringProducts = productService.getExpiringProducts(days);
        LocalDate today = LocalDate.now();

        // Create a list of maps containing product and its days left
        List<Map<String, Object>> productsWithDetails = new ArrayList<>();

        for (Product product : expiringProducts) {
            Map<String, Object> item = new HashMap<>();
            item.put("product", product);

            if (product.getExpiryDate() != null) {
                long daysLeft = ChronoUnit.DAYS.between(today, product.getExpiryDate());
                item.put("daysLeft", daysLeft);
            } else {
                item.put("daysLeft", null);
            }

            productsWithDetails.add(item);
        }

        model.addAttribute("productsWithDetails", productsWithDetails);
        model.addAttribute("days", days);
        model.addAttribute("title", "Expiry Alerts");
        return "expiry/list";
    }
}