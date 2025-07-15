package com.migros.courier.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class LocationNotChangedException extends RuntimeException {

    public LocationNotChangedException(String message) {
        super(message);
    }
}
