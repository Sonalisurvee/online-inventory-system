package com.inventory.system.service;

import com.inventory.system.model.Inventory;
import com.inventory.system.model.Product;
import com.inventory.system.model.Store;
import java.util.List;
import java.util.Optional;

public interface InventoryService {

    // Basic CRUD
    Inventory saveInventory(Inventory inventory);
    Optional<Inventory> getInventoryById(Long id);
    List<Inventory> getAllInventory();
    void deleteInventory(Long id);

    // Find by product and store
    Optional<Inventory> getInventoryByProductAndStore(Product product, Store store);
    Optional<Inventory> getInventoryByProductAndStore(Long productId, Long storeId);

    // Get inventory by store
    List<Inventory> getInventoryByStore(Store store);
    List<Inventory> getInventoryByStoreId(Long storeId);

    // Get inventory by product
    List<Inventory> getInventoryByProduct(Product product);
    List<Inventory> getInventoryByProductId(Long productId);

    // Stock operations
    Inventory addStock(Long productId, Long storeId, int quantity);
    Inventory removeStock(Long productId, Long storeId, int quantity);
    Inventory updateStock(Long productId, Long storeId, int newQuantity);

    // Initialize inventory for a product in a store
    Inventory initializeInventory(Product product, Store store, int initialQuantity);

    // Low stock operations
    List<Inventory> getLowStockItems(Long storeId);
    List<Inventory> getAllLowStockItems();

    // Check availability
    boolean isProductAvailable(Long productId, Long storeId, int requiredQuantity);

    // Calculations
    int getTotalStockAcrossStores(Long productId);
    double getStoreStockValue(Long storeId);
    int getTotalProductsInStore(Long storeId);
    int getTotalUnitsInStore(Long storeId);
    int getLowStockCount(Long storeId);

    // Transfer stock between stores
    boolean transferStock(Long fromStoreId, Long toStoreId, Long productId, int quantity);

    // Update min/max levels
    Inventory updateMinMaxLevels(Long inventoryId, int minQuantity, int maxQuantity);

    // Get all stores (for dropdown)
    List<Store> getAllStores();

    // Get all categories (for filtering)
    List<com.inventory.system.model.Category> getAllCategories();
}