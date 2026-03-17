package com.inventory.system.repository;

import com.inventory.system.model.Inventory;
import com.inventory.system.model.Product;
import com.inventory.system.model.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    // Find inventory by product and store
    Optional<Inventory> findByProductAndStore(Product product, Store store);

    // Find all inventory for a store
    List<Inventory> findByStore(Store store);

    // Find all inventory for a product
    List<Inventory> findByProduct(Product product);

    // Find low stock items for a store (quantity < minQuantity)
    @Query("SELECT i FROM Inventory i WHERE i.store = :store AND i.quantity < i.minQuantity")
    List<Inventory> findLowStockItems(@Param("store") Store store);

    // Find all low stock items across all stores
    @Query("SELECT i FROM Inventory i WHERE i.quantity < i.minQuantity")
    List<Inventory> findAllLowStockItems();

    // Find out of stock items
    @Query("SELECT i FROM Inventory i WHERE i.quantity = 0")
    List<Inventory> findOutOfStockItems();

    // Find inventory by product ID and store ID
    @Query("SELECT i FROM Inventory i WHERE i.product.id = :productId AND i.store.id = :storeId")
    Optional<Inventory> findByProductIdAndStoreId(@Param("productId") Long productId,
                                                  @Param("storeId") Long storeId);

    // Count total products in a store
    Long countByStore(Store store);

    // Delete all inventory for a product
    void deleteByProduct(Product product);

    // Delete all inventory for a store
    void deleteByStore(Store store);

    // Find low stock items with custom threshold
    @Query("SELECT i FROM Inventory i WHERE i.quantity < :threshold")
    List<Inventory> findLowStockByThreshold(@Param("threshold") int threshold);

    // Find low stock items for a store with custom threshold
    @Query("SELECT i FROM Inventory i WHERE i.store.id = :storeId AND i.quantity < :threshold")
    List<Inventory> findLowStockByStoreAndThreshold(@Param("storeId") Long storeId,
                                                    @Param("threshold") int threshold);

}