package com.inventory.system.service.impl;

import com.inventory.system.model.Inventory;
import com.inventory.system.model.Product;
import com.inventory.system.model.Store;
import com.inventory.system.model.Category;
import com.inventory.system.repository.InventoryRepository;
import com.inventory.system.repository.ProductRepository;
import com.inventory.system.repository.StoreRepository;
import com.inventory.system.repository.CategoryRepository;
import com.inventory.system.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class InventoryServiceImpl implements InventoryService {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public Inventory saveInventory(Inventory inventory) {
        return inventoryRepository.save(inventory);
    }

    @Override
    public Optional<Inventory> getInventoryById(Long id) {
        return inventoryRepository.findById(id);
    }

    @Override
    public List<Inventory> getAllInventory() {
        return inventoryRepository.findAll();
    }

    @Override
    public void deleteInventory(Long id) {
        inventoryRepository.deleteById(id);
    }

    @Override
    public Optional<Inventory> getInventoryByProductAndStore(Product product, Store store) {
        return inventoryRepository.findByProductAndStore(product, store);
    }

    @Override
    public Optional<Inventory> getInventoryByProductAndStore(Long productId, Long storeId) {
        Optional<Product> product = productRepository.findById(productId);
        Optional<Store> store = storeRepository.findById(storeId);

        if (product.isPresent() && store.isPresent()) {
            return inventoryRepository.findByProductAndStore(product.get(), store.get());
        }
        return Optional.empty();
    }

    @Override
    public List<Inventory> getInventoryByStore(Store store) {
        return inventoryRepository.findByStore(store);
    }

    @Override
    public List<Inventory> getInventoryByStoreId(Long storeId) {
        Optional<Store> store = storeRepository.findById(storeId);
        return store.map(value -> inventoryRepository.findByStore(value))
                .orElse(List.of());
    }

    @Override
    public List<Inventory> getInventoryByProduct(Product product) {
        return inventoryRepository.findByProduct(product);
    }

    @Override
    public List<Inventory> getInventoryByProductId(Long productId) {
        Optional<Product> product = productRepository.findById(productId);
        return product.map(value -> inventoryRepository.findByProduct(value))
                .orElse(List.of());
    }

    @Override
    @Transactional
    public Inventory addStock(Long productId, Long storeId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        Optional<Product> product = productRepository.findById(productId);
        Optional<Store> store = storeRepository.findById(storeId);

        if (product.isPresent() && store.isPresent()) {
            Optional<Inventory> inventoryOpt = inventoryRepository
                    .findByProductAndStore(product.get(), store.get());

            Inventory inventory;
            if (inventoryOpt.isPresent()) {
                inventory = inventoryOpt.get();
                inventory.setQuantity(inventory.getQuantity() + quantity);
            } else {
                inventory = new Inventory();
                inventory.setProduct(product.get());
                inventory.setStore(store.get());
                inventory.setQuantity(quantity);
                inventory.setMinQuantity(5); // Default
                inventory.setMaxQuantity(1000); // Default
            }

            return inventoryRepository.save(inventory);
        }
        throw new RuntimeException("Product or Store not found");
    }

    @Override
    @Transactional
    public Inventory removeStock(Long productId, Long storeId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        Optional<Product> product = productRepository.findById(productId);
        Optional<Store> store = storeRepository.findById(storeId);

        if (product.isPresent() && store.isPresent()) {
            Optional<Inventory> inventoryOpt = inventoryRepository
                    .findByProductAndStore(product.get(), store.get());

            if (inventoryOpt.isPresent()) {
                Inventory inventory = inventoryOpt.get();
                int newQuantity = inventory.getQuantity() - quantity;
                if (newQuantity >= 0) {
                    inventory.setQuantity(newQuantity);
                    return inventoryRepository.save(inventory);
                } else {
                    throw new RuntimeException("Insufficient stock! Available: " +
                            inventory.getQuantity() + ", Requested: " + quantity);
                }
            } else {
                throw new RuntimeException("No inventory found for this product in the store");
            }
        }
        throw new RuntimeException("Product or Store not found");
    }

    @Override
    @Transactional
    public Inventory updateStock(Long productId, Long storeId, int newQuantity) {
        if (newQuantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }

        Optional<Product> product = productRepository.findById(productId);
        Optional<Store> store = storeRepository.findById(storeId);

        if (product.isPresent() && store.isPresent()) {
            Optional<Inventory> inventoryOpt = inventoryRepository
                    .findByProductAndStore(product.get(), store.get());

            Inventory inventory;
            if (inventoryOpt.isPresent()) {
                inventory = inventoryOpt.get();
                inventory.setQuantity(newQuantity);
            } else {
                inventory = new Inventory();
                inventory.setProduct(product.get());
                inventory.setStore(store.get());
                inventory.setQuantity(newQuantity);
                inventory.setMinQuantity(5);
                inventory.setMaxQuantity(1000);
            }

            return inventoryRepository.save(inventory);
        }
        throw new RuntimeException("Product or Store not found");
    }

    @Override
    public Inventory initializeInventory(Product product, Store store, int initialQuantity) {
        Optional<Inventory> existing = inventoryRepository.findByProductAndStore(product, store);

        if (existing.isPresent()) {
            Inventory inventory = existing.get();
            inventory.setQuantity(initialQuantity);
            return inventoryRepository.save(inventory);
        } else {
            Inventory inventory = new Inventory();
            inventory.setProduct(product);
            inventory.setStore(store);
            inventory.setQuantity(initialQuantity);
            inventory.setMinQuantity(5);
            inventory.setMaxQuantity(1000);
            return inventoryRepository.save(inventory);
        }
    }

    @Override
    public List<Inventory> getLowStockItems(Long storeId) {
        Optional<Store> store = storeRepository.findById(storeId);
        return store.map(value -> inventoryRepository.findLowStockItems(value))
                .orElse(List.of());
    }

    @Override
    public List<Inventory> getAllLowStockItems() {
        return inventoryRepository.findAllLowStockItems();
    }

    @Override
    public boolean isProductAvailable(Long productId, Long storeId, int requiredQuantity) {
        Optional<Product> product = productRepository.findById(productId);
        Optional<Store> store = storeRepository.findById(storeId);

        if (product.isPresent() && store.isPresent()) {
            Optional<Inventory> inventoryOpt = inventoryRepository
                    .findByProductAndStore(product.get(), store.get());

            return inventoryOpt.filter(inventory ->
                    inventory.getQuantity() >= requiredQuantity).isPresent();
        }
        return false;
    }

    @Override
    public int getTotalStockAcrossStores(Long productId) {
        Optional<Product> product = productRepository.findById(productId);
        return product.map(p -> inventoryRepository.findByProduct(p)
                .stream()
                .mapToInt(Inventory::getQuantity)
                .sum()).orElse(0);
    }

    @Override
    public double getStoreStockValue(Long storeId) {
        Optional<Store> store = storeRepository.findById(storeId);
        if (store.isPresent()) {
            return inventoryRepository.findByStore(store.get())
                    .stream()
                    .mapToDouble(inv -> inv.getQuantity() *
                            (inv.getProduct().getUnitPrice() != null ?
                                    inv.getProduct().getUnitPrice().doubleValue() : 0))
                    .sum();
        }
        return 0.0;
    }

    @Override
    public int getTotalProductsInStore(Long storeId) {
        Optional<Store> store = storeRepository.findById(storeId);
        return store.map(s -> inventoryRepository.findByStore(s).size()).orElse(0);
    }

    @Override
    public int getTotalUnitsInStore(Long storeId) {
        Optional<Store> store = storeRepository.findById(storeId);
        if (store.isPresent()) {
            return inventoryRepository.findByStore(store.get())
                    .stream()
                    .mapToInt(Inventory::getQuantity)
                    .sum();
        }
        return 0;
    }

    @Override
    public int getLowStockCount(Long storeId) {
        return getLowStockItems(storeId).size();
    }

    @Override
    @Transactional
    public boolean transferStock(Long fromStoreId, Long toStoreId, Long productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        if (fromStoreId.equals(toStoreId)) {
            throw new IllegalArgumentException("Source and destination stores must be different");
        }

        // Check if source has enough stock
        if (!isProductAvailable(productId, fromStoreId, quantity)) {
            return false;
        }

        try {
            // Remove from source
            removeStock(productId, fromStoreId, quantity);
            // Add to destination
            addStock(productId, toStoreId, quantity);
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Transfer failed: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Inventory updateMinMaxLevels(Long inventoryId, int minQuantity, int maxQuantity) {
        Optional<Inventory> inventoryOpt = inventoryRepository.findById(inventoryId);
        if (inventoryOpt.isPresent()) {
            Inventory inventory = inventoryOpt.get();
            inventory.setMinQuantity(minQuantity);
            inventory.setMaxQuantity(maxQuantity);
            return inventoryRepository.save(inventory);
        }
        throw new RuntimeException("Inventory not found with id: " + inventoryId);
    }

    @Override
    public List<Store> getAllStores() {
        return storeRepository.findAll();
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
}