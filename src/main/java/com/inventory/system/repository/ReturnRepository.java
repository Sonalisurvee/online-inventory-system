package com.inventory.system.repository;

import com.inventory.system.model.Return;
import com.inventory.system.model.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReturnRepository extends JpaRepository<Return, Long> {

    List<Return> findByStore(Store store);

    List<Return> findByReturnDateBetween(LocalDate start, LocalDate end);

    List<Return> findByReturnNoContainingIgnoreCase(String returnNo);
}