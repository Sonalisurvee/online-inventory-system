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

import java.util.List;

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
        System.out.println("Trying to return template: inventory/store-inventory");

        // Get inventory for this store
        List<Inventory> inventory = inventoryService.getInventoryByStoreId(storeId);

        // Calculate stats
        int totalProducts = inventory.size();
        double totalValue = inventoryService.getStoreStockValue(storeId);
        int totalUnits = inventoryService.getTotalUnitsInStore(storeId);
        int lowStockCount = inventoryService.getLowStockCount(storeId);

        // Get low stock items for alerts
        List<Inventory> lowStockItems = inventoryService.getLowStockItems(storeId);

        // Add data to model
        model.addAttribute("store", store);
        model.addAttribute("inventory", inventory);
        model.addAttribute("allStores", storeService.getAllStores());
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("totalValue", totalValue);
        model.addAttribute("totalUnits", totalUnits);
        model.addAttribute("lowStockCount", lowStockCount);
        model.addAttribute("lowStockItems", lowStockItems);
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

    // Show transfer stock form
    @GetMapping("/transfer")
    public String showTransferForm(@RequestParam(required = false) Long productId,
                                   @RequestParam(required = false) Long fromStoreId,
                                   Model model) {
        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("stores", storeService.getAllStores());
        model.addAttribute("selectedProductId", productId);
        model.addAttribute("selectedFromStoreId", fromStoreId);
        model.addAttribute("title", "Transfer Stock");
        return "inventory/transfer";
    }

    // Process transfer stock
    @PostMapping("/transfer")
    public String transferStock(@RequestParam Long productId,
                                @RequestParam Long fromStoreId,
                                @RequestParam Long toStoreId,
                                @RequestParam int quantity,
                                RedirectAttributes redirectAttributes) {
        try {
            if (quantity <= 0) {
                redirectAttributes.addFlashAttribute("error", "Quantity must be positive!");
                return "redirect:/stock/transfer";
            }

            if (fromStoreId.equals(toStoreId)) {
                redirectAttributes.addFlashAttribute("error", "Source and destination stores must be different!");
                return "redirect:/stock/transfer";
            }

            boolean success = inventoryService.transferStock(fromStoreId, toStoreId, productId, quantity);
            if (success) {
                redirectAttributes.addFlashAttribute("success",
                        quantity + " units transferred successfully!");
            } else {
                redirectAttributes.addFlashAttribute("error",
                        "Transfer failed! Check if source store has enough stock.");
            }

            // FIX THIS: Redirect to the source store inventory view page
            return "redirect:/stock/view/" + fromStoreId;  // Changed from /stock/store/

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
            return "redirect:/stock/transfer";
        }
    }

    // View low stock items across all stores
    @GetMapping("/low-stock")
    public String viewLowStock(Model model) {
        List<Inventory> lowStockItems = inventoryService.getAllLowStockItems();
        model.addAttribute("inventory", lowStockItems);
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

}