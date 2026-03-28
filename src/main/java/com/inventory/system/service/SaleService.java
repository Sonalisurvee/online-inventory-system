package com.inventory.system.service;

import com.inventory.system.model.Sale;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface SaleService {

    Sale saveSale(Sale sale);

    Optional<Sale> getSaleById(Long id);

    List<Sale> getAllSales();

    List<Sale> getSalesByStore(Long storeId);

    List<Sale> getSalesByDateRange(LocalDate start, LocalDate end);

    void deleteSale(Long id);

    double getTotalSalesAmount(LocalDate start, LocalDate end);

    String generateInvoiceNumber(); // helper for creating unique invoice

    Map<String, Double> getMonthlySales(int months);

}