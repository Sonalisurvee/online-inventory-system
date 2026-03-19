package com.inventory.system.repository;

import com.inventory.system.model.Sale;
import com.inventory.system.model.Store;
import com.inventory.system.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {

    List<Sale> findByStore(Store store);

    List<Sale> findByUser(User user);

    List<Sale> findBySaleDateBetween(LocalDate start, LocalDate end);

    List<Sale> findByInvoiceNoContainingIgnoreCase(String invoiceNo);

    List<Sale> findByProductIdAndStoreId(Long productId, Long storeId);
}