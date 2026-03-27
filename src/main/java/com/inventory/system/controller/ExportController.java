package com.inventory.system.controller;

import com.inventory.system.model.*;
import com.inventory.system.service.*;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/export")
public class ExportController {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private SaleService saleService;

    @Autowired
    private PurchaseService purchaseService;

    @Autowired
    private StoreService storeService;

    @Autowired
    private ProductService productService;

    @Autowired
    private SupplierService supplierService;

    // Helper to write Excel
    private void writeExcel(Workbook workbook, HttpServletResponse response, String filename) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=" + filename);
        workbook.write(response.getOutputStream());
        workbook.close();
    }

    // Helper to write PDF
    private void writePdf(Document document, HttpServletResponse response, String filename) throws IOException, DocumentException {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=" + filename);
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();
    }

    // ========== STOCK VALUE REPORT ==========
    @GetMapping("/stock-value/excel")
    public void exportStockValueExcel(HttpServletResponse response) throws IOException {
        var stores = storeService.getAllStores();
        Map<String, Map<String, Object>> reportData = getStockValueData(stores);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Stock Value");

        org.apache.poi.ss.usermodel.Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Store");
        header.createCell(1).setCellValue("Products");
        header.createCell(2).setCellValue("Total Units");
        header.createCell(3).setCellValue("Total Value (₹)");
        header.createCell(4).setCellValue("Low Stock Items");

        int rowNum = 1;
        for (var store : stores) {
            Map<String, Object> data = reportData.get(store.getName());
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(store.getName());
            row.createCell(1).setCellValue((int) data.get("totalProducts"));
            row.createCell(2).setCellValue((int) data.get("totalUnits"));
            // data.get("totalValue") is BigDecimal, so get its double value
            row.createCell(3).setCellValue(((BigDecimal) data.get("totalValue")).doubleValue());
            row.createCell(4).setCellValue((int) data.get("lowStockCount"));
        }

        writeExcel(workbook, response, "stock-value-report.xlsx");
    }

    @GetMapping("/stock-value/pdf")
    public void exportStockValuePdf(HttpServletResponse response) throws IOException, DocumentException {
        var stores = storeService.getAllStores();
        Map<String, Map<String, Object>> reportData = getStockValueData(stores);

        Document document = new Document(PageSize.A4.rotate());
        writePdf(document, response, "stock-value-report.pdf");

        com.lowagie.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Paragraph title = new Paragraph("Stock Value Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph("Generated on: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                FontFactory.getFont(FontFactory.HELVETICA, 10)));
        document.add(Chunk.NEWLINE);

        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.addCell("Store");
        table.addCell("Products");
        table.addCell("Total Units");
        table.addCell("Total Value (₹)");
        table.addCell("Low Stock Items");

        for (var store : stores) {
            Map<String, Object> data = reportData.get(store.getName());
            table.addCell(store.getName());
            table.addCell(String.valueOf((int) data.get("totalProducts")));
            table.addCell(String.valueOf((int) data.get("totalUnits")));
            table.addCell(String.format("%.2f", ((BigDecimal) data.get("totalValue")).doubleValue()));
            table.addCell(String.valueOf((int) data.get("lowStockCount")));
        }

        document.add(table);
        document.close();
    }

    // ========== SALES REPORT ==========
    @GetMapping("/sales/excel")
    public void exportSalesExcel(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                 HttpServletResponse response) throws IOException {
        if (startDate == null) startDate = LocalDate.now().withDayOfMonth(1);
        if (endDate == null) endDate = LocalDate.now();

        var sales = saleService.getSalesByDateRange(startDate, endDate);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sales");

        org.apache.poi.ss.usermodel.Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Invoice No");
        header.createCell(1).setCellValue("Date");
        header.createCell(2).setCellValue("Store");
        header.createCell(3).setCellValue("Product");
        header.createCell(4).setCellValue("Quantity");
        header.createCell(5).setCellValue("Unit Price");
        header.createCell(6).setCellValue("Total");
        header.createCell(7).setCellValue("Payment Method");
        header.createCell(8).setCellValue("Customer");

        int rowNum = 1;
        for (var sale : sales) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(sale.getInvoiceNo());
            row.createCell(1).setCellValue(sale.getSaleDate().toString());
            row.createCell(2).setCellValue(sale.getStore().getName());
            row.createCell(3).setCellValue(sale.getProduct().getName());
            row.createCell(4).setCellValue(sale.getQuantity());
            row.createCell(5).setCellValue(sale.getUnitPrice().doubleValue());
            row.createCell(6).setCellValue(sale.getGrandTotal().doubleValue());
            row.createCell(7).setCellValue(sale.getPaymentMethod().toString());
            row.createCell(8).setCellValue(sale.getCustomerName() != null ? sale.getCustomerName() : "");
        }

        writeExcel(workbook, response, "sales-report.xlsx");
    }

    @GetMapping("/sales/pdf")
    public void exportSalesPdf(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                               HttpServletResponse response) throws IOException, DocumentException {
        if (startDate == null) startDate = LocalDate.now().withDayOfMonth(1);
        if (endDate == null) endDate = LocalDate.now();

        var sales = saleService.getSalesByDateRange(startDate, endDate);
        double total = saleService.getTotalSalesAmount(startDate, endDate);

        Document document = new Document(PageSize.A4.rotate());
        writePdf(document, response, "sales-report.pdf");

        com.lowagie.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Paragraph title = new Paragraph("Sales Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph("Period: " + startDate + " to " + endDate));
        document.add(new Paragraph("Total Sales: ₹" + String.format("%.2f", total)));
        document.add(Chunk.NEWLINE);

        PdfPTable table = new PdfPTable(9);
        table.setWidthPercentage(100);
        table.addCell("Invoice No");
        table.addCell("Date");
        table.addCell("Store");
        table.addCell("Product");
        table.addCell("Qty");
        table.addCell("Unit Price");
        table.addCell("Total");
        table.addCell("Payment");
        table.addCell("Customer");

        for (var sale : sales) {
            table.addCell(sale.getInvoiceNo());
            table.addCell(sale.getSaleDate().toString());
            table.addCell(sale.getStore().getName());
            table.addCell(sale.getProduct().getName());
            table.addCell(String.valueOf(sale.getQuantity()));
            table.addCell(String.format("%.2f", sale.getUnitPrice().doubleValue()));
            table.addCell(String.format("%.2f", sale.getGrandTotal().doubleValue()));
            table.addCell(sale.getPaymentMethod().toString());
            table.addCell(sale.getCustomerName() != null ? sale.getCustomerName() : "");
        }

        document.add(table);
        document.close();
    }

    // ========== PURCHASE REPORT ==========
    @GetMapping("/purchases/excel")
    public void exportPurchasesExcel(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                     @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                     HttpServletResponse response) throws IOException {
        if (startDate == null) startDate = LocalDate.now().withDayOfMonth(1);
        if (endDate == null) endDate = LocalDate.now();

        var purchases = purchaseService.getPurchasesByDateRange(startDate, endDate);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Purchases");

        org.apache.poi.ss.usermodel.Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Invoice No");
        header.createCell(1).setCellValue("Date");
        header.createCell(2).setCellValue("Supplier");
        header.createCell(3).setCellValue("Product");
        header.createCell(4).setCellValue("Store");
        header.createCell(5).setCellValue("Quantity");
        header.createCell(6).setCellValue("Unit Cost");
        header.createCell(7).setCellValue("Total Cost");
        header.createCell(8).setCellValue("Payment Status");

        int rowNum = 1;
        for (var p : purchases) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(p.getInvoiceNo());
            row.createCell(1).setCellValue(p.getPurchaseDate().toString());
            row.createCell(2).setCellValue(p.getSupplier().getName());
            row.createCell(3).setCellValue(p.getProduct().getName());
            row.createCell(4).setCellValue(p.getStore().getName());
            row.createCell(5).setCellValue(p.getQuantity());
            row.createCell(6).setCellValue(p.getUnitCost().doubleValue());
            row.createCell(7).setCellValue(p.getTotalCost().doubleValue());
            row.createCell(8).setCellValue(p.getPaymentStatus().toString());
        }

        writeExcel(workbook, response, "purchases-report.xlsx");
    }

    @GetMapping("/purchases/pdf")
    public void exportPurchasesPdf(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                   HttpServletResponse response) throws IOException, DocumentException {
        if (startDate == null) startDate = LocalDate.now().withDayOfMonth(1);
        if (endDate == null) endDate = LocalDate.now();

        var purchases = purchaseService.getPurchasesByDateRange(startDate, endDate);
        double total = purchaseService.getTotalPurchaseAmount(startDate, endDate);

        Document document = new Document(PageSize.A4.rotate());
        writePdf(document, response, "purchases-report.pdf");

        com.lowagie.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Paragraph title = new Paragraph("Purchase Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph("Period: " + startDate + " to " + endDate));
        document.add(new Paragraph("Total Purchases: ₹" + String.format("%.2f", total)));
        document.add(Chunk.NEWLINE);

        PdfPTable table = new PdfPTable(9);
        table.setWidthPercentage(100);
        table.addCell("Invoice No");
        table.addCell("Date");
        table.addCell("Supplier");
        table.addCell("Product");
        table.addCell("Store");
        table.addCell("Qty");
        table.addCell("Unit Cost");
        table.addCell("Total Cost");
        table.addCell("Payment Status");

        for (var p : purchases) {
            table.addCell(p.getInvoiceNo());
            table.addCell(p.getPurchaseDate().toString());
            table.addCell(p.getSupplier().getName());
            table.addCell(p.getProduct().getName());
            table.addCell(p.getStore().getName());
            table.addCell(String.valueOf(p.getQuantity()));
            table.addCell(String.format("%.2f", p.getUnitCost().doubleValue()));
            table.addCell(String.format("%.2f", p.getTotalCost().doubleValue()));
            table.addCell(p.getPaymentStatus().toString());
        }

        document.add(table);
        document.close();
    }

    // ========== PRODUCT LIST ==========
    @GetMapping("/products/excel")
    public void exportProductsExcel(HttpServletResponse response) throws IOException {
        List<Product> products = productService.getAllProducts();

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Products");

        org.apache.poi.ss.usermodel.Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("ID");
        header.createCell(1).setCellValue("Name");
        header.createCell(2).setCellValue("Category");
        header.createCell(3).setCellValue("Price (₹)");
        header.createCell(4).setCellValue("Reorder Level");
        header.createCell(5).setCellValue("Expiry Date");
        header.createCell(6).setCellValue("Status");

        int rowNum = 1;
        for (Product p : products) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(p.getId());
            row.createCell(1).setCellValue(p.getName());
            row.createCell(2).setCellValue(p.getCategory() != null ? p.getCategory().getName() : "");
            row.createCell(3).setCellValue(p.getUnitPrice().doubleValue());
            row.createCell(4).setCellValue(p.getReorderLevel());
            row.createCell(5).setCellValue(p.getExpiryDate() != null ? p.getExpiryDate().toString() : "");
            row.createCell(6).setCellValue(p.getStatus());
        }

        writeExcel(workbook, response, "products.xlsx");
    }

    @GetMapping("/products/pdf")
    public void exportProductsPdf(HttpServletResponse response) throws IOException, DocumentException {
        List<Product> products = productService.getAllProducts();

        Document document = new Document(PageSize.A4.rotate());
        writePdf(document, response, "products.pdf");

        com.lowagie.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Paragraph title = new Paragraph("Product Catalog", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph("Generated on: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy"))));
        document.add(Chunk.NEWLINE);

        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        table.addCell("ID");
        table.addCell("Name");
        table.addCell("Category");
        table.addCell("Price (₹)");
        table.addCell("Reorder Level");
        table.addCell("Expiry Date");
        table.addCell("Status");

        for (Product p : products) {
            table.addCell(String.valueOf(p.getId()));
            table.addCell(p.getName());
            table.addCell(p.getCategory() != null ? p.getCategory().getName() : "");
            table.addCell(String.format("%.2f", p.getUnitPrice().doubleValue()));
            table.addCell(String.valueOf(p.getReorderLevel()));
            table.addCell(p.getExpiryDate() != null ? p.getExpiryDate().toString() : "");
            table.addCell(p.getStatus());
        }

        document.add(table);
        document.close();
    }

    // ========== SUPPLIER LIST ==========
    @GetMapping("/suppliers/excel")
    public void exportSuppliersExcel(HttpServletResponse response) throws IOException {
        List<Supplier> suppliers = supplierService.getAllSuppliers();

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Suppliers");

        org.apache.poi.ss.usermodel.Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("ID");
        header.createCell(1).setCellValue("Name");
        header.createCell(2).setCellValue("Contact Person");
        header.createCell(3).setCellValue("Phone");
        header.createCell(4).setCellValue("Email");
        header.createCell(5).setCellValue("GST");
        header.createCell(6).setCellValue("Status");

        int rowNum = 1;
        for (Supplier s : suppliers) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(s.getId());
            row.createCell(1).setCellValue(s.getName());
            row.createCell(2).setCellValue(s.getContactPerson() != null ? s.getContactPerson() : "");
            row.createCell(3).setCellValue(s.getPhone() != null ? s.getPhone() : "");
            row.createCell(4).setCellValue(s.getEmail() != null ? s.getEmail() : "");
            row.createCell(5).setCellValue(s.getGstNumber() != null ? s.getGstNumber() : "");
            row.createCell(6).setCellValue(s.getStatus());
        }

        writeExcel(workbook, response, "suppliers.xlsx");
    }

    @GetMapping("/suppliers/pdf")
    public void exportSuppliersPdf(HttpServletResponse response) throws IOException, DocumentException {
        List<Supplier> suppliers = supplierService.getAllSuppliers();

        Document document = new Document(PageSize.A4.rotate());
        writePdf(document, response, "suppliers.pdf");

        com.lowagie.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Paragraph title = new Paragraph("Suppliers List", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph("Generated on: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy"))));
        document.add(Chunk.NEWLINE);

        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        table.addCell("ID");
        table.addCell("Name");
        table.addCell("Contact Person");
        table.addCell("Phone");
        table.addCell("Email");
        table.addCell("GST");
        table.addCell("Status");

        for (Supplier s : suppliers) {
            table.addCell(String.valueOf(s.getId()));
            table.addCell(s.getName());
            table.addCell(s.getContactPerson() != null ? s.getContactPerson() : "");
            table.addCell(s.getPhone() != null ? s.getPhone() : "");
            table.addCell(s.getEmail() != null ? s.getEmail() : "");
            table.addCell(s.getGstNumber() != null ? s.getGstNumber() : "");
            table.addCell(s.getStatus());
        }

        document.add(table);
        document.close();
    }

    // ========== LOW STOCK REPORT ==========
    @GetMapping("/low-stock/excel")
    public void exportLowStockExcel(HttpServletResponse response) throws IOException {
        List<Inventory> lowStock = inventoryService.getAllLowStockItems();

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Low Stock Items");

        org.apache.poi.ss.usermodel.Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Store");
        header.createCell(1).setCellValue("Product");
        header.createCell(2).setCellValue("Current Stock");
        header.createCell(3).setCellValue("Minimum Level");
        header.createCell(4).setCellValue("Status");

        int rowNum = 1;
        for (Inventory inv : lowStock) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(inv.getStore().getName());
            row.createCell(1).setCellValue(inv.getProduct().getName());
            row.createCell(2).setCellValue(inv.getQuantity());
            row.createCell(3).setCellValue(inv.getMinQuantity());
            row.createCell(4).setCellValue(inv.getQuantity() == 0 ? "OUT OF STOCK" : "LOW STOCK");
        }

        writeExcel(workbook, response, "low-stock-report.xlsx");
    }

    @GetMapping("/low-stock/pdf")
    public void exportLowStockPdf(HttpServletResponse response) throws IOException, DocumentException {
        List<Inventory> lowStock = inventoryService.getAllLowStockItems();

        Document document = new Document(PageSize.A4.rotate());
        writePdf(document, response, "low-stock-report.pdf");

        com.lowagie.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Paragraph title = new Paragraph("Low Stock Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph("Generated on: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy"))));
        document.add(Chunk.NEWLINE);

        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.addCell("Store");
        table.addCell("Product");
        table.addCell("Current Stock");
        table.addCell("Minimum Level");
        table.addCell("Status");

        for (Inventory inv : lowStock) {
            table.addCell(inv.getStore().getName());
            table.addCell(inv.getProduct().getName());
            table.addCell(String.valueOf(inv.getQuantity()));
            table.addCell(String.valueOf(inv.getMinQuantity()));
            table.addCell(inv.getQuantity() == 0 ? "OUT OF STOCK" : "LOW STOCK");
        }

        document.add(table);
        document.close();
    }

    // Helper to get stock value data (FIXED)
    private Map<String, Map<String, Object>> getStockValueData(List<Store> stores) {
        Map<String, Map<String, Object>> result = new HashMap<>();
        for (Store store : stores) {
            Map<String, Object> data = new HashMap<>();
            data.put("totalProducts", inventoryService.getTotalProductsInStore(store.getId()));
            data.put("totalUnits", inventoryService.getTotalUnitsInStore(store.getId()));
            // Convert the double to BigDecimal
            double stockValue = inventoryService.getStoreStockValue(store.getId());
            data.put("totalValue", BigDecimal.valueOf(stockValue));
            data.put("lowStockCount", inventoryService.getLowStockCount(store.getId()));
            result.put(store.getName(), data);
        }
        return result;
    }
}