package com.migros.courier.controller;

import com.migros.courier.controller.request.LocationRequest;
import com.migros.courier.controller.request.OrderRequest;
import com.migros.courier.model.dto.OrderDto;
import com.migros.courier.model.dto.StoreEntranceLogDto;
import com.migros.courier.service.courier.CourierService;
import com.migros.courier.service.order.OrderService;
import com.migros.courier.service.store.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CourierTrackingController {
    private final CourierService courierService;
    private final OrderService orderService;
    private final StoreService storeService;

    @PostMapping("/couriers/{id}/location")
    public ResponseEntity<Void> updateCourierLocation(@PathVariable Long id, @RequestBody LocationRequest locationDto) {
        courierService.updateCourierLocation(id, locationDto.getLat(), locationDto.getLng());
        return ResponseEntity.ok().build();
    }

    /**
     * Belirtilen kuryenin o ana kadar katettiği toplam mesafeyi (metre cinsinden) döndürür.
     *
     * @param id Kuryenin ID'si.
     * @return Kuryenin toplam mesafesi.
     */
    @GetMapping("/couriers/{id}/distance")
    public ResponseEntity<Double> getTotalTravelDistance(@PathVariable Long id) {
        Double totalDistance = courierService.getTotalTravelDistance(id);
        return ResponseEntity.ok(totalDistance);
    }

    /**
     * Yeni bir sipariş oluşturur.
     * Sistem, müşterinin konumuna göre en yakın kuryeyi ve o kuryeye en yakın mağazayı otomatik olarak atar.
     *
     * @param orderRequest Müşterinin enlem ve boylamını içeren DTO.
     * @return Oluşturulan siparişin detaylarını içeren DTO.
     */
    @PostMapping("/orders")
    public ResponseEntity<OrderDto> createOrder(@RequestBody OrderRequest orderRequest) {
        OrderDto createdOrder = orderService.createOrder(orderRequest);
        return ResponseEntity.ok(createdOrder);
    }

    /**
     * Bir kuryenin bir mağazanın 100 metrelik yarıçapına yaptığı tüm girişleri listeler.
     * Bu endpoint, projenin ana gereksinimlerinden birini doğrulamak için kullanılır.
     *
     * @return Tüm mağaza giriş loglarının listesi.
     */
    @GetMapping("/store-entrances")
    public ResponseEntity<List<StoreEntranceLogDto>> getAllStoreEntrances() {
        // Not: Bu metodu StoreService'e eklememiz gerekiyor.
        List<StoreEntranceLogDto> logs = storeService.getAllLogs();
        return ResponseEntity.ok(logs);
    }
}
