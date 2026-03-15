package com.inventory.system.repository;

import com.inventory.system.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Find category by name (exact match)
    Optional<Category> findByName(String name);

    // Find categories by name containing (search)
    List<Category> findByNameContainingIgnoreCase(String name);

    // Check if category exists by name
    boolean existsByName(String name);

    // Find all active categories
    List<Category> findByStatus(String status);
}