package com.migros.courier.event;

import com.migros.courier.dao.entity.Courier;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEvent;
public class CourierLocationEvent extends ApplicationEvent {
    private final Courier courier;

    public CourierLocationEvent(Object source, Courier courier) {
        super(source);
        this.courier = courier;
    }

    public Courier getCourier() {
        return courier;
    }
}
