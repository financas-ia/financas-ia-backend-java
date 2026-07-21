package com.example.financas.auth.controller;

import com.example.financas.auth.domain.dto.*;
import com.example.financas.auth.service.AuthService;
import com.example.financas.user.domain.entity.User;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginDTO data) {
        Object newLogin = this.authService.login(data);
        return ResponseEntity.ok(newLogin);
    }

    @PostMapping("/verify-2fa")
    public ResponseEntity<LoginResponseDTO> verifyTwoFactorCode(@RequestBody @Valid TwoFactorDTO data, @AuthenticationPrincipal User user) {
        LoginResponseDTO newLogin = this.authService.verifyTwoFactorCode(data.code(), user);
        return ResponseEntity.ok(newLogin);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<LoginResponseDTO> refreshToken(@RequestBody @Valid RefreshTokenRequestDTO data) {
        LoginResponseDTO newLogin = this.authService.refreshToken(data.refreshToken());
        return ResponseEntity.ok(newLogin);
    }

    @PostMapping("/request-password-recovery")
    public ResponseEntity<String> requestPasswordRecovery(@RequestBody @Valid RecoveryPasswordDTO data) {
        this.authService.forgorPassword(data.email());
        return ResponseEntity.ok("Password recovery email sent");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassowrd(@RequestBody @Valid ResetPasswordDTO data, @RequestParam String token) {
        this.authService.resetPassword(token, data.newPassword());
        return ResponseEntity.ok().build();
    }
}
