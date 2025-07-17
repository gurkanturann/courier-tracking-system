package com.migros.courier.controller.order.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderRequest {
    private double customerLat;
    private double customerLng;
}
