package com.hyunn.tableplanner.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSetPartnerRequest {
    @NotBlank(message = "Username is required")
    private String username;
}
