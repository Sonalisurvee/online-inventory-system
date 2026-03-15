package com.inventory.system.repository;

import com.inventory.system.model.Store;
import com.inventory.system.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {

    // Find stores by manager
    List<Store> findByManager(User manager);

    // Find stores by status (status comes from BaseEntity)
    List<Store> findByStatus(String status);

    // Find stores by name containing (search feature)
    // IMPORTANT: Using 'name' because that's the field name in Store.java
    List<Store> findByNameContainingIgnoreCase(String name);

    // Check if store exists by name
    boolean existsByName(String name);  // Changed from 'existsByStoreName'
}