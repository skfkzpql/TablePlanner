package com.hyunn.tableplanner.dto.store;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreSummaryResponse {
    private Long id;
    private String name;
    private String location;
    private Double averageRating;
}
