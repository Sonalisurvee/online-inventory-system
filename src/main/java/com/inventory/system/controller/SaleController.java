package com.inventory.system.controller;

import com.inventory.system.model.Sale;
import com.inventory.system.model.User;
import com.inventory.system.service.ProductService;
import com.inventory.system.service.SaleService;
import com.inventory.system.service.StoreService;
import com.inventory.system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/sales")
public class SaleController {

    @Autowired
    private SaleService saleService;

    @Autowired
    private ProductService productService;

    @Autowired
    private StoreService storeService;

    @Autowired
    private UserService userService;

    // List all sales
    @GetMapping
    public String listSales(Model model) {
        List<Sale> sales = saleService.getAllSales();
        model.addAttribute("sales", sales);
        model.addAttribute("title", "Sales Management");
        return "sales/list";
    }

    // Show form to create new sale
    @GetMapping("/new")
    public String showNewForm(Model model) {
        Sale sale = new Sale();
        sale.setSaleDate(LocalDate.now());
        // Generate invoice number automatically
        sale.setInvoiceNo(saleService.generateInvoiceNumber());
        model.addAttribute("sale", sale);
        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("stores", storeService.getAllStores());
        model.addAttribute("title", "New Sale");
        return "sales/form";
    }

    // Save sale
    @PostMapping("/save")
    public String saveSale(@ModelAttribute Sale sale,
                           @RequestParam Long productId,
                           @RequestParam Long storeId,
                           RedirectAttributes redirectAttributes) {
        try {
            // Get current user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            User currentUser = userService.getUserByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            sale.setUser(currentUser);

            // Set relationships
            sale.setProduct(productService.getProductById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found")));
            sale.setStore(storeService.getStoreById(storeId)
                    .orElseThrow(() -> new RuntimeException("Store not found")));

            if (sale.getSaleDate() == null) {
                sale.setSaleDate(LocalDate.now());
            }

            saleService.saveSale(sale);
            redirectAttributes.addFlashAttribute("success", "Sale recorded successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/sales";
    }

    // View sale details
    @GetMapping("/view/{id}")
    public String viewSale(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Sale sale = saleService.getSaleById(id)
                    .orElseThrow(() -> new RuntimeException("Sale not found"));
            model.addAttribute("sale", sale);
            model.addAttribute("title", "Sale Details");
            return "sales/view";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Sale not found!");
            return "redirect:/sales";
        }
    }

    // Print invoice (optional)
    @GetMapping("/invoice/{id}")
    public String printInvoice(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Sale sale = saleService.getSaleById(id)
                    .orElseThrow(() -> new RuntimeException("Sale not found"));
            model.addAttribute("sale", sale);
            return "sales/invoice";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Sale not found!");
            return "redirect:/sales";
        }
    }

    // Delete sale (optional – may not be allowed in real world)
    @GetMapping("/delete/{id}")
    public String deleteSale(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            saleService.deleteSale(id);
            redirectAttributes.addFlashAttribute("success", "Sale deleted!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/sales";
    }

    // Search sales by invoice number
    @GetMapping("/search")
    public String searchSales(@RequestParam String keyword, Model model) {
        List<Sale> sales = saleService.searchSales(keyword);
        model.addAttribute("sales", sales);
        model.addAttribute("keyword", keyword);
        model.addAttribute("title", "Search Results");
        return "sales/list";
    }

}