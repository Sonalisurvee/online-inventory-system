package com.inventory.system.controller;

import com.inventory.system.model.*;
import com.inventory.system.service.*;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
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

    // ========== HELPER METHODS ==========

    /**
     * Creates a styled Excel cell.
     */
    private Cell createStyledCell(Row row, int col, Object value, CellStyle style) {
        Cell cell = row.createCell(col);
        if (value != null) {
            if (value instanceof Number) {
                cell.setCellValue(((Number) value).doubleValue());
            } else {
                cell.setCellValue(value.toString());
            }
        } else {
            cell.setCellValue("");
        }
        if (style != null) {
            cell.setCellStyle(style);
        }
        return cell;
    }

    /**
     * Creates header style for Excel.
     */
    private CellStyle getHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();  // fully qualified POI Font
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle getDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.LEFT);
        return style;
    }

    /**
     * Auto-sizes columns in Excel sheet.
     */
    private void autoSizeColumns(Sheet sheet, int columnCount) {
        for (int i = 0; i < columnCount; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * Creates a styled PDF cell for header.
     */
    private PdfPCell createPdfHeaderCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(5);
        return cell;
    }

    /**
     * Creates a normal PDF cell.
     */
    private PdfPCell createPdfCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5);
        return cell;
    }

    // ========== STOCK VALUE REPORT ==========
    @GetMapping("/stock-value/excel")
    public void exportStockValueExcel(HttpServletResponse response) throws IOException {
        List<Store> stores = storeService.getAllStores();
        Map<String, Map<String, Object>> reportData = getStockValueData(stores);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Stock Value");

        CellStyle headerStyle = getHeaderStyle(workbook);
        CellStyle dataStyle = getDataStyle(workbook);

        // Header
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Store", "Products", "Total Units", "Total Value (₹)", "Low Stock Items"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Data
        int rowNum = 1;
        for (Store store : stores) {
            Map<String, Object> data = reportData.get(store.getName());
            Row row = sheet.createRow(rowNum++);
            createStyledCell(row, 0, store.getName(), dataStyle);
            createStyledCell(row, 1, data.get("totalProducts"), dataStyle);
            createStyledCell(row, 2, data.get("totalUnits"), dataStyle);
            createStyledCell(row, 3, ((BigDecimal) data.get("totalValue")).doubleValue(), dataStyle);
            createStyledCell(row, 4, data.get("lowStockCount"), dataStyle);
        }

        autoSizeColumns(sheet, headers.length);
        writeExcel(workbook, response, "stock-value-report.xlsx");
    }

    @GetMapping("/stock-value/pdf")
    public void exportStockValuePdf(HttpServletResponse response) throws IOException, DocumentException {
        List<Store> stores = storeService.getAllStores();
        Map<String, Map<String, Object>> reportData = getStockValueData(stores);

        Document document = new Document(PageSize.A4.rotate());
        writePdf(document, response, "stock-value-report.pdf");

        Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
        Font headerFont = new Font(Font.HELVETICA, 12, Font.BOLD);
        Font normalFont = new Font(Font.HELVETICA, 10, Font.NORMAL);

        Paragraph title = new Paragraph("Stock Value Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph("Generated on: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")), normalFont));
        document.add(Chunk.NEWLINE);

        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{2, 1, 1, 2, 1});

        // Header
        table.addCell(createPdfHeaderCell("Store", headerFont));
        table.addCell(createPdfHeaderCell("Products", headerFont));
        table.addCell(createPdfHeaderCell("Total Units", headerFont));
        table.addCell(createPdfHeaderCell("Total Value (₹)", headerFont));
        table.addCell(createPdfHeaderCell("Low Stock Items", headerFont));

        // Data
        for (Store store : stores) {
            Map<String, Object> data = reportData.get(store.getName());
            table.addCell(createPdfCell(store.getName(), normalFont));
            table.addCell(createPdfCell(String.valueOf(data.get("totalProducts")), normalFont));
            table.addCell(createPdfCell(String.valueOf(data.get("totalUnits")), normalFont));
            table.addCell(createPdfCell(String.format("%.2f", ((BigDecimal) data.get("totalValue")).doubleValue()), normalFont));
            table.addCell(createPdfCell(String.valueOf(data.get("lowStockCount")), normalFont));
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

        List<Sale> sales = saleService.getSalesByDateRange(startDate, endDate);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sales");
        CellStyle headerStyle = getHeaderStyle(workbook);
        CellStyle dataStyle = getDataStyle(workbook);

        String[] headers = {"Invoice No", "Date", "Store", "Product", "Quantity", "Unit Price", "Total", "Payment Method", "Customer"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        for (Sale sale : sales) {
            Row row = sheet.createRow(rowNum++);
            createStyledCell(row, 0, sale.getInvoiceNo(), dataStyle);
            createStyledCell(row, 1, sale.getSaleDate().toString(), dataStyle);
            createStyledCell(row, 2, sale.getStore().getName(), dataStyle);
            createStyledCell(row, 3, sale.getProduct().getName(), dataStyle);
            createStyledCell(row, 4, sale.getQuantity(), dataStyle);
            createStyledCell(row, 5, sale.getUnitPrice().doubleValue(), dataStyle);
            createStyledCell(row, 6, sale.getGrandTotal().doubleValue(), dataStyle);
            createStyledCell(row, 7, sale.getPaymentMethod().toString(), dataStyle);
            createStyledCell(row, 8, sale.getCustomerName() != null ? sale.getCustomerName() : "", dataStyle);
        }

        autoSizeColumns(sheet, headers.length);
        writeExcel(workbook, response, "sales-report.xlsx");
    }

    @GetMapping("/sales/pdf")
    public void exportSalesPdf(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                               HttpServletResponse response) throws IOException, DocumentException {
        if (startDate == null) startDate = LocalDate.now().withDayOfMonth(1);
        if (endDate == null) endDate = LocalDate.now();

        List<Sale> sales = saleService.getSalesByDateRange(startDate, endDate);
        double total = saleService.getTotalSalesAmount(startDate, endDate);

        Document document = new Document(PageSize.A4.rotate());
        writePdf(document, response, "sales-report.pdf");

        Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
        Font headerFont = new Font(Font.HELVETICA, 12, Font.BOLD);
        Font normalFont = new Font(Font.HELVETICA, 10, Font.NORMAL);

        Paragraph title = new Paragraph("Sales Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph("Period: " + startDate + " to " + endDate, normalFont));
        document.add(new Paragraph("Total Sales: ₹" + String.format("%.2f", total), normalFont));
        document.add(Chunk.NEWLINE);

        PdfPTable table = new PdfPTable(9);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{2, 2, 2, 2, 1, 2, 2, 2, 2});

        // Header
        String[] headers = {"Invoice No", "Date", "Store", "Product", "Qty", "Unit Price", "Total", "Payment", "Customer"};
        for (String h : headers) {
            table.addCell(createPdfHeaderCell(h, headerFont));
        }

        // Data
        for (Sale sale : sales) {
            table.addCell(createPdfCell(sale.getInvoiceNo(), normalFont));
            table.addCell(createPdfCell(sale.getSaleDate().toString(), normalFont));
            table.addCell(createPdfCell(sale.getStore().getName(), normalFont));
            table.addCell(createPdfCell(sale.getProduct().getName(), normalFont));
            table.addCell(createPdfCell(String.valueOf(sale.getQuantity()), normalFont));
            table.addCell(createPdfCell(String.format("%.2f", sale.getUnitPrice().doubleValue()), normalFont));
            table.addCell(createPdfCell(String.format("%.2f", sale.getGrandTotal().doubleValue()), normalFont));
            table.addCell(createPdfCell(sale.getPaymentMethod().toString(), normalFont));
            table.addCell(createPdfCell(sale.getCustomerName() != null ? sale.getCustomerName() : "", normalFont));
        }

        document.add(table);
        document.close();
    }

    // ========== PURCHASE REPORT (similar pattern) ==========
    // For brevity, I'll show the key parts; you can replicate for purchases, products, suppliers, low stock.
    // I'll provide the full code for purchases here, and you can apply the same enhancements to others.

    @GetMapping("/purchases/excel")
    public void exportPurchasesExcel(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                     @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                     HttpServletResponse response) throws IOException {
        if (startDate == null) startDate = LocalDate.now().withDayOfMonth(1);
        if (endDate == null) endDate = LocalDate.now();

        List<Purchase> purchases = purchaseService.getPurchasesByDateRange(startDate, endDate);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Purchases");
        CellStyle headerStyle = getHeaderStyle(workbook);
        CellStyle dataStyle = getDataStyle(workbook);

        String[] headers = {"Invoice No", "Date", "Supplier", "Product", "Store", "Quantity", "Unit Cost", "Total Cost", "Payment Status"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        for (Purchase p : purchases) {
            Row row = sheet.createRow(rowNum++);
            createStyledCell(row, 0, p.getInvoiceNo(), dataStyle);
            createStyledCell(row, 1, p.getPurchaseDate().toString(), dataStyle);
            createStyledCell(row, 2, p.getSupplier().getName(), dataStyle);
            createStyledCell(row, 3, p.getProduct().getName(), dataStyle);
            createStyledCell(row, 4, p.getStore().getName(), dataStyle);
            createStyledCell(row, 5, p.getQuantity(), dataStyle);
            createStyledCell(row, 6, p.getUnitCost().doubleValue(), dataStyle);
            createStyledCell(row, 7, p.getTotalCost().doubleValue(), dataStyle);
            createStyledCell(row, 8, p.getPaymentStatus().toString(), dataStyle);
        }

        autoSizeColumns(sheet, headers.length);
        writeExcel(workbook, response, "purchases-report.xlsx");
    }

    @GetMapping("/purchases/pdf")
    public void exportPurchasesPdf(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                   HttpServletResponse response) throws IOException, DocumentException {
        if (startDate == null) startDate = LocalDate.now().withDayOfMonth(1);
        if (endDate == null) endDate = LocalDate.now();

        List<Purchase> purchases = purchaseService.getPurchasesByDateRange(startDate, endDate);
        double total = purchaseService.getTotalPurchaseAmount(startDate, endDate);

        Document document = new Document(PageSize.A4.rotate());
        writePdf(document, response, "purchases-report.pdf");

        Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
        Font headerFont = new Font(Font.HELVETICA, 12, Font.BOLD);
        Font normalFont = new Font(Font.HELVETICA, 10, Font.NORMAL);

        Paragraph title = new Paragraph("Purchase Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph("Period: " + startDate + " to " + endDate, normalFont));
        document.add(new Paragraph("Total Purchases: ₹" + String.format("%.2f", total), normalFont));
        document.add(Chunk.NEWLINE);

        PdfPTable table = new PdfPTable(9);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{2, 2, 2, 2, 1, 2, 2, 2, 2});

        // Header
        String[] headers = {"Invoice No", "Date", "Supplier", "Product", "Store", "Qty", "Unit Cost", "Total Cost", "Payment Status"};
        for (String h : headers) {
            table.addCell(createPdfHeaderCell(h, headerFont));
        }

        // Data
        for (Purchase p : purchases) {
            table.addCell(createPdfCell(p.getInvoiceNo(), normalFont));
            table.addCell(createPdfCell(p.getPurchaseDate().toString(), normalFont));
            table.addCell(createPdfCell(p.getSupplier().getName(), normalFont));
            table.addCell(createPdfCell(p.getProduct().getName(), normalFont));
            table.addCell(createPdfCell(p.getStore().getName(), normalFont));
            table.addCell(createPdfCell(String.valueOf(p.getQuantity()), normalFont));
            table.addCell(createPdfCell(String.format("%.2f", p.getUnitCost().doubleValue()), normalFont));
            table.addCell(createPdfCell(String.format("%.2f", p.getTotalCost().doubleValue()), normalFont));
            table.addCell(createPdfCell(p.getPaymentStatus().toString(), normalFont));
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
        CellStyle headerStyle = getHeaderStyle(workbook);
        CellStyle dataStyle = getDataStyle(workbook);

        String[] headers = {"ID", "Name", "Category", "Price (₹)", "Reorder Level", "Expiry Date", "Status"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        for (Product p : products) {
            Row row = sheet.createRow(rowNum++);
            createStyledCell(row, 0, p.getId(), dataStyle);
            createStyledCell(row, 1, p.getName(), dataStyle);
            createStyledCell(row, 2, p.getCategory() != null ? p.getCategory().getName() : "", dataStyle);
            createStyledCell(row, 3, p.getUnitPrice().doubleValue(), dataStyle);
            createStyledCell(row, 4, p.getReorderLevel(), dataStyle);
            createStyledCell(row, 5, p.getExpiryDate() != null ? p.getExpiryDate().toString() : "", dataStyle);
            createStyledCell(row, 6, p.getStatus(), dataStyle);
        }

        autoSizeColumns(sheet, headers.length);
        writeExcel(workbook, response, "products.xlsx");
    }

    @GetMapping("/products/pdf")
    public void exportProductsPdf(HttpServletResponse response) throws IOException, DocumentException {
        List<Product> products = productService.getAllProducts();

        Document document = new Document(PageSize.A4.rotate());
        writePdf(document, response, "products.pdf");

        Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
        Font headerFont = new Font(Font.HELVETICA, 12, Font.BOLD);
        Font normalFont = new Font(Font.HELVETICA, 10, Font.NORMAL);

        Paragraph title = new Paragraph("Product Catalog", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph("Generated on: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")), normalFont));
        document.add(Chunk.NEWLINE);

        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1, 3, 2, 2, 2, 2, 2});

        String[] headers = {"ID", "Name", "Category", "Price (₹)", "Reorder Level", "Expiry Date", "Status"};
        for (String h : headers) {
            table.addCell(createPdfHeaderCell(h, headerFont));
        }

        for (Product p : products) {
            table.addCell(createPdfCell(String.valueOf(p.getId()), normalFont));
            table.addCell(createPdfCell(p.getName(), normalFont));
            table.addCell(createPdfCell(p.getCategory() != null ? p.getCategory().getName() : "", normalFont));
            table.addCell(createPdfCell(String.format("%.2f", p.getUnitPrice().doubleValue()), normalFont));
            table.addCell(createPdfCell(String.valueOf(p.getReorderLevel()), normalFont));
            table.addCell(createPdfCell(p.getExpiryDate() != null ? p.getExpiryDate().toString() : "", normalFont));
            table.addCell(createPdfCell(p.getStatus(), normalFont));
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
        CellStyle headerStyle = getHeaderStyle(workbook);
        CellStyle dataStyle = getDataStyle(workbook);

        String[] headers = {"ID", "Name", "Contact Person", "Phone", "Email", "GST", "Status"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        for (Supplier s : suppliers) {
            Row row = sheet.createRow(rowNum++);
            createStyledCell(row, 0, s.getId(), dataStyle);
            createStyledCell(row, 1, s.getName(), dataStyle);
            createStyledCell(row, 2, s.getContactPerson() != null ? s.getContactPerson() : "", dataStyle);
            createStyledCell(row, 3, s.getPhone() != null ? s.getPhone() : "", dataStyle);
            createStyledCell(row, 4, s.getEmail() != null ? s.getEmail() : "", dataStyle);
            createStyledCell(row, 5, s.getGstNumber() != null ? s.getGstNumber() : "", dataStyle);
            createStyledCell(row, 6, s.getStatus(), dataStyle);
        }

        autoSizeColumns(sheet, headers.length);
        writeExcel(workbook, response, "suppliers.xlsx");
    }

    @GetMapping("/suppliers/pdf")
    public void exportSuppliersPdf(HttpServletResponse response) throws IOException, DocumentException {
        List<Supplier> suppliers = supplierService.getAllSuppliers();

        Document document = new Document(PageSize.A4.rotate());
        writePdf(document, response, "suppliers.pdf");

        Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
        Font headerFont = new Font(Font.HELVETICA, 12, Font.BOLD);
        Font normalFont = new Font(Font.HELVETICA, 10, Font.NORMAL);

        Paragraph title = new Paragraph("Suppliers List", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph("Generated on: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")), normalFont));
        document.add(Chunk.NEWLINE);

        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1, 3, 2, 2, 2, 2, 2});

        String[] headers = {"ID", "Name", "Contact Person", "Phone", "Email", "GST", "Status"};
        for (String h : headers) {
            table.addCell(createPdfHeaderCell(h, headerFont));
        }

        for (Supplier s : suppliers) {
            table.addCell(createPdfCell(String.valueOf(s.getId()), normalFont));
            table.addCell(createPdfCell(s.getName(), normalFont));
            table.addCell(createPdfCell(s.getContactPerson() != null ? s.getContactPerson() : "", normalFont));
            table.addCell(createPdfCell(s.getPhone() != null ? s.getPhone() : "", normalFont));
            table.addCell(createPdfCell(s.getEmail() != null ? s.getEmail() : "", normalFont));
            table.addCell(createPdfCell(s.getGstNumber() != null ? s.getGstNumber() : "", normalFont));
            table.addCell(createPdfCell(s.getStatus(), normalFont));
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
        CellStyle headerStyle = getHeaderStyle(workbook);
        CellStyle dataStyle = getDataStyle(workbook);

        String[] headers = {"Store", "Product", "Current Stock", "Minimum Level", "Status"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        for (Inventory inv : lowStock) {
            Row row = sheet.createRow(rowNum++);
            createStyledCell(row, 0, inv.getStore().getName(), dataStyle);
            createStyledCell(row, 1, inv.getProduct().getName(), dataStyle);
            createStyledCell(row, 2, inv.getQuantity(), dataStyle);
            createStyledCell(row, 3, inv.getMinQuantity(), dataStyle);
            createStyledCell(row, 4, inv.getQuantity() == 0 ? "OUT OF STOCK" : "LOW STOCK", dataStyle);
        }

        autoSizeColumns(sheet, headers.length);
        writeExcel(workbook, response, "low-stock-report.xlsx");
    }

    @GetMapping("/low-stock/pdf")
    public void exportLowStockPdf(HttpServletResponse response) throws IOException, DocumentException {
        List<Inventory> lowStock = inventoryService.getAllLowStockItems();

        Document document = new Document(PageSize.A4.rotate());
        writePdf(document, response, "low-stock-report.pdf");

        Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
        Font headerFont = new Font(Font.HELVETICA, 12, Font.BOLD);
        Font normalFont = new Font(Font.HELVETICA, 10, Font.NORMAL);

        Paragraph title = new Paragraph("Low Stock Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph("Generated on: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")), normalFont));
        document.add(Chunk.NEWLINE);

        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{2, 3, 1, 1, 2});

        String[] headers = {"Store", "Product", "Current Stock", "Min Level", "Status"};
        for (String h : headers) {
            table.addCell(createPdfHeaderCell(h, headerFont));
        }

        for (Inventory inv : lowStock) {
            table.addCell(createPdfCell(inv.getStore().getName(), normalFont));
            table.addCell(createPdfCell(inv.getProduct().getName(), normalFont));
            table.addCell(createPdfCell(String.valueOf(inv.getQuantity()), normalFont));
            table.addCell(createPdfCell(String.valueOf(inv.getMinQuantity()), normalFont));
            table.addCell(createPdfCell(inv.getQuantity() == 0 ? "OUT OF STOCK" : "LOW STOCK", normalFont));
        }

        document.add(table);
        document.close();
    }

    // ---------- Helper Methods ----------
    private void writeExcel(Workbook workbook, HttpServletResponse response, String filename) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=" + filename);
        workbook.write(response.getOutputStream());
        workbook.close();
    }

    private void writePdf(Document document, HttpServletResponse response, String filename) throws IOException, DocumentException {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=" + filename);
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();
    }

    private Map<String, Map<String, Object>> getStockValueData(List<Store> stores) {
        Map<String, Map<String, Object>> result = new HashMap<>();
        for (Store store : stores) {
            Map<String, Object> data = new HashMap<>();
            data.put("totalProducts", inventoryService.getTotalProductsInStore(store.getId()));
            data.put("totalUnits", inventoryService.getTotalUnitsInStore(store.getId()));
            double stockValue = inventoryService.getStoreStockValue(store.getId());
            data.put("totalValue", BigDecimal.valueOf(stockValue));
            data.put("lowStockCount", inventoryService.getLowStockCount(store.getId()));
            result.put(store.getName(), data);
        }
        return result;
    }
}