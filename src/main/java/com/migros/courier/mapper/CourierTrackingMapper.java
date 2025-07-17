package com.migros.courier.mapper;

import com.migros.courier.dao.entity.Courier;
import com.migros.courier.dao.entity.Order;
import com.migros.courier.dao.entity.Store;
import com.migros.courier.dao.entity.StoreEntranceLog;
import com.migros.courier.model.dto.CourierDto;
import com.migros.courier.model.dto.OrderDto;
import com.migros.courier.model.dto.StoreDto;
import com.migros.courier.model.dto.StoreEntranceLogDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface CourierTrackingMapper {
    CourierTrackingMapper INSTANCE = Mappers.getMapper(CourierTrackingMapper.class);
    CourierDto toCourierDto(Courier courier);

    StoreDto toStoreDto(Store store);

    @Mapping(source ="status", target = "orderStatus")
    OrderDto toOrderDto(Order order);

    @Mapping(source = "courier.name", target = "courierName")
    @Mapping(source = "store.name", target = "storeName")
    StoreEntranceLogDto toStoreEntranceLogDto(StoreEntranceLog log);
}
