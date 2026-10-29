package com.example.financas.auth.controller;

import com.example.financas.auth.domain.dto.*;
import com.example.financas.auth.service.AuthService;
import com.example.financas.user.domain.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.intellij.lang.annotations.JdkConstants;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "auth", description = "Endpoints para authenticação de usuario")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Rota para login de usuarios",
            description = "Recebe o dto de login e realiza o login no sistema. " +
                    "Retorna o acessToken curto e o RefreshToken.")
    @ApiResponse(responseCode = "200", description = "Login Realizado com sucesso")
    @ApiResponse(responseCode = "401", description = "Usuario não encontrado")
    @ApiResponse(responseCode = "403", description = "Acesso negado")
    public ResponseEntity<?> login(@RequestBody @Valid LoginDTO data) {
        Object newLogin = this.authService.login(data);
        return ResponseEntity.ok(newLogin);
    }

    @PostMapping("/verify-2fa")
    @Operation(summary = "Rota para verificação do codigo de 2 fatores",
            description = "Recebe o codigo de 2 fatores enviado no email do usuario" +
                    " e verifica se ele esta correto.")
    @ApiResponse(responseCode = "200", description = "Codigo validado com sucesso")
    @ApiResponse(responseCode = "401", description = "Codigo invalido")
    public ResponseEntity<LoginResponseDTO> verifyTwoFactorCode(@RequestBody @Valid TwoFactorDTO data, @AuthenticationPrincipal User user) {
        LoginResponseDTO newLogin = this.authService.verifyTwoFactorCode(data.code(), user);
        return ResponseEntity.ok(newLogin);
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Rota para renovar o acessToken do usuario",
            description = "Recebe o refreshToken do usuario, verifica sua validade " +
                    "e retorna um novo acessToken e refreshToken para o usuario")
    @ApiResponse(responseCode = "200", description = "Token validado e novos tokens retornados")
    @ApiResponse(responseCode = "400", description = "Token invalido")
    public ResponseEntity<LoginResponseDTO> refreshToken(@RequestBody @Valid RefreshTokenRequestDTO data) {
        LoginResponseDTO newLogin = this.authService.refreshToken(data.refreshToken());
        return ResponseEntity.ok(newLogin);
    }

    @PostMapping("/request-password-recovery")
    @Operation(summary = "Rota para solicitar recuperação de senha",
            description = "Usuario solicita recuperação de senha e um email é disparado")
    @ApiResponse(responseCode = "200", description = "Rota realizada com sucesso")
    public ResponseEntity<String> requestPasswordRecovery(@RequestBody @Valid RecoveryPasswordDTO data) {
        this.authService.forgorPassword(data.email());
        return ResponseEntity.ok("Password recovery email sent");
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Usuario realiza a troca da senha",
            description = "Usuario envia a nova senha")
    @ApiResponse(responseCode = "200", description = "Senha atualizada com sucesso")
    public ResponseEntity<?> resetPassowrd(@RequestBody @Valid ResetPasswordDTO data, @RequestParam String token) {
        this.authService.resetPassword(token, data.newPassword());
        return ResponseEntity.ok().build();
    }
}
