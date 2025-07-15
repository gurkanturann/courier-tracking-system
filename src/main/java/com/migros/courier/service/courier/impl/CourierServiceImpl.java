package com.migros.courier.service.courier.impl;

import com.migros.courier.dao.entity.Courier;
import com.migros.courier.dao.entity.CourierLocationLog;
import com.migros.courier.dao.repository.CourierLocationLogRepository;
import com.migros.courier.dao.repository.CourierRepository;
import com.migros.courier.dao.repository.OrderRepository;
import com.migros.courier.event.CourierLocationEvent;
import com.migros.courier.exception.CourierNotFoundException;
import com.migros.courier.exception.LocationNotChangedException;
import com.migros.courier.mapper.CourierTrackingMapper;
import com.migros.courier.model.dto.CourierDto;
import com.migros.courier.model.enums.OrderStatus;
import com.migros.courier.service.courier.CourierService;
import com.migros.courier.service.distance.DistanceCalculatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class CourierServiceImpl implements CourierService {
    private final CourierRepository courierRepository;
    private final CourierLocationLogRepository locationLogRepository;
    private final OrderRepository orderRepository;
    private final DistanceCalculatorService distanceCalculator;
    private final ApplicationEventPublisher eventPublisher;
    private final CourierTrackingMapper mapper;
    @Override
    public void updateCourierLocation(Long courierId, double newLat, double newLng) {
        Courier courier = courierRepository.findById(courierId)
                .orElseThrow(() -> new CourierNotFoundException("Kurye bulunamadı: " + courierId));

        if (courier.getCurrentLat() == newLat && courier.getCurrentLng()==newLng) {
            throw new LocationNotChangedException("Kurye zaten girdiğiniz konumdadır. Güncelleme yapılmadı.");
        }
        courier.setCurrentLat(newLat);
        courier.setCurrentLng(newLng);
        courierRepository.save(courier);

        CourierLocationLog log = new CourierLocationLog();
        log.setCourier(courier);
        log.setLat(newLat);
        log.setLng(newLng);
        log.setTimestamp(LocalDateTime.now());
        locationLogRepository.save(log);

        eventPublisher.publishEvent(new CourierLocationEvent(this, courier));
    }

    @Override
    public CourierDto findNearestCourier(double lat, double lng) {
        List<OrderStatus> activeStatuses = List.of(OrderStatus.ASSIGNED, OrderStatus.PICKED_UP);
        List<Courier> busyCouriers = orderRepository.findCouriersWithOrdersInStatus(activeStatuses);

        List<Courier> allCouriers = courierRepository.findAll();
        List<Courier> availableCouriers = allCouriers.stream()
                .filter(courier -> !busyCouriers.contains(courier))
                .toList();

        if (availableCouriers.isEmpty()) {
            throw new IllegalStateException("Sistemde uygun kurye bulunmuyor.");
        }

        return availableCouriers.stream()
                .min(Comparator.comparing(courier ->
                        distanceCalculator.calculateDistance(courier.getCurrentLat(), courier.getCurrentLng(), lat, lng)
                ))
                .map(mapper::toCourierDto)
                .orElseThrow(() -> new IllegalStateException("Uygun kurye bulunamadı."));

    }

    @Override
    public Double getTotalTravelDistance(Long courierId) {
        Courier courier = courierRepository.findById(courierId)
                .orElseThrow(() -> new CourierNotFoundException("Kurye bulunamadı: " + courierId));

        List<CourierLocationLog> logs = locationLogRepository.findByCourierOrderByTimestampAsc(courier);

        if (logs.size() < 2) {
            return 0.0;
        }

        return IntStream.range(1, logs.size())
                .mapToDouble(i -> {
                    CourierLocationLog prevLog = logs.get(i - 1);
                    CourierLocationLog currentLog = logs.get(i);
                    return distanceCalculator.calculateDistance(
                            prevLog.getLat(), prevLog.getLng(),
                            currentLog.getLat(), currentLog.getLng()
                    );
                })
                .sum();
    }
}
