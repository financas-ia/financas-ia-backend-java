package com.example.financas.user.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.br.CPF;

import java.time.LocalDate;

public record UpdateUserDTO(
        String email,

        String name,

        LocalDate dateOfBirth,

        String phoneNumber
) {
}
