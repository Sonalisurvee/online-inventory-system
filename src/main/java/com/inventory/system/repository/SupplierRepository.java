package com.inventory.system.repository;

import com.inventory.system.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    // Find supplier by name
    Optional<Supplier> findByName(String name);

    // Find suppliers by name containing (search)
    List<Supplier> findByNameContainingIgnoreCase(String name);

    // Find supplier by email
    Optional<Supplier> findByEmail(String email);

    // Find supplier by phone
    Optional<Supplier> findByPhone(String phone);

    // Find supplier by GST number
    Optional<Supplier> findByGstNumber(String gstNumber);

    // Find all active suppliers
    List<Supplier> findByStatus(String status);
}