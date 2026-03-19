package com.inventory.system.service;

import com.inventory.system.model.Supplier;
import java.util.List;
import java.util.Optional;

public interface SupplierService {

    Supplier saveSupplier(Supplier supplier);
    List<Supplier> getAllSuppliers();
    Optional<Supplier> getSupplierById(Long id);
    Optional<Supplier> getSupplierByName(String name);
    List<Supplier> searchSuppliers(String keyword);
    void deleteSupplier(Long id);
    boolean supplierExists(String name);
    Supplier activateSupplier(Long id);
    Supplier deactivateSupplier(Long id);
}