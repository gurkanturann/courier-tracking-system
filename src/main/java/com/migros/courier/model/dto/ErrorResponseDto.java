package com.migros.courier.model.dto;

import java.time.LocalDateTime;

public record ErrorResponseDto (
    int statusCode,
    String message,
    LocalDateTime timestamp
    ){}
