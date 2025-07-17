package com.migros.courier.service;

import com.migros.courier.service.distance.impl.DistanceCalculatorServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DistanceCalculatorServiceTest {

    private DistanceCalculatorServiceImpl distanceCalculatorService;

    @BeforeEach
    void setUp() {
        distanceCalculatorService = new DistanceCalculatorServiceImpl();
    }

    @Test
    void calculateDistance_shouldReturnZeroForSameCoordinates() {
        double lat = 40.9923307;
        double lng = 29.1244229;

        double distance = distanceCalculatorService.calculateDistance(lat, lng, lat, lng);

        assertEquals(0.0, distance);
    }

    @Test
    void calculateDistance_shouldReturnCorrectDistanceForKnownCoordinates() {
        double lat1 = 40.9923307;
        double lng1 = 29.1244229;
        double lat2 = 40.986106;
        double lng2 = 29.1161293;

        double expectedDistance = 981.656843521228;
        double delta = 0.1;

        double actualDistance = distanceCalculatorService.calculateDistance(lat1, lng1, lat2, lng2);

        assertEquals(expectedDistance, actualDistance, delta);
    }
}
