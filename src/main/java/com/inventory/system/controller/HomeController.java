package com.inventory.system.controller;

import com.inventory.system.model.Store;
import com.inventory.system.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

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

    @Autowired
    private SaleService saleService;

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
        int expiringCount = productService.getExpiringProducts(30).size();

        model.addAttribute("totalStores", stores.size());
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("totalCategories", totalCategories);
        model.addAttribute("lowStockCount", lowStockCount);
        model.addAttribute("expiringCount", expiringCount);
        model.addAttribute("stores", stores);

        // Prepare store summaries (will be used for stock value chart)
        List<Map<String, Object>> storeSummaries = new ArrayList<>();

        // If we have stores and products, create store summaries
        if (stores.size() > 0 && totalProducts > 0) {
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

            model.addAttribute("lowStockItems", inventoryService.getAllLowStockItems());
            model.addAttribute("recentActivities", new ArrayList<>()); // placeholder
        }


        // Add store summaries to model (always, even if empty)
        model.addAttribute("storeSummaries", storeSummaries);

        // Charts data (only if there are stores and products)
        if (stores.size() > 0 && totalProducts > 0) {
            model.addAttribute("monthlySales", saleService.getMonthlySales(6));
            model.addAttribute("lowStockByCategory", inventoryService.getLowStockCountByCategory());
        } else {
            // Provide empty maps to avoid null in template
            model.addAttribute("monthlySales", new HashMap<String, Double>());
            model.addAttribute("lowStockByCategory", new HashMap<String, Integer>());
        }

        return "dashboard";
    }
}