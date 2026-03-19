package com.inventory.system.controller;

import com.inventory.system.service.InventoryService;
import com.inventory.system.service.PurchaseService;
import com.inventory.system.service.SaleService;
import com.inventory.system.service.StoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/reports")
public class ReportController {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private SaleService saleService;

    @Autowired
    private PurchaseService purchaseService;

    @Autowired
    private StoreService storeService;

    // Main reports dashboard
    @GetMapping
    public String reportsDashboard(Model model) {
        model.addAttribute("stores", storeService.getAllStores());
        return "reports/dashboard";
    }

    // Stock value report
    @GetMapping("/stock-value")
    public String stockValueReport(Model model) {
        var stores = storeService.getAllStores();
        Map<String, Object> reportData = new HashMap<>();
        for (var store : stores) {
            Map<String, Object> storeData = new HashMap<>();
            storeData.put("totalProducts", inventoryService.getTotalProductsInStore(store.getId()));
            storeData.put("totalUnits", inventoryService.getTotalUnitsInStore(store.getId()));
            storeData.put("totalValue", inventoryService.getStoreStockValue(store.getId()));
            storeData.put("lowStockCount", inventoryService.getLowStockCount(store.getId()));
            reportData.put(store.getName(), storeData);
        }
        model.addAttribute("reportData", reportData);
        model.addAttribute("stores", stores);
        return "reports/stock-value";
    }

    // Sales report with date range
    @GetMapping("/sales")
    public String salesReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) {

        if (startDate == null) {
            startDate = LocalDate.now().withDayOfMonth(1); // first day of current month
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        var sales = saleService.getSalesByDateRange(startDate, endDate);
        double totalAmount = saleService.getTotalSalesAmount(startDate, endDate);

        model.addAttribute("sales", sales);
        model.addAttribute("totalAmount", totalAmount);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        return "reports/sales";
    }

    // Purchase report with date range
    @GetMapping("/purchases")
    public String purchasesReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) {

        if (startDate == null) {
            startDate = LocalDate.now().withDayOfMonth(1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        var purchases = purchaseService.getPurchasesByDateRange(startDate, endDate);
        double totalAmount = purchaseService.getTotalPurchaseAmount(startDate, endDate);

        model.addAttribute("purchases", purchases);
        model.addAttribute("totalAmount", totalAmount);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        return "reports/purchases";
    }
}