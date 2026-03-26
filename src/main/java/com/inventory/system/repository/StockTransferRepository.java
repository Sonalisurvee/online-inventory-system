package com.inventory.system.repository;

import com.inventory.system.model.StockTransfer;
import com.inventory.system.model.Store;
import com.inventory.system.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StockTransferRepository extends JpaRepository<StockTransfer, Long> {

    List<StockTransfer> findByStatus(String status);

    List<StockTransfer> findByFromStore(Store store);

    List<StockTransfer> findByToStore(Store store);

    List<StockTransfer> findByRequestedBy(User user);

    List<StockTransfer> findByApprovedBy(User user);
}