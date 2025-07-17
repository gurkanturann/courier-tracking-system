package com.migros.courier.controller.order;

import com.migros.courier.controller.order.request.OrderRequest;
import com.migros.courier.model.dto.OrderDto;
import com.migros.courier.service.order.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderDto> createOrder(@Valid @RequestBody OrderRequest orderRequest) {
        OrderDto newOrder = orderService.createOrder(orderRequest);
        return new ResponseEntity<>(newOrder, HttpStatus.CREATED);
    }
}
