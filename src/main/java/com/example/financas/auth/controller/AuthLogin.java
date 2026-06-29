package com.example.financas.auth.controller;

import com.example.financas.auth.dto.LoginDTO;
import com.example.financas.auth.dto.LoginResponseDTO;
import com.example.financas.config.TokenService;
import com.example.financas.user.domain.entity.User;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthLogin {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    public AuthLogin(AuthenticationManager authenticationManager, TokenService tokenService) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
    }

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody @Valid LoginDTO data) {
        var usernamePassword = new UsernamePasswordAuthenticationToken(data.email(), data.password());
        var auth = this.authenticationManager.authenticate(usernamePassword);

        var token = tokenService.generateToken(((User) auth.getPrincipal()).getEmail());
        return ResponseEntity.ok(new LoginResponseDTO(token));
    }
}
