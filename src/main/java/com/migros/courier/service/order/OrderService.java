package com.migros.courier.service.order;

import com.migros.courier.controller.request.OrderRequest;
import com.migros.courier.event.CourierLocationEvent;
import com.migros.courier.model.dto.OrderDto;

public interface OrderService {
    OrderDto createOrder(OrderRequest requestDto);

    void updateOrderStatusBasedOnLocation(CourierLocationEvent event);
}
