package com.example.financas.user.domain.dto;

import com.example.financas.user.domain.entity.User;

import java.time.LocalDate;
import java.util.UUID;

public record UserResponseDTO(
        UUID id,
        String email,
        String name,
        String role,
        String CPF,
        String phoneNumber,
        LocalDate dateofBirth
) {
    public UserResponseDTO(User user) {
        this(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole().name(),
                user.getCpf(),
                user.getPhoneNumber(),
                user.getDateOfBirth()
        );
    }
}
