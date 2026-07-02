package com.example.financas.auth.controller;

import com.example.financas.auth.entity.dto.LoginDTO;
import com.example.financas.auth.entity.dto.LoginResponseDTO;
import com.example.financas.auth.entity.dto.RefreshTokenRequestDTO;
import com.example.financas.auth.entity.dto.TwoFactorDTO;
import com.example.financas.auth.service.AuthService;
import com.example.financas.auth.service.TwoFactorCodeService;
import com.example.financas.config.jwt.PreAuthTokenJwt;
import com.example.financas.config.jwt.AcessTokenJwt;
import com.example.financas.user.domain.entity.User;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        return ResponseEntity.ok(newLogin);}
}
