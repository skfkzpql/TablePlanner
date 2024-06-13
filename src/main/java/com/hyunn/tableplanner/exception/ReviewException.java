package com.hyunn.tableplanner.exception;

import org.springframework.http.HttpStatus;

public class ReviewException extends RuntimeException {
    private final HttpStatus status;

    public ReviewException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public static ReviewException notReviewOwner(String username, Long reservationId) {
        return new ReviewException(
                String.format("User '%s' is not the owner of reservation with ID %d.", username, reservationId),
                HttpStatus.FORBIDDEN
        );
    }

    public static ReviewException reservationNotCompleted(Long reservationId) {
        return new ReviewException(
                String.format("Reservation with ID %d is not completed and cannot be reviewed.", reservationId),
                HttpStatus.FORBIDDEN
        );
    }

    public static ReviewException alreadyReviewed(Long reservationId) {
        return new ReviewException(
                String.format("Reservation with ID %d has already been reviewed.", reservationId),
                HttpStatus.FORBIDDEN
        );
    }

    public static ReviewException unauthorizedAction(String username, Long reviewId) {
        return new ReviewException(
                String.format("User '%s' is not authorized to perform action on review with ID %d.",
                        username,
                        reviewId),
                HttpStatus.UNAUTHORIZED
        );
    }

    public static ReviewException reviewNotFound(Long reviewId) {
        return new ReviewException(
                String.format("Review with ID %d not found.", reviewId),
                HttpStatus.NOT_FOUND
        );
    }

    public HttpStatus getStatus() {
        return status;
    }
}
