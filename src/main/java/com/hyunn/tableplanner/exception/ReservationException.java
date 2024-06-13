package com.hyunn.tableplanner.exception;

import com.hyunn.tableplanner.model.types.ReservationStatus;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ReservationException extends RuntimeException {
    private final HttpStatus status;

    private ReservationException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public static ReservationException reservationNotFoundException(Long id) {
        return new ReservationException("Reservation with ID " + id + " not found.", HttpStatus.NOT_FOUND);
    }

    public static ReservationException confirmationNumberNotFoundException(String confirmationNumber) {
        return new ReservationException("Reservation with confirmation number " + confirmationNumber + " not found.",
                HttpStatus.NOT_FOUND);
    }


    public static ReservationException invalidReservationTimeException(String message) {
        return new ReservationException(message, HttpStatus.BAD_REQUEST);
    }

    public static ReservationException reservationStatusException(ReservationStatus status) {
        return new ReservationException("Invalid reservation status: " + status, HttpStatus.BAD_REQUEST);
    }

    public static ReservationException accessDeniedException(String username, Long reservationId) {
        return new ReservationException("User" + username + "is not authorized to access the reservation" + reservationId,
                HttpStatus.FORBIDDEN);
    }
}
