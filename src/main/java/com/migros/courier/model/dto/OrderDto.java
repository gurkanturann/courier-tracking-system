package com.migros.courier.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderDto {
    private Long id;
    private String orderStatus;
    private CourierDto assignedCourier;
    private StoreDto sourceStore;
}
