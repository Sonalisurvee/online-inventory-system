package com.inventory.system.service.impl;

import com.inventory.system.model.Sale;
import com.inventory.system.model.Store;
import com.inventory.system.repository.SaleRepository;
import com.inventory.system.repository.StoreRepository;
import com.inventory.system.service.InventoryService;
import com.inventory.system.service.SaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class SaleServiceImpl implements SaleService {

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private InventoryService inventoryService;

    private static final AtomicLong invoiceCounter = new AtomicLong(0);

    @Override
    @Transactional
    public Sale saveSale(Sale sale) {
        // Calculate totals if not set
        if (sale.getTotalPrice() == null && sale.getUnitPrice() != null && sale.getQuantity() != null) {
            sale.setTotalPrice(sale.getUnitPrice().multiply(new BigDecimal(sale.getQuantity())));
        }
        if (sale.getGrandTotal() == null) {
            BigDecimal total = sale.getTotalPrice() != null ? sale.getTotalPrice() : BigDecimal.ZERO;
            BigDecimal discount = sale.getDiscount() != null ? sale.getDiscount() : BigDecimal.ZERO;
            BigDecimal tax = sale.getTax() != null ? sale.getTax() : BigDecimal.ZERO;
            sale.setGrandTotal(total.subtract(discount).add(tax));
        }

        // Check stock availability before saving
        boolean available = inventoryService.isProductAvailable(
                sale.getProduct().getId(),
                sale.getStore().getId(),
                sale.getQuantity()
        );

        if (!available) {
            throw new RuntimeException("Insufficient stock for product: " + sale.getProduct().getName());
        }

        // Save sale first
        Sale saved = saleRepository.save(sale);

        // Decrease stock
        inventoryService.removeStock(
                sale.getProduct().getId(),
                sale.getStore().getId(),
                sale.getQuantity()
        );

        return saved;
    }

    @Override
    public Optional<Sale> getSaleById(Long id) {
        return saleRepository.findById(id);
    }

    @Override
    public List<Sale> getAllSales() {
        return saleRepository.findAll();
    }

    @Override
    public List<Sale> getSalesByStore(Long storeId) {
        Optional<Store> store = storeRepository.findById(storeId);
        return store.map(saleRepository::findByStore).orElse(List.of());
    }

    @Override
    public List<Sale> getSalesByDateRange(LocalDate start, LocalDate end) {
        return saleRepository.findBySaleDateBetween(start, end);
    }

    @Override
    public void deleteSale(Long id) {
        saleRepository.deleteById(id);
    }

    @Override
    public double getTotalSalesAmount(LocalDate start, LocalDate end) {
        return saleRepository.findBySaleDateBetween(start, end)
                .stream()
                .mapToDouble(s -> s.getGrandTotal() != null ? s.getGrandTotal().doubleValue() : 0.0)
                .sum();
    }

    @Override
    public String generateInvoiceNumber() {
        long seq = invoiceCounter.incrementAndGet();
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "SALE-" + datePart + "-" + String.format("%04d", seq);
    }
}