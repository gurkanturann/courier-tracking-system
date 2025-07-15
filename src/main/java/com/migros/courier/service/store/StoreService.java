package com.migros.courier.service.store;

import com.migros.courier.event.CourierLocationEvent;
import com.migros.courier.model.dto.StoreDto;
import com.migros.courier.model.dto.StoreEntranceLogDto;

import java.util.List;

public interface StoreService {
    void handleCourierLocationUpdate(CourierLocationEvent event);

    StoreDto findNearestStore(double lat, double lng);

    List<StoreEntranceLogDto> getAllLogs();
}
