package com.migros.courier.controller.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderRequest {
    private double customerLat;
    private double customerLng;
}
