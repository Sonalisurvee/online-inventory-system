package com.inventory.system.controller;

import com.inventory.system.model.Inventory;
import com.inventory.system.model.Product;
import com.inventory.system.model.Store;
import com.inventory.system.service.InventoryService;
import com.inventory.system.service.ProductService;
import com.inventory.system.service.StoreService;
import com.inventory.system.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.Optional;
import com.inventory.system.dto.StockMovement;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/stock")  // Changed from /inventory to /stock
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private ProductService productService;

    @Autowired
    private StoreService storeService;

    @Autowired
    private CategoryService categoryService;

    // View inventory for a specific store - UPDATED URL
    @GetMapping("/view/{storeId}")
    public String viewStoreInventory(@PathVariable Long storeId,
                                     @RequestParam(required = false) String search,
                                     @RequestParam(required = false) Long category,
                                     @RequestParam(required = false) String status,
                                     Model model) {

        System.out.println("========== INVENTORY CONTROLLER ==========");
        System.out.println("Trying to view inventory for store ID: " + storeId);

        Store store = storeService.getStoreById(storeId).orElse(null);
        if (store == null) {
            System.out.println("Store not found with ID: " + storeId);
            return "redirect:/stores";
        }

        System.out.println("Store found: " + store.getName());

        // Get all inventory for this store
        List<Inventory> allInventory = inventoryService.getInventoryByStoreId(storeId);

        // Apply filters
        List<Inventory> filteredInventory = allInventory.stream()
                .filter(item -> {
                    // Search filter (product name)
                    if (search != null && !search.trim().isEmpty()) {
                        String productName = item.getProduct().getName().toLowerCase();
                        if (!productName.contains(search.toLowerCase())) {
                            return false;
                        }
                    }
                    // Category filter
                    if (category != null && category > 0) {
                        if (item.getProduct().getCategory() == null ||
                                !category.equals(item.getProduct().getCategory().getId())) {
                            return false;
                        }
                    }
                    // Status filter
                    if (status != null && !status.isEmpty()) {
                        boolean isLow = item.getQuantity() < item.getMinQuantity();
                        boolean isOut = item.getQuantity() == 0;
                        boolean isGood = !isLow && !isOut;

                        switch (status) {
                            case "low":
                                if (!isLow) return false;
                                break;
                            case "out":
                                if (!isOut) return false;
                                break;
                            case "good":
                                if (!isGood) return false;
                                break;
                        }
                    }
                    return true;
                })
                .collect(Collectors.toList());

        // Calculate stats based on filtered inventory? Or keep overall?
        // Usually stats remain overall, but we can also show filtered stats.
        // Let's keep overall stats for now.
        int totalProducts = allInventory.size();
        double totalValue = inventoryService.getStoreStockValue(storeId);
        int totalUnits = inventoryService.getTotalUnitsInStore(storeId);
        int lowStockCount = inventoryService.getLowStockCount(storeId);

        // Get low stock items for alerts (unfiltered)
        List<Inventory> lowStockItems = inventoryService.getLowStockItems(storeId);

        // Add data to model
        model.addAttribute("store", store);
        model.addAttribute("inventory", filteredInventory); // Use filtered list
        model.addAttribute("allStores", storeService.getAllStores());
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("totalValue", totalValue);
        model.addAttribute("totalUnits", totalUnits);
        model.addAttribute("lowStockCount", lowStockCount);
        model.addAttribute("lowStockItems", lowStockItems);
        model.addAttribute("search", search); // pass back for input value
        model.addAttribute("categoryId", category); // pass back for dropdown selection
        model.addAttribute("status", status); // pass back for dropdown selection
        model.addAttribute("title", "Inventory - " + store.getName());

        return "inventory/store-inventory";
    }

    // Show add stock form
    @GetMapping("/add")
    public String showAddStockForm(@RequestParam(required = false) Long productId,
                                   @RequestParam(required = false) Long storeId,
                                   Model model) {
        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("stores", storeService.getAllStores());
        model.addAttribute("selectedProductId", productId);
        model.addAttribute("selectedStoreId", storeId);
        model.addAttribute("title", "Add Stock");
        return "inventory/add-stock";
    }

    // Process add stock
    @PostMapping("/add")
    public String addStock(@RequestParam Long productId,
                           @RequestParam Long storeId,
                           @RequestParam int quantity,
                           RedirectAttributes redirectAttributes) {
        try {
            if (quantity <= 0) {
                redirectAttributes.addFlashAttribute("error", "Quantity must be positive!");
                return "redirect:/stock/add";
            }

            Inventory inventory = inventoryService.addStock(productId, storeId, quantity);
            redirectAttributes.addFlashAttribute("success",
                    "Stock added successfully! New quantity: " + inventory.getQuantity());

            // FIX THIS: Redirect to the store inventory view page
            return "redirect:/stock/view/" + storeId;  // Changed from /stock/store/

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
            return "redirect:/stock/add";
        }
    }

    // Show remove stock form
    @GetMapping("/remove")
    public String showRemoveStockForm(@RequestParam(required = false) Long productId,
                                      @RequestParam(required = false) Long storeId,
                                      Model model) {
        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("stores", storeService.getAllStores());
        model.addAttribute("selectedProductId", productId);
        model.addAttribute("selectedStoreId", storeId);
        model.addAttribute("title", "Remove Stock");
        return "inventory/remove-stock";
    }

    // Process remove stock
    @PostMapping("/remove")
    public String removeStock(@RequestParam Long productId,
                              @RequestParam Long storeId,
                              @RequestParam int quantity,
                              RedirectAttributes redirectAttributes) {
        try {
            if (quantity <= 0) {
                redirectAttributes.addFlashAttribute("error", "Quantity must be positive!");
                return "redirect:/stock/remove";
            }

            // Check if enough stock is available
            if (!inventoryService.isProductAvailable(productId, storeId, quantity)) {
                redirectAttributes.addFlashAttribute("error", "Insufficient stock!");
                return "redirect:/stock/remove";
            }

            Inventory inventory = inventoryService.removeStock(productId, storeId, quantity);
            redirectAttributes.addFlashAttribute("success",
                    "Stock removed successfully! Remaining quantity: " + inventory.getQuantity());

            // FIX THIS: Redirect to the store inventory view page
            return "redirect:/stock/view/" + storeId;  // Changed from /stock/store/

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
            return "redirect:/stock/remove";
        }
    }

    // View low stock items across all stores
    @GetMapping("/low-stock")
    public String viewLowStock(@RequestParam(required = false) Integer threshold, Model model) {
        int actualThreshold = (threshold != null) ? threshold : 5; // default to 5
        List<Inventory> lowStockItems = inventoryService.getInventoryBelowThreshold(actualThreshold);

        // Calculate stats
        long storeCount = lowStockItems.stream().map(i -> i.getStore().getId()).distinct().count();
        long productCount = lowStockItems.stream().map(i -> i.getProduct().getId()).distinct().count();

        model.addAttribute("inventory", lowStockItems);
        model.addAttribute("threshold", actualThreshold);
        model.addAttribute("storeCount", storeCount);
        model.addAttribute("productCount", productCount);
        model.addAttribute("title", "Low Stock Alert");

        return "inventory/low-stock";
    }

    // Update min/max levels
    @GetMapping("/levels/{id}")
    public String showUpdateLevelsForm(@PathVariable Long id, Model model) {
        Inventory inventory = inventoryService.getInventoryById(id).orElse(null);
        if (inventory == null) {
            return "redirect:/stores";
        }
        model.addAttribute("inventory", inventory);
        model.addAttribute("title", "Update Stock Levels");
        return "inventory/update-levels";
    }

    @PostMapping("/levels/{id}")
    public String updateLevels(@PathVariable Long id,
                               @RequestParam int minQuantity,
                               @RequestParam int maxQuantity,
                               RedirectAttributes redirectAttributes) {
        try {
            Inventory inventory = inventoryService.updateMinMaxLevels(id, minQuantity, maxQuantity);
            redirectAttributes.addFlashAttribute("success", "Stock levels updated!");
            return "redirect:/stock/store/" + inventory.getStore().getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
            return "redirect:/stock/levels/" + id;
        }
    }

    // Add this test method to InventoryController.java
    @GetMapping("/test")
    @ResponseBody
    public String test() {
        return "InventoryController is working! URL pattern: /stock/test";
    }

    @GetMapping("/test-page")
    public String testPage() {
        System.out.println("========== TEST PAGE ==========");
        System.out.println("Test page accessed");
        return "inventory/test";
    }


    @GetMapping("/api/stock")
    @ResponseBody
    public ResponseEntity<?> getCurrentStock(@RequestParam Long productId, @RequestParam Long storeId) {
        try {
            Optional<Inventory> inventory = inventoryService.getInventoryByProductAndStore(productId, storeId);
            if (inventory.isPresent()) {
                return ResponseEntity.ok(inventory.get().getQuantity());
            } else {
                return ResponseEntity.ok(0); // No inventory record means zero stock
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching stock: " + e.getMessage());
        }
    }

    @GetMapping("/history/{id}")
    public String viewHistory(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Inventory inventory = null; // declare outside
        try {
            inventory = inventoryService.getInventoryById(id)
                    .orElseThrow(() -> new RuntimeException("Inventory not found"));

            List<StockMovement> movements = inventoryService.getStockMovements(
                    inventory.getProduct().getId(),
                    inventory.getStore().getId()
            );

            model.addAttribute("inventory", inventory);
            model.addAttribute("movements", movements);
            model.addAttribute("title", "Stock History - " + inventory.getProduct().getName());

            return "inventory/history";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error loading history: " + e.getMessage());
            // Use the inventory variable safely (it may be null if exception occurred before assignment)
            Long storeId = (inventory != null) ? inventory.getStore().getId() : null;
            return "redirect:/stock/view/" + (storeId != null ? storeId : "");
        }
    }

}