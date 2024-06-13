package com.hyunn.tableplanner.dto.store;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreDetailUserResponse {
    private Long id;
    private String name;
    private String location;
    private String description;
    private double rating;
    private int reviews;
}
