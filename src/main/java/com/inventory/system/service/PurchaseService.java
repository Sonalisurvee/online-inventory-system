package com.inventory.system.service;

import com.inventory.system.model.Purchase;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PurchaseService {

    Purchase savePurchase(Purchase purchase);

    Optional<Purchase> getPurchaseById(Long id);

    List<Purchase> getAllPurchases();

    List<Purchase> getPurchasesBySupplier(Long supplierId);

    List<Purchase> getPurchasesByStore(Long storeId);

    List<Purchase> getPurchasesByDateRange(LocalDate start, LocalDate end);

    void deletePurchase(Long id);

    // For reports
    double getTotalPurchaseAmount(LocalDate start, LocalDate end);

    List<Purchase> searchPurchases(String keyword);
}