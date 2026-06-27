package com.example.financas.exceptions.dto;

import org.springframework.http.HttpStatus;

public record RestErrorMessage(HttpStatus status, String message) {
}
