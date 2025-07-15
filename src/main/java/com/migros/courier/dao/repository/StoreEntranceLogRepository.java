package com.migros.courier.dao.repository;

import com.migros.courier.dao.entity.Courier;
import com.migros.courier.dao.entity.Store;
import com.migros.courier.dao.entity.StoreEntranceLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface StoreEntranceLogRepository extends JpaRepository<StoreEntranceLog, Long> {
    boolean existsByCourierAndStoreAndEntranceTimeAfter(Courier courier, Store store, LocalDateTime minusMinutes);
}
