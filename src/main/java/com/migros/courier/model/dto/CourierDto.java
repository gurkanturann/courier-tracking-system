package com.migros.courier.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CourierDto {
    private Long id;
    private String name;
    private double currentLat;
    private double currentLng;
    private double totalDistance;
}
