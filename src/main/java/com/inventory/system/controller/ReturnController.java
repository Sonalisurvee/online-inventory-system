package com.inventory.system.controller;

import com.inventory.system.model.Return;
import com.inventory.system.model.User;
import com.inventory.system.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.inventory.system.model.Sale;
import com.inventory.system.model.Return;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/returns")
public class ReturnController {

    @Autowired
    private ReturnService returnService;

    @Autowired
    private ProductService productService;

    @Autowired
    private StoreService storeService;

    @Autowired
    private SaleService saleService;

    @Autowired
    private UserService userService;

    // List all returns
    @GetMapping
    public String listReturns(Model model) {
        List<Return> returns = returnService.getAllReturns();
        model.addAttribute("returns", returns);
        model.addAttribute("title", "Returns Management");
        return "returns/list";
    }

    // Show form to create new return
    @GetMapping("/new")
    public String showNewForm(Model model) {
        Return returnObj = new Return();
        returnObj.setReturnDate(LocalDate.now());
        returnObj.setReturnNo(returnService.generateReturnNumber()); // generate for display
        model.addAttribute("returnObj", returnObj);
        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("stores", storeService.getAllStores());
        model.addAttribute("sales", saleService.getAllSales()); // optional, to link
        model.addAttribute("title", "New Return");
        return "returns/form";
    }

    // Save return
    @PostMapping("/save")
    public String saveReturn(@ModelAttribute("returnObj") Return returnObj,
                             @RequestParam Long saleId,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        try {
            // Get current user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            User currentUser = userService.getUserByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            returnObj.setUser(currentUser);

            Sale sale = saleService.getSaleById(saleId)
                    .orElseThrow(() -> new RuntimeException("Sale not found"));

            // Link return to sale, and set product/store from sale
            returnObj.setSale(sale);
            returnObj.setProduct(sale.getProduct());
            returnObj.setStore(sale.getStore());

            // Validate return quantity <= sold quantity
            if (returnObj.getQuantity() > sale.getQuantity()) {
                throw new RuntimeException("Cannot return more than sold quantity!");
            }

            returnService.saveReturn(returnObj);
            redirectAttributes.addFlashAttribute("success", "Return recorded successfully!");
            return "redirect:/returns";
        } catch (Exception e) {
            e.printStackTrace();
            // Keep the return object in model and show error on same page
            model.addAttribute("error", e.getMessage());
            model.addAttribute("returnObj", returnObj);
            // Re-fetch sales for the dropdown
            model.addAttribute("sales", saleService.getAllSales());
            model.addAttribute("title", "New Return");
            return "returns/form";
        }
    }

    // View return details
    @GetMapping("/view/{id}")
    public String viewReturn(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Return returnObj = returnService.getReturnById(id)
                    .orElseThrow(() -> new RuntimeException("Return not found"));
            model.addAttribute("returnObj", returnObj);
            model.addAttribute("title", "Return Details");
            return "returns/view";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Return not found!");
            return "redirect:/returns";
        }
    }

    // Delete return (optional)
    @GetMapping("/delete/{id}")
    public String deleteReturn(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            returnService.deleteReturn(id);
            redirectAttributes.addFlashAttribute("success", "Return deleted!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/returns";
    }

    // Search returns by return number
    @GetMapping("/search")
    public String searchReturns(@RequestParam String keyword, Model model) {
        List<Return> returns = returnService.searchReturns(keyword);
        model.addAttribute("returns", returns);
        model.addAttribute("keyword", keyword);
        model.addAttribute("title", "Search Results");
        return "returns/list";
    }

}