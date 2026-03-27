package com.inventory.system.controller;

import com.inventory.system.model.*;
import com.inventory.system.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.inventory.system.model.Inventory;

import java.util.List;

@Controller
@RequestMapping("/transfers")
public class StockTransferController {

    @Autowired
    private StockTransferService transferService;

    @Autowired
    private ProductService productService;

    @Autowired
    private StoreService storeService;

    @Autowired
    private UserService userService;

    @Autowired
    private InventoryService inventoryService;

    // List all transfers
    @GetMapping
    public String listTransfers(@RequestParam(required = false) String status, Model model) {
        List<StockTransfer> transfers;
        if (status != null && !status.isEmpty()) {
            transfers = transferService.getTransfersByStatus(status);
        } else {
            transfers = transferService.getAllTransfers();
        }
        model.addAttribute("transfers", transfers);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("title", "Stock Transfers");
        return "transfers/list";
    }

    // Show form to request new transfer
    @GetMapping("/new")
    public String showNewForm(@RequestParam(required = false) Long productId,
                              @RequestParam(required = false) Long fromStoreId,
                              @RequestParam(required = false) Long toStoreId,
                              Model model) {
        StockTransfer transfer = new StockTransfer();
        transfer.setTransferNo(transferService.generateTransferNumber());
        model.addAttribute("transfer", transfer);
        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("stores", storeService.getAllStores());
        model.addAttribute("selectedProductId", productId);
        model.addAttribute("selectedFromStoreId", fromStoreId);
        model.addAttribute("selectedToStoreId", toStoreId);
        model.addAttribute("title", "Request Transfer");
        return "transfers/form";
    }

    // Save transfer request (status = PENDING)
    @PostMapping("/save")
    public String saveTransfer(@ModelAttribute StockTransfer transfer,
                               @RequestParam Long productId,
                               @RequestParam Long fromStoreId,
                               @RequestParam Long toStoreId,
                               RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            User currentUser = userService.getUserByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Product product = productService.getProductById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            Store fromStore = storeService.getStoreById(fromStoreId)
                    .orElseThrow(() -> new RuntimeException("Source store not found"));
            Store toStore = storeService.getStoreById(toStoreId)
                    .orElseThrow(() -> new RuntimeException("Destination store not found"));

            // Validation 1: Different stores
            if (fromStoreId.equals(toStoreId)) {
                redirectAttributes.addFlashAttribute("error", "Source and destination stores cannot be the same.");
                return "redirect:/transfers/new?productId=" + productId + "&fromStoreId=" + fromStoreId + "&toStoreId=" + toStoreId;
            }

            // Validation 2: Stock availability
            boolean available = inventoryService.isProductAvailable(productId, fromStoreId, transfer.getQuantity());
            if (!available) {
                int currentStock = inventoryService.getInventoryByProductAndStore(productId, fromStoreId)
                        .map(Inventory::getQuantity).orElse(0);
                redirectAttributes.addFlashAttribute("error",
                        "Insufficient stock in source store! Available: " + currentStock);
                return "redirect:/transfers/new?productId=" + productId + "&fromStoreId=" + fromStoreId + "&toStoreId=" + toStoreId;
            }

            transfer.setProduct(product);
            transfer.setFromStore(fromStore);
            transfer.setToStore(toStore);
            transfer.setRequestedBy(currentUser);

            transferService.createTransfer(transfer);
            redirectAttributes.addFlashAttribute("success", "Transfer request submitted! Awaiting approval.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
            return "redirect:/transfers/new?productId=" + productId + "&fromStoreId=" + fromStoreId + "&toStoreId=" + toStoreId;
        }
        return "redirect:/transfers";
    }

    // View transfer details
    @GetMapping("/view/{id}")
    public String viewTransfer(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            StockTransfer transfer = transferService.getTransferById(id)
                    .orElseThrow(() -> new RuntimeException("Transfer not found"));
            model.addAttribute("transfer", transfer);
            model.addAttribute("title", "Transfer Details");
            return "transfers/view";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Transfer not found!");
            return "redirect:/transfers";
        }
    }

    // Approve transfer (manager only)
    @GetMapping("/approve/{id}")
    public String approveTransfer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            User approver = userService.getUserByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Check if user has MANAGER or ADMIN role
            if (!(approver.getRole().name().equals("MANAGER") || approver.getRole().name().equals("ADMIN"))) {
                redirectAttributes.addFlashAttribute("error", "Only managers can approve transfers");
                return "redirect:/transfers";
            }

            StockTransfer approved = transferService.approveTransfer(id, approver);
            // Optionally auto-complete after approval? Or require manual completion.
            // Let's provide a separate complete action.
            redirectAttributes.addFlashAttribute("success", "Transfer approved. Now you can complete it.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/transfers";
    }

    // Complete transfer (move stock)
    @GetMapping("/complete/{id}")
    public String completeTransfer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            transferService.completeTransfer(id);
            redirectAttributes.addFlashAttribute("success", "Transfer completed! Stock moved.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/transfers";
    }

    // Reject transfer
    @GetMapping("/reject/{id}")
    public String rejectTransfer(@PathVariable Long id, @RequestParam(required = false) String reason,
                                 RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            User approver = userService.getUserByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            transferService.rejectTransfer(id, approver, reason);
            redirectAttributes.addFlashAttribute("success", "Transfer rejected.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/transfers";
    }

    // Delete transfer (optional)
    @GetMapping("/delete/{id}")
    public String deleteTransfer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            transferService.deleteTransfer(id);
            redirectAttributes.addFlashAttribute("success", "Transfer deleted.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/transfers";
    }
}