package com.hyunn.tableplanner.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class StoreException extends RuntimeException {
    private final HttpStatus status;

    private StoreException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public static StoreException storeNotFound(Long id) {
        return new StoreException("Store not found with id: " + id, HttpStatus.NOT_FOUND);
    }

    public static StoreException storeAlreadyExists(String name) {
        return new StoreException("Store already exists with name: " + name, HttpStatus.CONFLICT);
    }

    public static StoreException unauthorizedException(String username, String storeName) {
        return new StoreException("User " + username + " is not authorized to access store " + storeName,
                HttpStatus.UNAUTHORIZED);
    }
}
