package com.migros.courier.service;

import com.migros.courier.constant.CourierTrackingConstant;
import com.migros.courier.dao.entity.Courier;
import com.migros.courier.dao.entity.Store;
import com.migros.courier.dao.entity.StoreEntranceLog;
import com.migros.courier.dao.repository.StoreEntranceLogRepository;
import com.migros.courier.dao.repository.StoreRepository;
import com.migros.courier.event.CourierLocationEvent;
import com.migros.courier.model.dto.StoreDto;
import com.migros.courier.service.distance.DistanceCalculatorService;
import com.migros.courier.service.store.impl.StoreServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.anyDouble;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StoreServiceTest {

    @Mock
    private StoreRepository storeRepository;
    @Mock
    private StoreEntranceLogRepository logRepository;
    @Mock
    private DistanceCalculatorService distanceCalculator;

    @InjectMocks
    private StoreServiceImpl storeService;

    private Store storeA;
    private Store storeB;
    private Courier courier;

    @BeforeEach
    void setUp() {
        storeA = new Store();
        storeA.setId(1L);
        storeA.setName("Ataşehir Migros");
        storeA.setLat(40.99);
        storeA.setLng(29.12);

        storeB = new Store();
        storeB.setId(2L);
        storeB.setName("Novada Migros");
        storeB.setLat(40.98);
        storeB.setLng(29.11);

        courier = new Courier();
        courier.setId(10L);
        courier.setName("Hızlı Kurye");
        courier.setCurrentLat(40.989);
        courier.setCurrentLng(29.121);
    }

    @Test
    void findNearestStore_shouldPopulateCacheAndReturnNearest() {
        List<Store> allStores = List.of(storeA, storeB);
        when(storeRepository.findAll()).thenReturn(allStores);
        when(distanceCalculator.calculateDistance(storeA.getLat(), storeA.getLng(), 40.0, 29.0)).thenReturn(1000.0);
        when(distanceCalculator.calculateDistance(storeB.getLat(), storeB.getLng(), 40.0, 29.0)).thenReturn(500.0);

        StoreDto nearestStore = storeService.findNearestStore(40.0, 29.0);

        assertNotNull(nearestStore);
        assertEquals(storeB.getName(), nearestStore.getName());
        verify(storeRepository, times(1)).findAll();

        storeService.findNearestStore(40.1, 29.1);
        verify(storeRepository, times(2)).findAll();
    }

    @Test
    void findNearestStore_shouldThrowExceptionWhenNoStoresAvailable() {
        when(storeRepository.findAll()).thenReturn(Collections.emptyList());

        assertThrows(IllegalStateException.class, () -> storeService.findNearestStore(40.0, 29.0));
    }

    @Test
    void handleCourierLocationUpdate_shouldSaveLogWhenCourierEntersRadius() {
        when(storeRepository.findAll()).thenReturn(List.of(storeA));
        storeService.findNearestStore(0,0);

        CourierLocationEvent event = new CourierLocationEvent(this, courier);
        when(distanceCalculator.calculateDistance(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(CourierTrackingConstant.APPROACH_RADIUS_METERS - 1);
        when(logRepository.existsByCourierAndStoreAndEntranceTimeAfter(any(), any(), any())).thenReturn(false);

        storeService.handleCourierLocationUpdate(event);

        verify(logRepository, times(1)).save(any(StoreEntranceLog.class));
    }

    @Test
    void handleCourierLocationUpdate_shouldNotSaveLogWhenCourierReEntersWithinCooldown() {
        when(storeRepository.findAll()).thenReturn(List.of(storeA));
        storeService.findNearestStore(0,0);

        CourierLocationEvent event = new CourierLocationEvent(this, courier);
        when(distanceCalculator.calculateDistance(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(CourierTrackingConstant.APPROACH_RADIUS_METERS - 1);
        when(logRepository.existsByCourierAndStoreAndEntranceTimeAfter(any(), any(), any())).thenReturn(true);

        storeService.handleCourierLocationUpdate(event);

        verify(logRepository, never()).save(any());
    }

    @Test
    void handleCourierLocationUpdate_shouldDoNothingWhenCourierIsOutsideRadius() {
        when(storeRepository.findAll()).thenReturn(List.of(storeA));
        storeService.findNearestStore(0,0);

        CourierLocationEvent event = new CourierLocationEvent(this, courier);
        when(distanceCalculator.calculateDistance(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(CourierTrackingConstant.APPROACH_RADIUS_METERS + 1);

        storeService.handleCourierLocationUpdate(event);

        verify(logRepository, never()).existsByCourierAndStoreAndEntranceTimeAfter(any(), any(), any());
        verify(logRepository, never()).save(any());
    }
}

