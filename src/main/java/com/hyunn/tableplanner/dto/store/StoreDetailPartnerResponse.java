package com.hyunn.tableplanner.dto.store;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreDetailPartnerResponse {
    private Long id;
    private String name;
    private String location;
    private String description;
    private double rating;
    private int reviews;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
