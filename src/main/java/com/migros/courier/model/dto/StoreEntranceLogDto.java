package com.migros.courier.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
@Getter
@Setter
public class StoreEntranceLogDto {
    String courierName;
    String storeName;
    LocalDateTime entranceTime;

}
