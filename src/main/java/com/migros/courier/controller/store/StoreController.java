package com.migros.courier.controller.store;

import com.migros.courier.model.dto.StoreEntranceLogDto;
import com.migros.courier.service.store.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/store")
@RequiredArgsConstructor
public class StoreController {
    private final StoreService storeService;

    @GetMapping("/entrances")
    public ResponseEntity<List<StoreEntranceLogDto>> getStoreEntranceLogs() {
        List<StoreEntranceLogDto> logs = storeService.getAllLogs();
        return ResponseEntity.ok(logs);
    }
}
