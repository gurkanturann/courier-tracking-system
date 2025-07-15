package com.migros.courier.service.order.impl;

import com.migros.courier.controller.request.OrderRequest;
import com.migros.courier.dao.entity.Courier;
import com.migros.courier.dao.entity.Order;
import com.migros.courier.dao.entity.Store;
import com.migros.courier.dao.repository.CourierRepository;
import com.migros.courier.dao.repository.OrderRepository;
import com.migros.courier.dao.repository.StoreRepository;
import com.migros.courier.event.CourierLocationEvent;
import com.migros.courier.exception.CourierNotFoundException;
import com.migros.courier.exception.StoreNotFoundException;
import com.migros.courier.mapper.CourierTrackingMapper;
import com.migros.courier.model.dto.CourierDto;
import com.migros.courier.model.dto.OrderDto;
import com.migros.courier.model.dto.StoreDto;
import com.migros.courier.model.enums.OrderStatus;
import com.migros.courier.service.courier.CourierService;
import com.migros.courier.service.distance.DistanceCalculatorService;
import com.migros.courier.service.order.OrderService;
import com.migros.courier.service.store.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private static final double DELIVERY_RADIUS_METERS = 100.0;
    private final OrderRepository orderRepository;
    private final CourierRepository courierRepository;
    private final StoreRepository storeRepository;
    private final CourierService courierService;
    private final StoreService storeService;
    private final CourierTrackingMapper mapper;

    private final DistanceCalculatorService distanceCalculator;
    @Override
    @Transactional
    public OrderDto createOrder(OrderRequest requestDto) {
        CourierDto nearestCourierDto = courierService.findNearestCourier(requestDto.getCustomerLat(), requestDto.getCustomerLng());

        StoreDto nearestStoreDto = storeService.findNearestStore(nearestCourierDto.getCurrentLat(), nearestCourierDto.getCurrentLng());

        Courier courierEntity = courierRepository.findById(nearestCourierDto.getId())
                .orElseThrow(() -> new CourierNotFoundException("Atama için kurye bulunamadı: " + nearestCourierDto.getId()));

        Store storeEntity = storeRepository.findByName(nearestStoreDto.getName())
                .orElseThrow(() -> new StoreNotFoundException("Atama için mağaza bulunamadı: " + nearestStoreDto.getName()));

        Order newOrder = new Order();
        newOrder.setCustomerLat(requestDto.getCustomerLat());
        newOrder.setCustomerLng(requestDto.getCustomerLng());
        newOrder.setAssignedCourier(courierEntity);
        newOrder.setSourceStore(storeEntity);
        newOrder.setStatus(OrderStatus.ASSIGNED);
        Order savedOrder = orderRepository.save(newOrder);

        return mapper.toOrderDto(savedOrder);
    }

    @Override
    @Async
    @EventListener(CourierLocationEvent.class)
    @Transactional
    public void updateOrderStatusBasedOnLocation(CourierLocationEvent event) {
        Courier courier = event.getCourier();

        List<Order> assignedOrders = orderRepository.findByAssignedCourierAndStatus(courier, OrderStatus.ASSIGNED);
        for (Order order : assignedOrders) {
            double distanceToStore = distanceCalculator.calculateDistance(
                    courier.getCurrentLat(),
                    courier.getCurrentLng(),
                    order.getSourceStore().getLat(),
                    order.getSourceStore().getLng()
            );

            if (distanceToStore <= DELIVERY_RADIUS_METERS) {
                order.setStatus(OrderStatus.PICKED_UP);
                orderRepository.save(order);
                System.out.println("DURUM GÜNCELLEMESİ: Sipariş " + order.getId() + " durumu PICKED_UP olarak güncellendi.");
            }
        }

        List<Order> pickedUpOrders = orderRepository.findByAssignedCourierAndStatus(courier, OrderStatus.PICKED_UP);
        for (Order order : pickedUpOrders) {
            double distanceToCustomer = distanceCalculator.calculateDistance(
                    courier.getCurrentLat(),
                    courier.getCurrentLng(),
                    order.getCustomerLat(),
                    order.getCustomerLng()
            );

            if (distanceToCustomer <= DELIVERY_RADIUS_METERS) {
                order.setStatus(OrderStatus.DELIVERED);
                orderRepository.save(order);
                System.out.println("DURUM GÜNCELLEMESİ: Sipariş " + order.getId() + " durumu DELIVERED olarak güncellendi.");
            }
        }
    }


}
