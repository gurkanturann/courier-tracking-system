package com.migros.courier.service.courier;

import com.migros.courier.model.dto.CourierDto;

public interface CourierService {
    void updateCourierLocation(Long courierId, double newLat, double newLng);

    CourierDto findNearestCourier(double lat, double lng);

    Double getTotalTravelDistance(Long courierId);
}
