package com.inventory.system.repository;

import com.inventory.system.model.Product;
import com.inventory.system.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Find products by category
    List<Product> findByCategory(Category category);

    // Find products by name containing (search)
    List<Product> findByNameContainingIgnoreCase(String name);

    // Find products by price range
    List<Product> findByUnitPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    // FIXED: Find products that need reordering (reorder level is a threshold, not stock)
    @Query("SELECT p FROM Product p WHERE p.reorderLevel > 0 AND p.reorderLevel < 10")
    List<Product> findProductsNeedingReorder();

    // Find products by category ID
    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId")
    List<Product> findByCategoryId(@Param("categoryId") Long categoryId);

    // Check if product exists by name
    boolean existsByName(String name);

    // Find all active products
    List<Product> findByStatus(String status);
}