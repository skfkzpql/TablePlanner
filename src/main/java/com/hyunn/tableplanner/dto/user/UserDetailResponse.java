package com.hyunn.tableplanner.dto.user;

import com.hyunn.tableplanner.model.types.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDetailResponse {
    private String username;
    private String email;
    private UserRole role;
    private LocalDateTime createdAt;
}
