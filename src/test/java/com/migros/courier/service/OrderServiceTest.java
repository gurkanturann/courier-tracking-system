package com.migros.courier.service;

import com.migros.courier.constant.CourierTrackingConstant;
import com.migros.courier.controller.order.request.OrderRequest;
import com.migros.courier.dao.entity.Courier;
import com.migros.courier.dao.entity.Order;
import com.migros.courier.dao.entity.Store;
import com.migros.courier.dao.repository.CourierRepository;
import com.migros.courier.dao.repository.OrderRepository;
import com.migros.courier.dao.repository.StoreRepository;
import com.migros.courier.event.CourierLocationEvent;
import com.migros.courier.exception.CourierNotFoundException;
import com.migros.courier.model.dto.CourierDto;
import com.migros.courier.model.dto.OrderDto;
import com.migros.courier.model.dto.StoreDto;
import com.migros.courier.model.enums.OrderStatus;
import com.migros.courier.service.courier.CourierService;
import com.migros.courier.service.distance.DistanceCalculatorService;
import com.migros.courier.service.order.impl.OrderServiceImpl;
import com.migros.courier.service.store.StoreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.anyDouble;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private CourierRepository courierRepository;
    @Mock
    private StoreRepository storeRepository;
    @Mock
    private CourierService courierService;
    @Mock
    private StoreService storeService;
    @Mock
    private DistanceCalculatorService distanceCalculator;

    @InjectMocks
    private OrderServiceImpl orderService;

    private StoreDto storeDto;
    private CourierDto courierDto;
    private Store store;
    private Courier courier;
    private Order order;

    @BeforeEach
    void setUp() {
        storeDto = new StoreDto();
        storeDto.setName("Test Store");
        storeDto.setLat(41.0);
        storeDto.setLng(29.0);

        courierDto = new CourierDto();
        courierDto.setId(1L);

        store = new Store();
        store.setId(10L);
        store.setName("Test Store");
        store.setLat(41.0);
        store.setLng(29.0);

        courier = new Courier();
        courier.setId(1L);
        courier.setCurrentLat(41.01);
        courier.setCurrentLng(29.01);

        order = new Order();
        order.setId(100L);
        order.setAssignedCourier(courier);
        order.setSourceStore(store);
        order.setCustomerLat(41.02);
        order.setCustomerLng(29.02);
    }

    @Test
    void createOrder_shouldSucceed() {
        OrderRequest request = new OrderRequest();
        request.setCustomerLat(40.9);
        request.setCustomerLng(28.9);

        Order savedOrder = new Order();
        savedOrder.setId(101L);

        when(storeService.findNearestStore(anyDouble(), anyDouble())).thenReturn(storeDto);
        when(courierService.findNearestCourier(anyDouble(), anyDouble())).thenReturn(courierDto);
        when(courierRepository.findById(1L)).thenReturn(Optional.of(courier));
        when(storeRepository.findByName("Test Store")).thenReturn(Optional.of(store));
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        OrderDto result = orderService.createOrder(request);

        assertNotNull(result);
        assertEquals(savedOrder.getId(), result.getId());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void createOrder_shouldThrowExceptionWhenCourierNotFoundInDb() {
        OrderRequest request = new OrderRequest();

        when(storeService.findNearestStore(anyDouble(), anyDouble())).thenReturn(storeDto);
        when(courierService.findNearestCourier(anyDouble(), anyDouble())).thenReturn(courierDto);
        when(courierRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CourierNotFoundException.class, () -> orderService.createOrder(request));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void updateOrderStatus_shouldChangeFromAssignedToPickedUp() {
        order.setStatus(OrderStatus.ASSIGNED);
        CourierLocationEvent event = new CourierLocationEvent(this, courier);

        when(orderRepository.findByAssignedCourierAndStatus(courier, OrderStatus.ASSIGNED)).thenReturn(List.of(order));
        when(orderRepository.findByAssignedCourierAndStatus(courier, OrderStatus.PICKED_UP)).thenReturn(Collections.emptyList());
        when(distanceCalculator.calculateDistance(courier.getCurrentLat(), courier.getCurrentLng(), store.getLat(), store.getLng()))
                .thenReturn(CourierTrackingConstant.APPROACH_RADIUS_METERS - 10);

        orderService.updateOrderStatusBasedOnLocation(event);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository, times(1)).save(orderCaptor.capture());
        assertEquals(OrderStatus.PICKED_UP, orderCaptor.getValue().getStatus());
    }

    @Test
    void updateOrderStatus_shouldChangeFromPickedUpToDelivered() {
        order.setStatus(OrderStatus.PICKED_UP);
        CourierLocationEvent event = new CourierLocationEvent(this, courier);

        when(orderRepository.findByAssignedCourierAndStatus(courier, OrderStatus.ASSIGNED)).thenReturn(Collections.emptyList());
        when(orderRepository.findByAssignedCourierAndStatus(courier, OrderStatus.PICKED_UP)).thenReturn(List.of(order));
        when(distanceCalculator.calculateDistance(courier.getCurrentLat(), courier.getCurrentLng(), order.getCustomerLat(), order.getCustomerLng()))
                .thenReturn(CourierTrackingConstant.APPROACH_RADIUS_METERS - 10);

        orderService.updateOrderStatusBasedOnLocation(event);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository, times(1)).save(orderCaptor.capture());
        assertEquals(OrderStatus.DELIVERED, orderCaptor.getValue().getStatus());
    }

    @Test
    void updateOrderStatus_shouldDoNothingWhenCourierIsFar() {
        order.setStatus(OrderStatus.ASSIGNED);
        CourierLocationEvent event = new CourierLocationEvent(this, courier);

        when(orderRepository.findByAssignedCourierAndStatus(courier, OrderStatus.ASSIGNED)).thenReturn(List.of(order));
        when(orderRepository.findByAssignedCourierAndStatus(courier, OrderStatus.PICKED_UP)).thenReturn(Collections.emptyList());
        when(distanceCalculator.calculateDistance(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(CourierTrackingConstant.APPROACH_RADIUS_METERS + 50);

        orderService.updateOrderStatusBasedOnLocation(event);

        verify(orderRepository, never()).save(any(Order.class));
    }
}
