package com.inventory.system.controller;

import com.inventory.system.model.Inventory;
import com.inventory.system.model.Product;
import com.inventory.system.model.Store;
import com.inventory.system.service.InventoryService;
import com.inventory.system.service.ProductService;
import com.inventory.system.service.StoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.HashSet;
import java.util.Set;

import java.util.List;

@Controller
@RequestMapping("/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private ProductService productService;

    @Autowired
    private StoreService storeService;

    // View inventory for a specific store
    @GetMapping("/store/{storeId}")
    public String viewStoreInventory(@PathVariable Long storeId, Model model) {
        Store store = storeService.getStoreById(storeId).orElse(null);
        if (store == null) {
            return "redirect:/inventory/stores";
        }

        List<Inventory> inventory = inventoryService.getInventoryByStoreId(storeId);
        double totalValue = inventoryService.getStoreStockValue(storeId);

        model.addAttribute("store", store);
        model.addAttribute("inventory", inventory);
        model.addAttribute("totalValue", totalValue);
        model.addAttribute("title", "Inventory - " + store.getName());
        return "inventory/store-inventory";
    }

    @GetMapping("/low-stock")
    public String viewLowStock(@RequestParam(required = false, defaultValue = "5") int threshold,
                               Model model) {
        List<Inventory> lowStockItems = inventoryService.getLowStockItemsByThreshold(threshold);

        // Calculate statistics
        Set<Long> storeIds = new HashSet<>();
        Set<Long> productIds = new HashSet<>();
        for (Inventory item : lowStockItems) {
            storeIds.add(item.getStore().getId());
            productIds.add(item.getProduct().getId());
        }

        model.addAttribute("inventory", lowStockItems);
        model.addAttribute("threshold", threshold);
        model.addAttribute("storeCount", storeIds.size());
        model.addAttribute("productCount", productIds.size());
        model.addAttribute("title", "Low Stock Alert (Threshold: " + threshold + ")");

        return "inventory/low-stock";
    }

    @GetMapping("/add-stock")
    public String showAddStockForm(@RequestParam(required = false) Long productId,
                                   @RequestParam(required = false) Long storeId,
                                   Model model) {

        List<Product> products = productService.getAllProducts();
        List<Store> stores = storeService.getAllStores();

        model.addAttribute("products", products);
        model.addAttribute("stores", stores);
        model.addAttribute("preselectedProductId", productId);
        model.addAttribute("preselectedStoreId", storeId);
        model.addAttribute("title", "Add Stock");

        return "inventory/add-stock";
    }

    // Process add stock
    @PostMapping("/add-stock")
    public String addStock(@RequestParam Long productId,
                           @RequestParam Long storeId,
                           @RequestParam int quantity,
                           RedirectAttributes redirectAttributes) {
        try {
            if (quantity <= 0) {
                redirectAttributes.addFlashAttribute("error", "Quantity must be positive!");
                return "redirect:/inventory/add-stock";
            }

            Inventory inventory = inventoryService.addStock(productId, storeId, quantity);
            if (inventory != null) {
                redirectAttributes.addFlashAttribute("success",
                        "Stock added successfully! New quantity: " + inventory.getQuantity());
            } else {
                redirectAttributes.addFlashAttribute("error", "Failed to add stock!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/inventory/store/" + storeId;
    }

    // Show form to remove stock
    @GetMapping("/remove-stock")
    public String showRemoveStockForm(@RequestParam(required = false) Long productId,
                                      @RequestParam(required = false) Long storeId,
                                      Model model) {

        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("stores", storeService.getAllStores());
        model.addAttribute("preselectedProductId", productId);
        model.addAttribute("preselectedStoreId", storeId);
        model.addAttribute("title", "Remove Stock");

        return "inventory/remove-stock";
    }

    // Process remove stock
    @PostMapping("/remove-stock")
    public String removeStock(@RequestParam Long productId,
                              @RequestParam Long storeId,
                              @RequestParam int quantity,
                              RedirectAttributes redirectAttributes) {
        try {
            if (quantity <= 0) {
                redirectAttributes.addFlashAttribute("error", "Quantity must be positive!");
                return "redirect:/inventory/remove-stock";
            }

            // Check if enough stock is available
            if (!inventoryService.isProductAvailable(productId, storeId, quantity)) {
                redirectAttributes.addFlashAttribute("error", "Insufficient stock!");
                return "redirect:/inventory/remove-stock";
            }

            Inventory inventory = inventoryService.removeStock(productId, storeId, quantity);
            if (inventory != null) {
                redirectAttributes.addFlashAttribute("success",
                        "Stock removed successfully! Remaining quantity: " + inventory.getQuantity());
            } else {
                redirectAttributes.addFlashAttribute("error", "Failed to remove stock!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/inventory/store/" + storeId;
    }

    // Show transfer stock form
    @GetMapping("/transfer")
    public String showTransferForm(@RequestParam(required = false) Long fromProductId,
                                   @RequestParam(required = false) Long fromStoreId,
                                   Model model) {

        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("stores", storeService.getAllStores());
        model.addAttribute("preselectedProductId", fromProductId);
        model.addAttribute("preselectedFromStoreId", fromStoreId);
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
                return "redirect:/inventory/transfer";
            }

            if (fromStoreId.equals(toStoreId)) {
                redirectAttributes.addFlashAttribute("error", "Source and destination stores must be different!");
                return "redirect:/inventory/transfer";
            }

            boolean success = inventoryService.transferStock(fromStoreId, toStoreId, productId, quantity);
            if (success) {
                redirectAttributes.addFlashAttribute("success",
                        quantity + " units transferred successfully!");
            } else {
                redirectAttributes.addFlashAttribute("error",
                        "Transfer failed! Check if source store has enough stock.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/inventory/store/" + fromStoreId;
    }

    // Update min/max levels
    @GetMapping("/update-levels/{id}")
    public String showUpdateLevelsForm(@PathVariable Long id, Model model,
                                       RedirectAttributes redirectAttributes) {
        try {
            Inventory inventory = inventoryService.getInventoryById(id)
                    .orElseThrow(() -> new RuntimeException("Inventory not found"));
            model.addAttribute("inventory", inventory);
            model.addAttribute("title", "Update Stock Levels");
            return "inventory/update-levels";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Inventory not found!");
            return "redirect:/inventory/stores";
        }
    }

    @PostMapping("/update-levels/{id}")
    public String updateLevels(@PathVariable Long id,
                               @RequestParam int minQuantity,
                               @RequestParam int maxQuantity,
                               RedirectAttributes redirectAttributes) {
        try {
            Inventory inventory = inventoryService.getInventoryById(id).orElse(null);

            if (inventory != null) {
                inventory.setMinQuantity(minQuantity);
                inventory.setMaxQuantity(maxQuantity);

                // Save using the new method
                inventoryService.updateInventory(inventory);

                Long storeId = inventory.getStore().getId();
                redirectAttributes.addFlashAttribute("success", "Stock levels updated successfully!");
                return "redirect:/store/" + storeId;
            } else {
                redirectAttributes.addFlashAttribute("error", "Inventory not found!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/inventory/stores";
    }

    @GetMapping("/custom-lowstock")
    public String customLowStock(@RequestParam(required = false, defaultValue = "5") int threshold,
                                 Model model) {
        List<Inventory> lowStockItems = inventoryService.getLowStockItemsByThreshold(threshold);

        // Get unique stores and products for statistics
        Set<Store> uniqueStores = new HashSet<>();
        Set<Product> uniqueProducts = new HashSet<>();
        for (Inventory item : lowStockItems) {
            uniqueStores.add(item.getStore());
            uniqueProducts.add(item.getProduct());
        }

        model.addAttribute("inventory", lowStockItems);
        model.addAttribute("threshold", threshold);
        model.addAttribute("stores", uniqueStores);
        model.addAttribute("products", uniqueProducts);
        model.addAttribute("title", "Custom Low Stock Report");

        return "inventory/custom-lowstock";
    }

}