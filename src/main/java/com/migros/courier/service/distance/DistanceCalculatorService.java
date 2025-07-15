package com.migros.courier.service.distance;

public interface DistanceCalculatorService {
    double calculateDistance(double lat1, double lng1, double lat2, double lng2);
}
