package com.inventory.system.service.impl;

import com.inventory.system.model.Supplier;
import com.inventory.system.repository.SupplierRepository;
import com.inventory.system.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class SupplierServiceImpl implements SupplierService {

    @Autowired
    private SupplierRepository supplierRepository;

    @Override
    public Supplier saveSupplier(Supplier supplier) {
        return supplierRepository.save(supplier);
    }

    @Override
    public List<Supplier> getAllSuppliers() {
        // Return only active suppliers by default
        return supplierRepository.findByStatus("ACTIVE");
    }

    @Override
    public Optional<Supplier> getSupplierById(Long id) {
        return supplierRepository.findById(id);
    }

    @Override
    public Optional<Supplier> getSupplierByName(String name) {
        return supplierRepository.findByName(name);
    }

    @Override
    public List<Supplier> searchSuppliers(String keyword) {
        return supplierRepository.findByNameContainingIgnoreCase(keyword);
    }

    @Override
    public void deleteSupplier(Long id) {
        // Soft delete by setting status to INACTIVE (optional)
        supplierRepository.findById(id).ifPresent(supplier -> {
            supplier.setStatus("INACTIVE");
            supplierRepository.save(supplier);
        });
        // Or hard delete: supplierRepository.deleteById(id);
    }

    @Override
    public boolean supplierExists(String name) {
        return supplierRepository.existsByName(name);
    }

    @Override
    public Supplier activateSupplier(Long id) {
        Optional<Supplier> supplierOpt = supplierRepository.findById(id);
        if (supplierOpt.isPresent()) {
            Supplier supplier = supplierOpt.get();
            supplier.setStatus("ACTIVE");
            return supplierRepository.save(supplier);
        }
        return null;
    }

    @Override
    public Supplier deactivateSupplier(Long id) {
        Optional<Supplier> supplierOpt = supplierRepository.findById(id);
        if (supplierOpt.isPresent()) {
            Supplier supplier = supplierOpt.get();
            supplier.setStatus("INACTIVE");
            return supplierRepository.save(supplier);
        }
        return null;
    }
}