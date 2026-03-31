package com.inventory.system.service.impl;

import com.inventory.system.model.Return;
import com.inventory.system.model.Store;
import com.inventory.system.repository.ReturnRepository;
import com.inventory.system.repository.StoreRepository;
import com.inventory.system.service.InventoryService;
import com.inventory.system.service.ReturnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ReturnServiceImpl implements ReturnService {

    @Autowired
    private ReturnRepository returnRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private InventoryService inventoryService;

    private static final AtomicLong returnCounter = new AtomicLong(0);

    @Override
    @Transactional
    public Return saveReturn(Return returnObj) {
        // Generate return number if not set
        if (returnObj.getReturnNo() == null) {
            returnObj.setReturnNo(generateReturnNumber());
        }
        // Save return first
        Return saved = returnRepository.save(returnObj);
        // Increase stock
        inventoryService.addStock(
                returnObj.getProduct().getId(),
                returnObj.getStore().getId(),
                returnObj.getQuantity()
        );
        return saved;
    }

    @Override
    public Optional<Return> getReturnById(Long id) {
        return returnRepository.findById(id);
    }

    @Override
    public List<Return> getAllReturns() {
        return returnRepository.findAll();
    }

    @Override
    public List<Return> getReturnsByStore(Long storeId) {
        Optional<Store> store = storeRepository.findById(storeId);
        return store.map(returnRepository::findByStore).orElse(List.of());
    }

    @Override
    public List<Return> getReturnsByDateRange(LocalDate start, LocalDate end) {
        return returnRepository.findByReturnDateBetween(start, end);
    }

    @Override
    public void deleteReturn(Long id) {
        returnRepository.deleteById(id);
    }

    @Override
    public String generateReturnNumber() {
        long seq = returnCounter.incrementAndGet();
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "RET-" + datePart + "-" + String.format("%04d", seq);
    }

    @Override
    public List<Return> searchReturns(String keyword) {
        return returnRepository.findByReturnNoContainingIgnoreCase(keyword);
    }

}