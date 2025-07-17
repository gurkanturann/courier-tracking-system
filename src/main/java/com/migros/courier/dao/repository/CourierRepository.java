package com.migros.courier.dao.repository;

import com.migros.courier.dao.entity.Courier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CourierRepository extends JpaRepository<Courier, Long> {
    Optional<Courier> findByName(String name);
}
