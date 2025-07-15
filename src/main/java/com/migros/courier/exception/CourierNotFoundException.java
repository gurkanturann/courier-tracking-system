package com.migros.courier.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class CourierNotFoundException extends RuntimeException {
    private Integer code;

    public CourierNotFoundException(String message) {
        super(message);
    }

    public CourierNotFoundException(String message, int code) {
        super(message);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}
