package com.bugtrackerpro.feign;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This is a lightweight DTO used by the Feign client.
 * It mirrors the UserDTO from user-service, but lives inside bug-service.
 * Microservices do NOT share DTO classes via a common library (in this style).
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private String role;
}
