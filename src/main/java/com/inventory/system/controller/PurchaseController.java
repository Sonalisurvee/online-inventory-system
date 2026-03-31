package com.inventory.system.controller;

import com.inventory.system.model.Purchase;
import com.inventory.system.service.ProductService;
import com.inventory.system.service.PurchaseService;
import com.inventory.system.service.StoreService;
import com.inventory.system.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.inventory.system.model.User;
import com.inventory.system.service.UserService;


import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/purchases")
public class PurchaseController {

    @Autowired
    private PurchaseService purchaseService;

    @Autowired
    private SupplierService supplierService;

    @Autowired
    private ProductService productService;

    @Autowired
    private StoreService storeService;

    @Autowired
    private UserService userService;

    // List all purchases
    @GetMapping
    public String listPurchases(Model model) {
        List<Purchase> purchases = purchaseService.getAllPurchases();
        model.addAttribute("purchases", purchases);
        model.addAttribute("title", "Purchase Management");
        return "purchases/list";
    }

    // Show form to create new purchase
    @GetMapping("/new")
    public String showNewForm(Model model) {
        Purchase purchase = new Purchase();
        purchase.setPurchaseDate(LocalDate.now()); // Set today's date as default
        model.addAttribute("purchase", purchase);
        model.addAttribute("suppliers", supplierService.getAllSuppliers());
        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("stores", storeService.getAllStores());
        model.addAttribute("title", "New Purchase");
        return "purchases/form";
    }

    // Save purchase
    @PostMapping("/save")
    public String savePurchase(@ModelAttribute Purchase purchase,
                               @RequestParam Long supplierId,
                               @RequestParam Long productId,
                               @RequestParam Long storeId,
                               RedirectAttributes redirectAttributes) {
        try {
            // Get currently logged-in user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            User currentUser = userService.getUserByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Set user on purchase
            purchase.setUser(currentUser);

            // Set relationships
            purchase.setSupplier(supplierService.getSupplierById(supplierId)
                    .orElseThrow(() -> new RuntimeException("Supplier not found")));
            purchase.setProduct(productService.getProductById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found")));
            purchase.setStore(storeService.getStoreById(storeId)
                    .orElseThrow(() -> new RuntimeException("Store not found")));

            // Set default date if not provided
            if (purchase.getPurchaseDate() == null) {
                purchase.setPurchaseDate(LocalDate.now());
            }

            purchaseService.savePurchase(purchase);
            redirectAttributes.addFlashAttribute("success", "Purchase recorded successfully!");
        } catch (Exception e) {
            e.printStackTrace(); // for debugging
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/purchases";
    }

    // View purchase details
    @GetMapping("/view/{id}")
    public String viewPurchase(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Purchase purchase = purchaseService.getPurchaseById(id)
                    .orElseThrow(() -> new RuntimeException("Purchase not found"));
            model.addAttribute("purchase", purchase);
            model.addAttribute("title", "Purchase Details");
            return "purchases/view";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Purchase not found!");
            return "redirect:/purchases";
        }
    }

    // Delete purchase (optional – might not be allowed in real world)
    @GetMapping("/delete/{id}")
    public String deletePurchase(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            purchaseService.deletePurchase(id);
            redirectAttributes.addFlashAttribute("success", "Purchase deleted!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/purchases";
    }

    // Search purchases by invoice number
    @GetMapping("/search")
    public String searchPurchases(@RequestParam String keyword, Model model) {
        List<Purchase> purchases = purchaseService.searchPurchases(keyword);
        model.addAttribute("purchases", purchases);
        model.addAttribute("keyword", keyword);
        model.addAttribute("title", "Search Results");
        return "purchases/list";
    }
}