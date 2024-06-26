package com.hyunn.tableplanner.dto.review;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewCreateRequest {
    private Long reservationId;
    private int rating;
    private String comment;
}
