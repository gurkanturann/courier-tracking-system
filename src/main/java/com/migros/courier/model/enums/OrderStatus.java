package com.migros.courier.model.enums;

import lombok.Getter;
import lombok.Setter;

public enum OrderStatus {
    PENDING,
    ASSIGNED,
    PICKED_UP,
    DELIVERED,
    CANCELLED
}
