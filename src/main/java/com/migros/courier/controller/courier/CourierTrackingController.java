package com.migros.courier.controller.courier;

import com.migros.courier.controller.courier.request.CreateCourierRequest;
import com.migros.courier.controller.courier.request.LocationRequest;
import com.migros.courier.model.dto.CourierDto;
import com.migros.courier.service.courier.CourierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequestMapping("/api/v1/courier")
@RequiredArgsConstructor
public class CourierTrackingController {
    private final CourierService courierService;

    @PostMapping("/create-courier")
    public ResponseEntity<CourierDto> createCourier(@Valid @RequestBody CreateCourierRequest courierRequestDto) {
        CourierDto createdCourier = courierService.createCourier(courierRequestDto);
        return new ResponseEntity<>(createdCourier, HttpStatus.CREATED);
    }
    @GetMapping
    public ResponseEntity<Page<CourierDto>> getAllCouriers(Pageable pageable) {
        Page<CourierDto> couriers = courierService.getAllCouriers(pageable);
        return ResponseEntity.ok(couriers);
    }

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
}
