package com.migros.courier.dao.repository;

import com.migros.courier.dao.entity.FailedEventLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FailedEventLogRepository extends JpaRepository<FailedEventLog, Long> {
    List<FailedEventLog> findByResolvedIsFalseAndRetryCountLessThan(int count);
}
