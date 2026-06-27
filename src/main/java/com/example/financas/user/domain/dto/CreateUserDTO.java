package com.example.financas.user.domain.dto;

import java.time.LocalDate;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.br.CPF;

public record CreateUserDTO (
        @NotBlank(message = "Email não pode ser nulo ou vazio")
        @Email(message = "Email inválido")
        String email,

        @NotBlank(message = "CPF não pode ser nulo ou vazio")
        @CPF(message = "CPF inválido")
        String cpf,

        @NotBlank(message = "Nome não pode ser nulo ou vazio")
        String name,

        @NotBlank
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$",
                message = "A senha deve ter no mínimo 8 caracteres, contendo pelo menos uma letra e um número"
        )
        String password,

        @NotNull(message = "Data de nascimento não pode ser nula")
        LocalDate dateOfBirth,

        @NotBlank(message = "Telefone não pode ser nulo ou vazio")
        String phoneNumber
) {
}
