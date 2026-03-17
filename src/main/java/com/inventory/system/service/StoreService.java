package com.inventory.system.service;

import com.inventory.system.model.Store;
import com.inventory.system.model.User;
import java.util.List;
import java.util.Optional;

public interface StoreService {

    // Create/Update
    Store saveStore(Store store);

    // Read
    List<Store> getAllStores();
    Optional<Store> getStoreById(Long id);
    List<Store> searchStores(String keyword);
    List<Store> getStoresByManager(User manager);
    List<Store> getActiveStores();

    // Delete
    void deleteStore(Long id);

    // Check existence
    boolean storeExists(String name);

    // Assign manager
    Store assignManager(Long storeId, Long managerId);

    // Toggle status
    Store activateStore(Long id);
    Store deactivateStore(Long id);
}