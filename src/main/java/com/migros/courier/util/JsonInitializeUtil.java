package com.migros.courier.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.migros.courier.dao.entity.Courier;
import com.migros.courier.dao.entity.Store;
import com.migros.courier.dao.repository.CourierRepository;
import com.migros.courier.dao.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.Comment;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JsonInitializeUtil implements CommandLineRunner {
    private final StoreRepository storeRepository;
    private final CourierRepository courierRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public void run(String... args) throws Exception {
        if (storeRepository.count() == 0) {
            InputStream inputStream = TypeReference.class.getResourceAsStream("/store.json");
            List<Store> stores = objectMapper.readValue(inputStream, new TypeReference<List<Store>>() {});
            storeRepository.saveAll(stores);
            System.out.println(stores.size() + " adet mağaza veritabanına yüklendi.");
        }

        if (courierRepository.count() == 0) {
            InputStream inputStream = TypeReference.class.getResourceAsStream("/courier.json");
            List<Courier> couriers = objectMapper.readValue(inputStream, new TypeReference<List<Courier>>() {});
            couriers.forEach(c -> {
                c.setCurrentLat(c.getCurrentLat());
                c.setCurrentLng(c.getCurrentLng());
            });
            courierRepository.saveAll(couriers);
            System.out.println(couriers.size() + " adet kurye veritabanına yüklendi.");
        }

    }
}
