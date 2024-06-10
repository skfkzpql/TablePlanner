package com.hyunn.tableplanner.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreDTO {
    private Long id;
    private Long partnerId;
    private String name;
    private String location;
    private String description;
    private LocalDateTime createdAt;
}