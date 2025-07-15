package com.migros.courier.dao.repository;

import com.migros.courier.dao.entity.Courier;
import com.migros.courier.dao.entity.Order;
import com.migros.courier.model.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByAssignedCourierAndStatus(Courier courier, OrderStatus status);
}
