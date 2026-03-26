package com.inventory.system.service.impl;

import com.inventory.system.model.StockTransfer;
import com.inventory.system.model.User;
import com.inventory.system.repository.StockTransferRepository;
import com.inventory.system.service.InventoryService;
import com.inventory.system.service.StockTransferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class StockTransferServiceImpl implements StockTransferService {

    @Autowired
    private StockTransferRepository transferRepository;

    @Autowired
    private InventoryService inventoryService;

    private static final AtomicLong transferCounter = new AtomicLong(0);

    @Override
    public StockTransfer createTransfer(StockTransfer transfer) {
        if (transfer.getTransferNo() == null) {
            transfer.setTransferNo(generateTransferNumber());
        }
        transfer.setRequestDate(LocalDate.now());
        transfer.setStatus("PENDING");
        return transferRepository.save(transfer);
    }

    @Override
    public StockTransfer approveTransfer(Long transferId, User approver) {
        StockTransfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new RuntimeException("Transfer not found"));

        if (!"PENDING".equals(transfer.getStatus())) {
            throw new RuntimeException("Transfer is already " + transfer.getStatus());
        }

        transfer.setApprovedBy(approver);
        transfer.setStatus("APPROVED");
        return transferRepository.save(transfer);
    }

    @Override
    public StockTransfer rejectTransfer(Long transferId, User approver, String reason) {
        StockTransfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new RuntimeException("Transfer not found"));

        if (!"PENDING".equals(transfer.getStatus())) {
            throw new RuntimeException("Transfer is already " + transfer.getStatus());
        }

        transfer.setApprovedBy(approver);
        transfer.setStatus("CANCELLED");
        transfer.setNotes(reason);
        return transferRepository.save(transfer);
    }

    @Override
    @Transactional
    public StockTransfer completeTransfer(Long transferId) {
        StockTransfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new RuntimeException("Transfer not found"));

        if (!"APPROVED".equals(transfer.getStatus())) {
            throw new RuntimeException("Only approved transfers can be completed");
        }

        // Move stock from source to destination
        inventoryService.removeStock(
                transfer.getProduct().getId(),
                transfer.getFromStore().getId(),
                transfer.getQuantity()
        );
        inventoryService.addStock(
                transfer.getProduct().getId(),
                transfer.getToStore().getId(),
                transfer.getQuantity()
        );

        transfer.setStatus("COMPLETED");
        transfer.setCompletionDate(LocalDate.now());
        return transferRepository.save(transfer);
    }

    @Override
    public Optional<StockTransfer> getTransferById(Long id) {
        return transferRepository.findById(id);
    }

    @Override
    public List<StockTransfer> getAllTransfers() {
        return transferRepository.findAll();
    }

    @Override
    public List<StockTransfer> getTransfersByStatus(String status) {
        return transferRepository.findByStatus(status);
    }

    @Override
    public void deleteTransfer(Long id) {
        transferRepository.deleteById(id);
    }

    @Override
    public String generateTransferNumber() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int randomPart = (int) (Math.random() * 10000);
        return "TRF-" + datePart + "-" + String.format("%04d", randomPart);
    }
}