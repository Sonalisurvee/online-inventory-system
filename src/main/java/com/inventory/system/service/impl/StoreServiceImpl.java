package com.inventory.system.service.impl;

import com.inventory.system.model.Store;
import com.inventory.system.model.User;
import com.inventory.system.repository.StoreRepository;
import com.inventory.system.repository.UserRepository;
import com.inventory.system.service.StoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class StoreServiceImpl implements StoreService {

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public Store saveStore(Store store) {
        return storeRepository.save(store);
    }

    @Override
    public List<Store> getAllStores() {
        return storeRepository.findByStatus("ACTIVE");
    }

    @Override
    public Optional<Store> getStoreById(Long id) {
        return storeRepository.findById(id);
    }

    @Override
    public List<Store> searchStores(String keyword) {
        return storeRepository.findByNameContainingIgnoreCase(keyword);
    }

    @Override
    public List<Store> getStoresByManager(User manager) {
        return storeRepository.findByManager(manager);
    }

    @Override
    public List<Store> getActiveStores() {
        return storeRepository.findByStatus("ACTIVE");
    }

    @Override
    public void deleteStore(Long id) {
        storeRepository.deleteById(id);
    }

    @Override
    public boolean storeExists(String name) {
        return storeRepository.existsByName(name);
    }

    @Override
    public Store assignManager(Long storeId, Long managerId) {
        Optional<Store> storeOpt = storeRepository.findById(storeId);
        Optional<User> managerOpt = userRepository.findById(managerId);

        if (storeOpt.isPresent() && managerOpt.isPresent()) {
            Store store = storeOpt.get();
            store.setManager(managerOpt.get());
            return storeRepository.save(store);
        }
        return null;
    }

    @Override
    public Store activateStore(Long id) {
        Optional<Store> storeOpt = storeRepository.findById(id);
        if (storeOpt.isPresent()) {
            Store store = storeOpt.get();
            store.setStatus("ACTIVE");
            return storeRepository.save(store);
        }
        return null;
    }

    @Override
    public Store deactivateStore(Long id) {
        Optional<Store> storeOpt = storeRepository.findById(id);
        if (storeOpt.isPresent()) {
            Store store = storeOpt.get();
            store.setStatus("INACTIVE");
            return storeRepository.save(store);
        }
        return null;
    }
}