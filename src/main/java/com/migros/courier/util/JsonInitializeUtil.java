package com.migros.courier.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.migros.courier.dao.entity.Courier;
import com.migros.courier.dao.entity.CourierLocationLog;
import com.migros.courier.dao.entity.Store;
import com.migros.courier.dao.repository.CourierLocationLogRepository;
import com.migros.courier.dao.repository.CourierRepository;
import com.migros.courier.dao.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Slf4j
@Component
@RequiredArgsConstructor
public class JsonInitializeUtil implements CommandLineRunner {
    private final StoreRepository storeRepository;
    private final CourierRepository courierRepository;
    private final CourierLocationLogRepository locationLogRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public void run(String... args) throws Exception {
        if (storeRepository.count() == 0) {
            InputStream inputStream = TypeReference.class.getResourceAsStream("/store.json");
            List<Store> stores = objectMapper.readValue(inputStream, new TypeReference<List<Store>>() {});
            storeRepository.saveAll(stores);
            log.info(stores.size() + " stores uploaded to db");
        }

        if (courierRepository.count() == 0) {
            InputStream inputStream = TypeReference.class.getResourceAsStream("/courier.json");
            List<Courier> couriers = objectMapper.readValue(inputStream, new TypeReference<List<Courier>>() {});
            couriers.forEach(c -> {
                c.setCurrentLat(c.getCurrentLat());
                c.setCurrentLng(c.getCurrentLng());
            });
            courierRepository.saveAll(couriers);
            log.info(couriers.size() + " couriers uploaded to db");

            log.info("Couriers starting positions logging...");
            List<CourierLocationLog> initialLogs = new ArrayList<>();
            for (Courier courier : couriers) {
                CourierLocationLog log = new CourierLocationLog();
                log.setCourier(courier);
                log.setLat(courier.getCurrentLat());
                log.setLng(courier.getCurrentLng());
                log.setTimestamp(LocalDateTime.now());
                initialLogs.add(log);
            }
            locationLogRepository.saveAll(initialLogs);
        }

    }
}
