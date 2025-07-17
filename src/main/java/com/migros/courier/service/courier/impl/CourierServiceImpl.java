package com.migros.courier.service.courier.impl;

import com.migros.courier.constant.CourierTrackingConstant;
import com.migros.courier.controller.courier.request.CreateCourierRequest;
import com.migros.courier.dao.entity.Courier;
import com.migros.courier.dao.entity.CourierLocationLog;
import com.migros.courier.dao.repository.CourierLocationLogRepository;
import com.migros.courier.dao.repository.CourierRepository;
import com.migros.courier.dao.repository.OrderRepository;
import com.migros.courier.event.CourierLocationEvent;
import com.migros.courier.exception.CourierNotFoundException;
import com.migros.courier.exception.InvalidRequestException;
import com.migros.courier.exception.LocationNotChangedException;
import com.migros.courier.mapper.CourierTrackingMapper;
import com.migros.courier.model.dto.CourierDto;
import com.migros.courier.model.enums.OrderStatus;
import com.migros.courier.service.courier.CourierService;
import com.migros.courier.service.distance.DistanceCalculatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CourierServiceImpl implements CourierService {
    private final CourierRepository courierRepository;
    private final CourierLocationLogRepository locationLogRepository;
    private final OrderRepository orderRepository;
    private final DistanceCalculatorService distanceCalculator;
    private final ApplicationEventPublisher eventPublisher;
    @Override
    public void updateCourierLocation(Long courierId, double newLat, double newLng) {
        Courier courier = courierRepository.findById(courierId)
                .orElseThrow(() -> new CourierNotFoundException("Kurye bulunamadı: " + courierId));

        if (courier.getCurrentLat() == newLat && courier.getCurrentLng()==newLng) {
            throw new LocationNotChangedException("Kurye zaten girdiğiniz konumdadır. Güncelleme yapılmadı.");
        }
        double totalDistance=courier.getTotalDistance();
        double distanceDiff = distanceCalculator.calculateDistance(
                courier.getCurrentLat(), courier.getCurrentLng(),
                newLat, newLng
        );
        totalDistance += distanceDiff;
        courier.setTotalDistance(totalDistance);
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
                .map(CourierTrackingMapper.INSTANCE::toCourierDto)
                .orElseThrow(() -> new IllegalStateException("Uygun kurye bulunamadı."));

    }

    @Override
    public Double getTotalTravelDistance(Long courierId) {
        return courierRepository.findById(courierId)
                .map(Courier::getTotalDistance)
                .orElseThrow(() -> new CourierNotFoundException("Kurye bulunamadı: " + courierId));
    }

    @Override
    public CourierDto createCourier(CreateCourierRequest request) {
        courierRepository.findByName(request.getName()).ifPresent(c -> {
            throw new InvalidRequestException("Bu isme sahip bir kurye zaten mevcut: " + request.getName());
        });

        Courier newCourier = new Courier();
        newCourier.setName(request.getName());
        newCourier.setCurrentLat(CourierTrackingConstant.INITIAL_LATITUDE);
        newCourier.setCurrentLng(CourierTrackingConstant.INITIAL_LONGITUDE);
        newCourier.setTotalDistance(CourierTrackingConstant.INITIAL_DISTANCE);

        Courier savedCourier = courierRepository.save(newCourier);

        CourierLocationLog log = new CourierLocationLog();
        log.setCourier(newCourier);
        log.setLat(newCourier.getCurrentLat());
        log.setLng(newCourier.getCurrentLng());
        log.setTimestamp(LocalDateTime.now());
        locationLogRepository.save(log);

        return CourierTrackingMapper.INSTANCE.toCourierDto(savedCourier);
    }

    @Override
    public Page<CourierDto> getAllCouriers(Pageable pageable) {
        Page<Courier> courierPage = courierRepository.findAll(pageable);
        return courierPage.map(CourierTrackingMapper.INSTANCE::toCourierDto);
    }
}
