package com.migros.courier.service.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.migros.courier.constant.CourierTrackingConstant;
import com.migros.courier.dao.entity.Courier;
import com.migros.courier.dao.entity.FailedEventLog;
import com.migros.courier.dao.repository.FailedEventLogRepository;
import com.migros.courier.event.CourierLocationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@EnableScheduling
@RequiredArgsConstructor
public class EventRetryService {
    private final FailedEventLogRepository failedEventLogRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedRate = 10000)
    public void retryFailedEvents() {
        log.info("Failed Events Are Retrying");
        List<FailedEventLog> eventsToRetry = failedEventLogRepository.findByResolvedIsFalseAndRetryCountLessThan(CourierTrackingConstant.RETRY_COUNT);

        for (FailedEventLog failedEvent : eventsToRetry) {
            try {
                if (failedEvent.getEventType().equals("CourierLocationEvent")) {
                    Courier courierPayload = objectMapper.readValue(failedEvent.getEventPayload(), Courier.class);
                    eventPublisher.publishEvent(new CourierLocationEvent(this, courierPayload));
                    failedEvent.setResolved(true);
                }
            } catch (Exception e) {
                failedEvent.setRetryCount(failedEvent.getRetryCount() + 1);
                failedEvent.setErrorDetails("Retry failed: " + e.getMessage());
            } finally {
                failedEventLogRepository.save(failedEvent);
            }
        }
    }
}
