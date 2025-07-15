package com.migros.courier.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.migros.courier.dao.entity.FailedEventLog;
import com.migros.courier.dao.repository.FailedEventLogRepository;
import com.migros.courier.event.CourierLocationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class AsyncExceptionHandler implements AsyncUncaughtExceptionHandler {
    private final FailedEventLogRepository failedEventLogRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void handleUncaughtException(Throwable ex, Method method, Object... params) {
        System.err.println("!!! ASENKRON HATA YAKALANDI !!!");
        System.err.println("Metot Adı: " + method.getName());
        System.err.println("Hata Mesajı: " + ex.getMessage());

        // Hangi olay başarısız oldu?
        if (params.length > 0 && params[0] instanceof CourierLocationEvent event) {

            FailedEventLog failedEvent = new FailedEventLog();
            failedEvent.setEventType(event.getClass().getSimpleName());
            failedEvent.setErrorDetails(ex.getMessage());
            failedEvent.setFailedAt(LocalDateTime.now());

            try {
                // Olayın kendisini (içindeki kurye bilgisiyle) JSON olarak kaydet.
                String payload = objectMapper.writeValueAsString(event.getCourier());
                failedEvent.setEventPayload(payload);
            } catch (JsonProcessingException e) {
                failedEvent.setEventPayload("{\"error\": \"Payload serileştirilemedi.\"}");
            }

            failedEventLogRepository.save(failedEvent);
            System.err.println("Başarısız event veritabanına kaydedildi.");
        }
    }
}
