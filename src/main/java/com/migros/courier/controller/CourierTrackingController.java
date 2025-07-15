package com.migros.courier.controller;

import com.migros.courier.controller.request.LocationRequest;
import com.migros.courier.controller.request.OrderRequest;
import com.migros.courier.model.dto.OrderDto;
import com.migros.courier.model.dto.StoreEntranceLogDto;
import com.migros.courier.service.courier.CourierService;
import com.migros.courier.service.order.OrderService;
import com.migros.courier.service.store.StoreService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Tag(name = "Courier Tracking API")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CourierTrackingController {
    private final CourierService courierService;
    private final OrderService orderService;
    private final StoreService storeService;

    @PostMapping("/couriers/{id}/move")
    public ResponseEntity<Void> updateCourierLocation(@PathVariable Long id, @RequestBody LocationRequest locationDto) {
        courierService.updateCourierLocation(id, locationDto.getLat(), locationDto.getLng());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/couriers/{id}/get-total-distance")
    public ResponseEntity<Double> getTotalTravelDistance(@PathVariable Long id) {
        Double totalDistance = courierService.getTotalTravelDistance(id);
        return ResponseEntity.ok(totalDistance);
    }
    @PostMapping("/order")
    public ResponseEntity<OrderDto> createOrder(@RequestBody OrderRequest orderRequest) {
        OrderDto createdOrder = orderService.createOrder(orderRequest);
        return ResponseEntity.ok(createdOrder);
    }

    @GetMapping("/store-entrances")
    public ResponseEntity<List<StoreEntranceLogDto>> getAllStoreEntrances() {
        List<StoreEntranceLogDto> logs = storeService.getAllLogs();
        return ResponseEntity.ok(logs);
    }
}
