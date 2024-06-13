package com.hyunn.tableplanner.dto.reservation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationUpdateRequest {
    private Long id;
    private LocalDateTime newReservationTime;
}
