package com.hyunn.tableplanner.dto.reservation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationApprovalRequest {
    private Long reservationId;
    private String status;
}
