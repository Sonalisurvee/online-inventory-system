package com.inventory.system.service;

import com.inventory.system.model.Inventory;
import com.inventory.system.model.Product;
import com.inventory.system.model.Store;
import java.util.List;
import java.util.Optional;

public interface InventoryService {

    // Initialize inventory for a product in a store
    Inventory initializeInventory(Product product, Store store, int initialQuantity);

    // Get inventory by ID
    Optional<Inventory> getInventoryById(Long id);

    // Get inventory by product and store
    Optional<Inventory> getInventoryByProductAndStore(Product product, Store store);

    // Get all inventory for a store
    List<Inventory> getInventoryByStore(Store store);
    List<Inventory> getInventoryByStoreId(Long storeId);

    // Get all inventory for a product across stores
    List<Inventory> getInventoryByProduct(Product product);
    List<Inventory> getInventoryByProductId(Long productId);

    // Update stock (add or remove)
    Inventory addStock(Long productId, Long storeId, int quantity);
    Inventory removeStock(Long productId, Long storeId, int quantity);
    Inventory updateStock(Long productId, Long storeId, int newQuantity);

    // Add to InventoryService interface
    Inventory updateInventory(Inventory inventory);

    // Get low stock items for a store
    List<Inventory> getLowStockItems(Long storeId);

    // Get all inventory with low stock
    List<Inventory> getAllLowStockItems();

    // Check if product is available in store
    boolean isProductAvailable(Long productId, Long storeId, int requiredQuantity);

    // Get total stock of a product across all stores
    int getTotalStockAcrossStores(Long productId);

    // Get stock value for a store
    double getStoreStockValue(Long storeId);

    // Transfer stock between stores
    boolean transferStock(Long fromStoreId, Long toStoreId, Long productId, int quantity);

    // Get low stock items based on custom threshold
    List<Inventory> getLowStockItemsByThreshold(int threshold);

    // Get low stock items for a store based on custom threshold
    List<Inventory> getLowStockItemsByStoreAndThreshold(Long storeId, int threshold);

    // Update low stock threshold for a product in a store
    Inventory updateLowStockThreshold(Long inventoryId, int newThreshold);
}