package com.migros.courier.controller.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LocationRequest {
    private double lat;
    private double lng;
}
