package com.migros.courier.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.migros.courier.constant.CourierTrackingConstant;
import com.migros.courier.dao.entity.Courier;
import com.migros.courier.dao.entity.FailedEventLog;
import com.migros.courier.dao.repository.FailedEventLogRepository;
import com.migros.courier.event.CourierLocationEvent;
import com.migros.courier.service.event.EventRetryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventRetryServiceTest {

    @Mock
    private FailedEventLogRepository failedEventLogRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private EventRetryService eventRetryService;

    private FailedEventLog successfulEventLog;
    private FailedEventLog failingEventLog;
    private Courier courier;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        courier = new Courier();
        courier.setId(1L);
        courier.setName("Test Kurye");

        String payload = objectMapper.writeValueAsString(courier);

        successfulEventLog = new FailedEventLog();
        successfulEventLog.setId(101L);
        successfulEventLog.setEventType("CourierLocationEvent");
        successfulEventLog.setEventPayload(payload);
        successfulEventLog.setResolved(false);
        successfulEventLog.setRetryCount(0);

        failingEventLog = new FailedEventLog();
        failingEventLog.setId(102L);
        failingEventLog.setEventType("CourierLocationEvent");
        failingEventLog.setEventPayload(payload);
        failingEventLog.setResolved(false);
        failingEventLog.setRetryCount(1);
    }

    @Test
    void retryFailedEvents_shouldRepublishEventAndMarkAsResolvedOnSuccess() {
        when(failedEventLogRepository.findByResolvedIsFalseAndRetryCountLessThan(CourierTrackingConstant.RETRY_COUNT))
                .thenReturn(List.of(successfulEventLog));

        doNothing().when(eventPublisher).publishEvent(any(CourierLocationEvent.class));

        eventRetryService.retryFailedEvents();

        verify(eventPublisher, times(1)).publishEvent(any(CourierLocationEvent.class));
        ArgumentCaptor<FailedEventLog> logCaptor = ArgumentCaptor.forClass(FailedEventLog.class);
        verify(failedEventLogRepository, times(1)).save(logCaptor.capture());

        FailedEventLog savedLog = logCaptor.getValue();
        assertTrue(savedLog.isResolved());
        assertEquals(0, savedLog.getRetryCount());
    }

    @Test
    void retryFailedEvents_shouldIncrementRetryCountOnFailure() {
        when(failedEventLogRepository.findByResolvedIsFalseAndRetryCountLessThan(CourierTrackingConstant.RETRY_COUNT))
                .thenReturn(List.of(failingEventLog));

        doThrow(new RuntimeException("Test exception")).when(eventPublisher).publishEvent(any(CourierLocationEvent.class));

        eventRetryService.retryFailedEvents();

        verify(eventPublisher, times(1)).publishEvent(any(CourierLocationEvent.class));
        ArgumentCaptor<FailedEventLog> logCaptor = ArgumentCaptor.forClass(FailedEventLog.class);
        verify(failedEventLogRepository, times(1)).save(logCaptor.capture());

        FailedEventLog savedLog = logCaptor.getValue();
        assertFalse(savedLog.isResolved());
        assertEquals(2, savedLog.getRetryCount());
        assertTrue(savedLog.getErrorDetails().contains("Retry failed"));
    }

    @Test
    void retryFailedEvents_shouldDoNothingWhenNoEventsToRetry() {
        when(failedEventLogRepository.findByResolvedIsFalseAndRetryCountLessThan(CourierTrackingConstant.RETRY_COUNT))
                .thenReturn(Collections.emptyList());

        eventRetryService.retryFailedEvents();

        verify(eventPublisher, never()).publishEvent(any());
        verify(failedEventLogRepository, never()).save(any());
    }

    @Test
    void retryFailedEvents_shouldHandleJsonProcessingException() throws JsonProcessingException {
        successfulEventLog.setEventPayload("invalid-json");
        when(failedEventLogRepository.findByResolvedIsFalseAndRetryCountLessThan(CourierTrackingConstant.RETRY_COUNT))
                .thenReturn(List.of(successfulEventLog));

        eventRetryService.retryFailedEvents();

        verify(eventPublisher, never()).publishEvent(any());
        ArgumentCaptor<FailedEventLog> logCaptor = ArgumentCaptor.forClass(FailedEventLog.class);
        verify(failedEventLogRepository, times(1)).save(logCaptor.capture());

        FailedEventLog savedLog = logCaptor.getValue();
        assertEquals(1, savedLog.getRetryCount());
        assertTrue(savedLog.getErrorDetails().contains("Unrecognized token 'invalid'"));
    }
}
