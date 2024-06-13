package com.hyunn.tableplanner.dto.reservation;

import com.hyunn.tableplanner.model.types.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationUpdateResponse {
    private String storeName;
    private LocalDateTime reservationTime;
    private ReservationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
