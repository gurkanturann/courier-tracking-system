package com.migros.courier.dao.repository;

import com.migros.courier.dao.entity.Courier;
import com.migros.courier.dao.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourierRepository extends JpaRepository<Courier, Long> {
}
