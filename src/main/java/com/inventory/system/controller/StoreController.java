package com.inventory.system.controller;

import com.inventory.system.model.Store;
import com.inventory.system.model.User;
import com.inventory.system.model.UserRole;
import com.inventory.system.service.StoreService;
import com.inventory.system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/stores")
public class StoreController {

    @Autowired
    private StoreService storeService;

    @Autowired
    private UserService userService;

    // List all stores
    @GetMapping
    public String listStores(Model model) {
        List<Store> stores = storeService.getAllStores();
        model.addAttribute("stores", stores);
        model.addAttribute("title", "Store Management");
        return "stores/list";
    }

    // Show form to create new store
    @GetMapping("/new")
    public String showNewForm(Model model) {
        model.addAttribute("store", new Store());
        model.addAttribute("managers", userService.getUsersByRole(UserRole.MANAGER));
        model.addAttribute("selectedManagerId", null);
        model.addAttribute("title", "Add New Store");
        return "stores/form";
    }

    // Show form to edit store
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model,
                               RedirectAttributes redirectAttributes) {
        try {
            System.out.println("=== EDITING STORE WITH ID: " + id + " ===");

            Store store = storeService.getStoreById(id)
                    .orElseThrow(() -> new RuntimeException("Store not found with id: " + id));

            System.out.println("Store found: " + store.getName());

            List<User> managers = userService.getUsersByRole(UserRole.MANAGER);
            System.out.println("Managers found: " + managers.size());

            Long selectedManagerId = store.getManager() != null ? store.getManager().getId() : null;

            model.addAttribute("store", store);
            model.addAttribute("managers", managers);
            model.addAttribute("selectedManagerId", selectedManagerId);
            model.addAttribute("title", "Edit Store");

            return "stores/form";
        } catch (Exception e) {
            System.err.println("ERROR in edit: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error loading store: " + e.getMessage());
            return "redirect:/stores";
        }
    }

    // Save new store - UPDATED to use managerId
    @PostMapping("/save")
    public String saveStore(@ModelAttribute Store store,
                            @RequestParam(required = false) Long managerId,
                            RedirectAttributes redirectAttributes) {
        try {
            System.out.println("=== SAVING STORE ===");
            System.out.println("Store ID: " + store.getId());
            System.out.println("Store Name: " + store.getName());
            System.out.println("Manager ID from form: " + managerId);

            // Set the manager if managerId is provided
            if (managerId != null) {
                User manager = userService.getUserById(managerId).orElse(null);
                store.setManager(manager);
            } else {
                store.setManager(null);
            }

            if (store.getId() == null) {
                // New store - check if name exists
                if (storeService.storeExists(store.getName())) {
                    redirectAttributes.addFlashAttribute("error",
                            "Store name already exists!");
                    return "redirect:/stores/new";
                }
            }

            Store savedStore = storeService.saveStore(store);
            System.out.println("Store saved with ID: " + savedStore.getId());

            redirectAttributes.addFlashAttribute("success",
                    "Store saved successfully!");
        } catch (Exception e) {
            System.err.println("Error saving store: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error",
                    "Error saving store: " + e.getMessage());
            return "redirect:/stores/edit/" + (store.getId() != null ? store.getId() : "");
        }
        return "redirect:/stores";
    }

    // Delete store
    @GetMapping("/delete/{id}")
    public String deleteStore(@PathVariable Long id,
                              RedirectAttributes redirectAttributes) {
        try {
            storeService.deleteStore(id);
            redirectAttributes.addFlashAttribute("success",
                    "Store deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Error deleting store: " + e.getMessage());
        }
        return "redirect:/stores";
    }

    // View store details
    @GetMapping("/view/{id}")
    public String viewStore(@PathVariable Long id, Model model,
                            RedirectAttributes redirectAttributes) {
        try {
            Store store = storeService.getStoreById(id)
                    .orElseThrow(() -> new RuntimeException("Store not found"));
            model.addAttribute("store", store);
            model.addAttribute("title", "Store Details");
            return "stores/view";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Store not found!");
            return "redirect:/stores";
        }
    }

    // Search stores
    @GetMapping("/search")
    public String searchStores(@RequestParam String keyword, Model model) {
        List<Store> stores = storeService.searchStores(keyword);
        model.addAttribute("stores", stores);
        model.addAttribute("keyword", keyword);
        model.addAttribute("title", "Search Results");
        return "stores/list";
    }

    // Assign manager
    @GetMapping("/assign-manager/{id}")
    public String showAssignManagerForm(@PathVariable Long id, Model model,
                                        RedirectAttributes redirectAttributes) {
        try {
            Store store = storeService.getStoreById(id)
                    .orElseThrow(() -> new RuntimeException("Store not found with id: " + id));
            model.addAttribute("store", store);
            model.addAttribute("managers", userService.getUsersByRole(UserRole.MANAGER));
            model.addAttribute("title", "Assign Manager");
            return "stores/assign-manager";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Store not found!");
            return "redirect:/stores";
        }
    }

    @PostMapping("/assign-manager/{id}")
    public String assignManager(@PathVariable Long id,
                                @RequestParam(required = false) Long managerId,
                                RedirectAttributes redirectAttributes) {
        try {
            System.out.println("=== ASSIGNING MANAGER ===");
            System.out.println("Store ID: " + id);
            System.out.println("Manager ID: " + managerId);

            if (managerId == null) {
                // Remove manager
                Store store = storeService.getStoreById(id).orElse(null);
                if (store != null) {
                    store.setManager(null);
                    storeService.saveStore(store);
                    redirectAttributes.addFlashAttribute("success", "Manager removed successfully!");
                }
            } else {
                storeService.assignManager(id, managerId);
                redirectAttributes.addFlashAttribute("success", "Manager assigned successfully!");
            }
        } catch (Exception e) {
            System.err.println("Error assigning manager: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error assigning manager: " + e.getMessage());
        }
        return "redirect:/stores/view/" + id;
    }
}