package com.migros.courier.service.store.impl;

import com.migros.courier.constant.CourierTrackingConstant;
import com.migros.courier.dao.entity.Courier;
import com.migros.courier.dao.entity.Store;
import com.migros.courier.dao.entity.StoreEntranceLog;
import com.migros.courier.dao.repository.StoreEntranceLogRepository;
import com.migros.courier.dao.repository.StoreRepository;
import com.migros.courier.event.CourierLocationEvent;
import com.migros.courier.mapper.CourierTrackingMapper;
import com.migros.courier.model.dto.StoreDto;
import com.migros.courier.model.dto.StoreEntranceLogDto;
import com.migros.courier.service.distance.DistanceCalculatorService;
import com.migros.courier.service.store.StoreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
@Slf4j
@Service
public class StoreServiceImpl implements StoreService {
    private final StoreRepository storeRepository;
    private final StoreEntranceLogRepository logRepository;
    private final DistanceCalculatorService distanceCalculator;
    private List<Store> storeCache ;

    public StoreServiceImpl(StoreRepository storeRepository, StoreEntranceLogRepository logRepository, DistanceCalculatorService distanceCalculator, List<Store> storeCache) {
        this.storeRepository = storeRepository;
        this.logRepository = logRepository;
        this.distanceCalculator = distanceCalculator;
        this.storeCache = Collections.emptyList();
    }

    @Async
    @EventListener(CourierLocationEvent.class)
    @Transactional
    public void handleCourierLocationUpdate(CourierLocationEvent event) {
        Courier courier = event.getCourier();
        for (Store store : storeCache) {
            double distance = distanceCalculator.calculateDistance(
                    courier.getCurrentLat(), courier.getCurrentLng(), store.getLat(), store.getLng());

            if (distance <= CourierTrackingConstant.APPROACH_RADIUS_METERS) {
                boolean recentlyEntered = logRepository.existsByCourierAndStoreAndEntranceTimeAfter(
                        courier, store, LocalDateTime.now().minusMinutes(CourierTrackingConstant.RE_ENTRANCE_COOL_DOWN_MINUTES));

                if (!recentlyEntered) {
                    StoreEntranceLog storeEntranceLog = new StoreEntranceLog();
                    storeEntranceLog.setCourier(courier);
                    storeEntranceLog.setStore(store);
                    storeEntranceLog.setEntranceTime(LocalDateTime.now());
                    logRepository.save(storeEntranceLog);
                    log.info("LOG: Courier: " + courier.getName() + "approached to store: " + store.getName());
                }
            }
        }
    }

    private List<Store> getStoreCache() {
        log.info("Stores cache loading...");
        this.storeCache = storeRepository.findAll();
        return this.storeCache;

    }

    public StoreDto findNearestStore(double lat, double lng) {
        List<Store> currentStores = getStoreCache();
        if (currentStores.isEmpty()) {
            throw new IllegalStateException("Sistemde kayıtlı mağaza bulunmuyor.");
        }

        return currentStores.stream()
                .min((s1, s2) -> Double.compare(
                        distanceCalculator.calculateDistance(s1.getLat(), s1.getLng(), lat, lng),
                        distanceCalculator.calculateDistance(s2.getLat(), s2.getLng(), lat, lng)
                ))
                .map(CourierTrackingMapper.INSTANCE::toStoreDto)
                .orElseThrow();
    }

    @Transactional(readOnly = true)
    public List<StoreEntranceLogDto> getAllLogs() {
        return logRepository.findAll().stream()
                .map(CourierTrackingMapper.INSTANCE::toStoreEntranceLogDto)
                .toList();
    }
}
