package com.inventory.system.service.impl;

import com.inventory.system.model.Purchase;
import com.inventory.system.model.Product;
import com.inventory.system.model.Store;
import com.inventory.system.model.Supplier;
import com.inventory.system.repository.PurchaseRepository;
import com.inventory.system.repository.ProductRepository;
import com.inventory.system.repository.StoreRepository;
import com.inventory.system.repository.SupplierRepository;
import com.inventory.system.service.InventoryService;
import com.inventory.system.service.PurchaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class PurchaseServiceImpl implements PurchaseService {

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private InventoryService inventoryService;

    @Override
    @Transactional
    public Purchase savePurchase(Purchase purchase) {
        // Calculate total cost if not set
        if (purchase.getTotalCost() == null && purchase.getUnitCost() != null && purchase.getQuantity() != null) {
            purchase.setTotalCost(purchase.getUnitCost().multiply(new java.math.BigDecimal(purchase.getQuantity())));
        }

        // Save purchase first
        Purchase saved = purchaseRepository.save(purchase);

        // Automatically add stock to inventory
        inventoryService.addStock(
                purchase.getProduct().getId(),
                purchase.getStore().getId(),
                purchase.getQuantity()
        );

        return saved;
    }

    @Override
    public Optional<Purchase> getPurchaseById(Long id) {
        return purchaseRepository.findById(id);
    }

    @Override
    public List<Purchase> getAllPurchases() {
        return purchaseRepository.findAll();
    }

    @Override
    public List<Purchase> getPurchasesBySupplier(Long supplierId) {
        Optional<Supplier> supplier = supplierRepository.findById(supplierId);
        return supplier.map(purchaseRepository::findBySupplier).orElse(List.of());
    }

    @Override
    public List<Purchase> getPurchasesByStore(Long storeId) {
        Optional<Store> store = storeRepository.findById(storeId);
        return store.map(purchaseRepository::findByStore).orElse(List.of());
    }

    @Override
    public List<Purchase> getPurchasesByDateRange(LocalDate start, LocalDate end) {
        return purchaseRepository.findByPurchaseDateBetween(start, end);
    }

    @Override
    public void deletePurchase(Long id) {
        purchaseRepository.deleteById(id);
    }

    @Override
    public double getTotalPurchaseAmount(LocalDate start, LocalDate end) {
        return purchaseRepository.findByPurchaseDateBetween(start, end)
                .stream()
                .mapToDouble(p -> p.getTotalCost() != null ? p.getTotalCost().doubleValue() : 0.0)
                .sum();
    }
}