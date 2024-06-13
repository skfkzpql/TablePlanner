package com.hyunn.tableplanner.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class UserException extends RuntimeException {
    private final HttpStatus status;

    private UserException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public static UserException usernameNotFound(String username) {
        return new UserException("Username not found: " + username, HttpStatus.NOT_FOUND);
    }

    public static UserException usernameExists(String username) {
        return new UserException("Username already exists: " + username, HttpStatus.CONFLICT);
    }

    public static UserException emailExists(String email) {
        return new UserException("Email already exists: " + email, HttpStatus.CONFLICT);
    }

    public static UserException userAlreadyPartner() {
        return new UserException("User is already a partner", HttpStatus.CONFLICT);
    }

    public static UserException unauthorizedException() {
        return new UserException("Unauthorized Exception", HttpStatus.UNAUTHORIZED);
    }
}
