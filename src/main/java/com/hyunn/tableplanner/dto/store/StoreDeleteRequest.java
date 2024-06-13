package com.hyunn.tableplanner.dto.store;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreDeleteRequest {
    @NotNull(message = "Id is required")
    private Long id;
}
