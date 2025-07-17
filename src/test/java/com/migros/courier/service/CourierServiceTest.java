package com.migros.courier.service;

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
import com.migros.courier.model.dto.CourierDto;
import com.migros.courier.service.courier.impl.CourierServiceImpl;
import com.migros.courier.service.distance.DistanceCalculatorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class CourierServiceTest {

    @Mock
    private CourierRepository courierRepository;
    @Mock
    private CourierLocationLogRepository locationLogRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private DistanceCalculatorService distanceCalculator;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private CourierServiceImpl courierService;

    private Courier courier;

    @BeforeEach
    void setUp() {
        courier = new Courier();
        courier.setId(1L);
        courier.setName("Test Kurye");
        courier.setCurrentLat(41.0);
        courier.setCurrentLng(29.0);
        courier.setTotalDistance(500.0);
    }

    @Test
    void createCourier_shouldSaveCourierAndLog() {
        CreateCourierRequest request = new CreateCourierRequest();
        request.setName("Yeni Kurye");

        Courier savedCourier = new Courier();
        savedCourier.setId(2L);
        savedCourier.setName("Yeni Kurye");
        savedCourier.setCurrentLat(CourierTrackingConstant.INITIAL_LATITUDE);
        savedCourier.setCurrentLng(CourierTrackingConstant.INITIAL_LONGITUDE);
        savedCourier.setTotalDistance(CourierTrackingConstant.INITIAL_DISTANCE);

        when(courierRepository.findByName("Yeni Kurye")).thenReturn(Optional.empty());
        when(courierRepository.save(any(Courier.class))).thenReturn(savedCourier);

        CourierDto resultDto = courierService.createCourier(request);

        assertNotNull(resultDto);
        assertEquals(savedCourier.getName(), resultDto.getName());

        verify(courierRepository, times(1)).save(any(Courier.class));
        verify(locationLogRepository, times(1)).save(any(CourierLocationLog.class));
    }

    @Test
    void createCourier_shouldThrowExceptionWhenCourierExists() {
        CreateCourierRequest request = new CreateCourierRequest();
        request.setName("Test Kurye");

        when(courierRepository.findByName("Test Kurye")).thenReturn(Optional.of(courier));

        assertThrows(InvalidRequestException.class, () -> courierService.createCourier(request));

        verify(courierRepository, never()).save(any());
        verify(locationLogRepository, never()).save(any());
    }

    @Test
    void updateCourierLocation_shouldUpdateAndLogAndPublishEvent() {
        long courierId = 1L;
        double newLat = 41.1;
        double newLng = 29.1;

        when(courierRepository.findById(courierId)).thenReturn(Optional.of(courier));
        when(distanceCalculator.calculateDistance(41.0, 29.0, newLat, newLng)).thenReturn(1000.0);

        courierService.updateCourierLocation(courierId, newLat, newLng);

        ArgumentCaptor<Courier> courierCaptor = ArgumentCaptor.forClass(Courier.class);
        verify(courierRepository, times(1)).save(courierCaptor.capture());
        Courier savedCourier = courierCaptor.getValue();

        assertEquals(1500.0, savedCourier.getTotalDistance());
        assertEquals(newLat, savedCourier.getCurrentLat());
        assertEquals(newLng, savedCourier.getCurrentLng());

        verify(locationLogRepository, times(1)).save(any(CourierLocationLog.class));
        verify(eventPublisher, times(1)).publishEvent(any(CourierLocationEvent.class));
    }

    @Test
    void updateCourierLocation_shouldThrowExceptionIfLocationNotChanged() {
        when(courierRepository.findById(1L)).thenReturn(Optional.of(courier));

        assertThrows(LocationNotChangedException.class, () ->
                courierService.updateCourierLocation(1L, 41.0, 29.0)
        );

        verify(courierRepository, never()).save(any());
    }

    @Test
    void getTotalTravelDistance_shouldReturnDistance() {
        when(courierRepository.findById(1L)).thenReturn(Optional.of(courier));

        Double distance = courierService.getTotalTravelDistance(1L);

        assertEquals(500.0, distance);
    }

    @Test
    void getTotalTravelDistance_shouldThrowExceptionIfCourierNotFound() {
        when(courierRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CourierNotFoundException.class, () -> courierService.getTotalTravelDistance(99L));
    }

    @Test
    void getAllCouriers_shouldReturnPagedCouriers() {
        Pageable pageable = PageRequest.of(0, 5);
        List<Courier> courierList = Collections.singletonList(courier);
        Page<Courier> courierPage = new PageImpl<>(courierList, pageable, 1);

        when(courierRepository.findAll(pageable)).thenReturn(courierPage);

        Page<CourierDto> result = courierService.getAllCouriers(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Test Kurye", result.getContent().get(0).getName());
    }

    @Test
    void findNearestCourier_shouldReturnNearestAvailableCourier() {
        Courier busyCourier = new Courier();
        busyCourier.setId(2L);

        Courier farCourier = new Courier();
        farCourier.setId(3L);
        farCourier.setCurrentLat(45.0);
        farCourier.setCurrentLng(35.0);

        List<Courier> allCouriers = List.of(courier, busyCourier, farCourier);
        List<Courier> busyCouriers = List.of(busyCourier);

        when(orderRepository.findCouriersWithOrdersInStatus(any())).thenReturn(busyCouriers);
        when(courierRepository.findAll()).thenReturn(allCouriers);
        when(distanceCalculator.calculateDistance(courier.getCurrentLat(), courier.getCurrentLng(), 41.05, 29.05)).thenReturn(100.0);
        when(distanceCalculator.calculateDistance(farCourier.getCurrentLat(), farCourier.getCurrentLng(), 41.05, 29.05)).thenReturn(5000.0);

        CourierDto result = courierService.findNearestCourier(41.05, 29.05);

        assertNotNull(result);
        assertEquals(courier.getId(), result.getId());
    }

    @Test
    void findNearestCourier_shouldThrowExceptionWhenNoAvailableCourier() {
        when(orderRepository.findCouriersWithOrdersInStatus(any())).thenReturn(Collections.emptyList());
        when(courierRepository.findAll()).thenReturn(Collections.emptyList());

        assertThrows(IllegalStateException.class, () -> courierService.findNearestCourier(41.0, 29.0));
    }
}
