package com.inventory.system.service;

import com.inventory.system.model.StockTransfer;
import java.util.List;
import java.util.Optional;
import com.inventory.system.model.User;

public interface StockTransferService {

    StockTransfer createTransfer(StockTransfer transfer);

    StockTransfer approveTransfer(Long transferId, User approver);

    StockTransfer rejectTransfer(Long transferId, User approver, String reason);

    StockTransfer completeTransfer(Long transferId); // after approval, stock is moved

    Optional<StockTransfer> getTransferById(Long id);

    List<StockTransfer> getAllTransfers();

    List<StockTransfer> getTransfersByStatus(String status);

    void deleteTransfer(Long id);

    String generateTransferNumber();
}