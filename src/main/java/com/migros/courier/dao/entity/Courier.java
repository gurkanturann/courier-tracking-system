package com.migros.courier.dao.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
public class Courier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private double currentLat;
    private double currentLng;
    private double totalDistance = 0.0;
    @OneToMany(mappedBy = "assignedCourier", fetch = FetchType.LAZY)
    private List<Order> orders;

}
