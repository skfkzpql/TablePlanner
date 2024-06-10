package com.hyunn.tableplanner.dto.review;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {
    private Long id;
    private Long storeId;
    private Long userId;
    private int rating;
    private String comment;
    private String createdAt;
}
