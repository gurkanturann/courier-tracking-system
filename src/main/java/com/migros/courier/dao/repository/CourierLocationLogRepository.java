package com.migros.courier.dao.repository;

import com.migros.courier.dao.entity.Courier;
import com.migros.courier.dao.entity.CourierLocationLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourierLocationLogRepository extends JpaRepository<CourierLocationLog, Long> {
    List<CourierLocationLog> findByCourierOrderByTimestampAsc(Courier courier);
}
