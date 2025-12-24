package com.keyy.momentlinkback.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    @lombok.Builder.Default
    private String type = "Bearer";
    private UserResponse user;
}
