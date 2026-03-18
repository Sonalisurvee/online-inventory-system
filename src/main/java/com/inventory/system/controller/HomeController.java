package com.inventory.system.controller;

import com.inventory.system.model.Store;
import com.inventory.system.service.StoreService;
import com.inventory.system.service.ProductService;
import com.inventory.system.service.CategoryService;
import com.inventory.system.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@Controller
public class HomeController {

    @Autowired
    private StoreService storeService;

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private InventoryService inventoryService;

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Basic counts
        List<Store> stores = storeService.getAllStores();
        int totalProducts = productService.getAllProducts().size();
        int totalCategories = categoryService.getAllCategories().size();
        int lowStockCount = inventoryService.getAllLowStockItems().size();

        model.addAttribute("totalStores", stores.size());
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("totalCategories", totalCategories);
        model.addAttribute("lowStockCount", lowStockCount);

        // Always add stores list
        model.addAttribute("stores", stores);

        // If we have stores and products, create store summaries
        if (stores.size() > 0 && totalProducts > 0) {
            List<Map<String, Object>> storeSummaries = new ArrayList<>();

            for (Store store : stores) {
                Map<String, Object> summary = new HashMap<>();
                summary.put("id", store.getId());
                summary.put("name", store.getName());
                summary.put("status", store.getStatus());

                // Get inventory for this store
                var inventory = inventoryService.getInventoryByStoreId(store.getId());
                summary.put("productCount", inventory.size());
                summary.put("totalUnits", inventoryService.getTotalUnitsInStore(store.getId()));
                summary.put("totalValue", inventoryService.getStoreStockValue(store.getId()));
                summary.put("lowStockCount", inventoryService.getLowStockCount(store.getId()));

                storeSummaries.add(summary);
            }

            model.addAttribute("storeSummaries", storeSummaries);
            model.addAttribute("lowStockItems", inventoryService.getAllLowStockItems());

            // Mock recent activities (you can replace with actual data later)
            List<Map<String, String>> recentActivities = new ArrayList<>();
            model.addAttribute("recentActivities", recentActivities);
        }

        return "dashboard";
    }
}