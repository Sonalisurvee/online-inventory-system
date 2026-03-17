package com.inventory.system.service.impl;

import com.inventory.system.model.Inventory;
import com.inventory.system.model.Product;
import com.inventory.system.model.Store;
import com.inventory.system.repository.InventoryRepository;
import com.inventory.system.repository.ProductRepository;
import com.inventory.system.repository.StoreRepository;
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

    @Override
    public Inventory initializeInventory(Product product, Store store, int initialQuantity) {
        // Check if inventory already exists
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
            inventory.setMinQuantity(5); // Default min quantity
            inventory.setMaxQuantity(1000); // Default max quantity
            return inventoryRepository.save(inventory);
        }
    }

    @Override
    public Optional<Inventory> getInventoryById(Long id) {
        return inventoryRepository.findById(id);
    }

    @Override
    public Optional<Inventory> getInventoryByProductAndStore(Product product, Store store) {
        return inventoryRepository.findByProductAndStore(product, store);
    }

    @Override
    public List<Inventory> getInventoryByStore(Store store) {
        return inventoryRepository.findByStore(store);
    }

    @Override
    public List<Inventory> getInventoryByStoreId(Long storeId) {
        Optional<Store> store = storeRepository.findById(storeId);
        if (store.isPresent()) {
            return inventoryRepository.findByStore(store.get());
        }
        return List.of(); // Return empty list if store not found
    }

    @Override
    public List<Inventory> getInventoryByProduct(Product product) {
        return inventoryRepository.findByProduct(product);
    }

    @Override
    public List<Inventory> getInventoryByProductId(Long productId) {
        Optional<Product> product = productRepository.findById(productId);
        if (product.isPresent()) {
            return inventoryRepository.findByProduct(product.get());
        }
        return List.of(); // Return empty list if product not found
    }

    @Override
    @Transactional
    public Inventory addStock(Long productId, Long storeId, int quantity) {
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
                inventory.setMinQuantity(5);
                inventory.setMaxQuantity(1000);
            }

            return inventoryRepository.save(inventory);
        }
        return null;
    }

    @Override
    @Transactional
    public Inventory removeStock(Long productId, Long storeId, int quantity) {
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
                }
            }
        }
        return null;
    }

    @Override
    @Transactional
    public Inventory updateStock(Long productId, Long storeId, int newQuantity) {
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
        return null;
    }

    @Override
    public List<Inventory> getLowStockItems(Long storeId) {
        Optional<Store> store = storeRepository.findById(storeId);
        if (store.isPresent()) {
            return inventoryRepository.findLowStockItems(store.get());
        }
        return List.of(); // Return empty list if store not found
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
        if (product.isPresent()) {
            return inventoryRepository.findByProduct(product.get())
                    .stream()
                    .mapToInt(Inventory::getQuantity)
                    .sum();
        }
        return 0; // Return 0 if product not found
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
        return 0;
    }

    @Override
    @Transactional
    public boolean transferStock(Long fromStoreId, Long toStoreId, Long productId, int quantity) {
        Optional<Product> product = productRepository.findById(productId);
        Optional<Store> fromStore = storeRepository.findById(fromStoreId);
        Optional<Store> toStore = storeRepository.findById(toStoreId);

        if (product.isPresent() && fromStore.isPresent() && toStore.isPresent()) {
            // Check if source has enough stock
            if (isProductAvailable(productId, fromStoreId, quantity)) {
                // Remove from source
                removeStock(productId, fromStoreId, quantity);
                // Add to destination
                addStock(productId, toStoreId, quantity);
                return true;
            }
        }
        return false;
    }

    @Override
    public Inventory updateInventory(Inventory inventory) {
        return inventoryRepository.save(inventory);
    }

    @Override
    public List<Inventory> getLowStockItemsByThreshold(int threshold) {
        return inventoryRepository.findLowStockByThreshold(threshold);
    }

    @Override
    public List<Inventory> getLowStockItemsByStoreAndThreshold(Long storeId, int threshold) {
        return inventoryRepository.findLowStockByStoreAndThreshold(storeId, threshold);
    }

    @Override
    public Inventory updateLowStockThreshold(Long inventoryId, int newThreshold) {
        Optional<Inventory> inventoryOpt = inventoryRepository.findById(inventoryId);
        if (inventoryOpt.isPresent()) {
            Inventory inventory = inventoryOpt.get();
            inventory.setMinQuantity(newThreshold);
            return inventoryRepository.save(inventory);
        }
        return null;
    }

}