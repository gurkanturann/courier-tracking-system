package com.migros.courier.service.courier;

import com.migros.courier.controller.courier.request.CreateCourierRequest;
import com.migros.courier.model.dto.CourierDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CourierService {
    void updateCourierLocation(Long courierId, double newLat, double newLng);
    CourierDto findNearestCourier(double lat, double lng);
    Double getTotalTravelDistance(Long courierId);
    CourierDto createCourier(CreateCourierRequest request);
    Page<CourierDto> getAllCouriers(Pageable pageable);
}
