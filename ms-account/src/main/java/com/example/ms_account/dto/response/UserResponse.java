package com.example.ms_account.dto.response;

public record UserResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String password,
        String role
) {
}
