package com.inventory.system.repository;

import com.inventory.system.model.Purchase;
import com.inventory.system.model.Supplier;
import com.inventory.system.model.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    List<Purchase> findBySupplier(Supplier supplier);

    List<Purchase> findByStore(Store store);

    List<Purchase> findByPurchaseDateBetween(LocalDate start, LocalDate end);

    List<Purchase> findByInvoiceNoContainingIgnoreCase(String invoiceNo);

    List<Purchase> findByProductId(Long productId);

    List<Purchase> findByProductIdAndStoreId(Long productId, Long storeId);
}