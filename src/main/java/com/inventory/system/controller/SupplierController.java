package com.inventory.system.controller;

import com.inventory.system.model.Supplier;
import com.inventory.system.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/suppliers")
public class SupplierController {

    @Autowired
    private SupplierService supplierService;

    // List all suppliers
    @GetMapping
    public String listSuppliers(Model model) {
        List<Supplier> suppliers = supplierService.getAllSuppliers();
        model.addAttribute("suppliers", suppliers);
        model.addAttribute("title", "Supplier Management");
        return "suppliers/list";
    }

    // Show form to create new supplier
    @GetMapping("/new")
    public String showNewForm(Model model) {
        model.addAttribute("supplier", new Supplier());
        model.addAttribute("title", "Add New Supplier");
        return "suppliers/form";
    }

    // Save supplier (create or update)
    @PostMapping("/save")
    public String saveSupplier(@ModelAttribute Supplier supplier, RedirectAttributes redirectAttributes) {
        try {
            // For new supplier, check if name already exists
            if (supplier.getId() == null && supplierService.supplierExists(supplier.getName())) {
                redirectAttributes.addFlashAttribute("error", "Supplier name already exists!");
                return "redirect:/suppliers/new";
            }
            supplierService.saveSupplier(supplier);
            redirectAttributes.addFlashAttribute("success", "Supplier saved successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error saving supplier: " + e.getMessage());
        }
        return "redirect:/suppliers";
    }

    // Show edit form
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Supplier supplier = supplierService.getSupplierById(id)
                    .orElseThrow(() -> new RuntimeException("Supplier not found"));
            model.addAttribute("supplier", supplier);
            model.addAttribute("title", "Edit Supplier");
            return "suppliers/form";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Supplier not found!");
            return "redirect:/suppliers";
        }
    }

    // Delete supplier
    @GetMapping("/delete/{id}")
    public String deleteSupplier(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            supplierService.deleteSupplier(id);
            redirectAttributes.addFlashAttribute("success", "Supplier deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting supplier: " + e.getMessage());
        }
        return "redirect:/suppliers";
    }

    // View supplier details
    @GetMapping("/view/{id}")
    public String viewSupplier(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Supplier supplier = supplierService.getSupplierById(id)
                    .orElseThrow(() -> new RuntimeException("Supplier not found"));
            model.addAttribute("supplier", supplier);
            model.addAttribute("title", "Supplier Details");
            return "suppliers/view";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Supplier not found!");
            return "redirect:/suppliers";
        }
    }

    // Search suppliers
    @GetMapping("/search")
    public String searchSuppliers(@RequestParam String keyword, Model model) {
        List<Supplier> suppliers = supplierService.searchSuppliers(keyword);
        model.addAttribute("suppliers", suppliers);
        model.addAttribute("keyword", keyword);
        model.addAttribute("title", "Search Results");
        return "suppliers/list";
    }
}