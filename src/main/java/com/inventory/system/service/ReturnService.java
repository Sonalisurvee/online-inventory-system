package com.inventory.system.service;

import com.inventory.system.model.Return;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReturnService {

    Return saveReturn(Return returnObj);

    Optional<Return> getReturnById(Long id);

    List<Return> getAllReturns();

    List<Return> getReturnsByStore(Long storeId);

    List<Return> getReturnsByDateRange(LocalDate start, LocalDate end);

    void deleteReturn(Long id);

    String generateReturnNumber();
}