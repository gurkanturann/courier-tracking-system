package com.migros.courier.service.courier.impl;

import com.migros.courier.dao.entity.Courier;
import com.migros.courier.dao.repository.CourierRepository;
import com.migros.courier.event.CourierLocationEvent;
import com.migros.courier.exception.CourierNotFoundException;
import com.migros.courier.mapper.CourierTrackingMapper;
import com.migros.courier.model.dto.CourierDto;
import com.migros.courier.service.courier.CourierService;
import com.migros.courier.service.distance.DistanceCalculatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourierServiceImpl implements CourierService {
    private final CourierRepository courierRepository;
    private final DistanceCalculatorService distanceCalculator;
    private final ApplicationEventPublisher eventPublisher;
    private final CourierTrackingMapper mapper;
    @Override
    public void updateCourierLocation(Long courierId, double newLat, double newLng) {
        Courier courier = courierRepository.findById(courierId)
                .orElseThrow(() -> new CourierNotFoundException("Kurye bulunamadı: " + courierId));

        if (courier.getCurrentLat() != 0 || courier.getCurrentLng() != 0) {
            double traveledDistance = distanceCalculator.calculateDistance(
                    courier.getCurrentLat(), courier.getCurrentLng(), newLat, newLng);
            courier.setTotalDistance(courier.getTotalDistance() + traveledDistance);
        }

        courier.setCurrentLat(newLat);
        courier.setCurrentLng(newLng);
        Courier updatedCourier = courierRepository.save(courier);

        eventPublisher.publishEvent(new CourierLocationEvent(this, updatedCourier));
    }

    @Override
    public CourierDto findNearestCourier(double lat, double lng) {
        List<Courier> allCouriers = courierRepository.findAll();
        if (allCouriers.isEmpty()) {
            throw new IllegalStateException("Sistemde aktif kurye bulunmuyor.");
        }

        Courier nearestCourier = allCouriers.stream()
                .min((c1, c2) -> Double.compare(
                        distanceCalculator.calculateDistance(c1.getCurrentLat(), c1.getCurrentLng(), lat, lng),
                        distanceCalculator.calculateDistance(c2.getCurrentLat(), c2.getCurrentLng(), lat, lng)
                )).orElseThrow();

        return mapper.toCourierDto(nearestCourier);

    }

    @Override
    public Double getTotalTravelDistance(Long courierId) {
        return courierRepository.findById(courierId)
                .map(Courier::getTotalDistance)
                .orElseThrow(() -> new CourierNotFoundException("Kurye bulunamadı: " + courierId));
    }
}
