package com.example.financas.auth.controller;

import com.example.financas.auth.entity.dto.LoginDTO;
import com.example.financas.auth.entity.dto.LoginResponseDTO;
import com.example.financas.auth.entity.dto.TwoFactorDTO;
import com.example.financas.auth.service.TwoFactorCodeService;
import com.example.financas.config.PreAuthTokenService;
import com.example.financas.config.TokenService;
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
public class AuthLogin {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final PreAuthTokenService preAuthTokenService;
    private final TwoFactorCodeService twoFactorCodeService;

    public AuthLogin(AuthenticationManager authenticationManager, TokenService tokenService, TwoFactorCodeService twoFactorCodeService, PreAuthTokenService preAuthTokenService) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.preAuthTokenService = preAuthTokenService;
        this.twoFactorCodeService = twoFactorCodeService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid LoginDTO data) {
        var usernamePassword = new UsernamePasswordAuthenticationToken(data.email(), data.password());
        var auth = this.authenticationManager.authenticate(usernamePassword);

        User user = (User) auth.getPrincipal();

        if (!user.isTwoFactorEnabled()) {
            var token = tokenService.generateToken(user.getEmail());
            return ResponseEntity.ok(new LoginResponseDTO(token));
        }

        String code = twoFactorCodeService.genareteTwoFactorCode();
        twoFactorCodeService.SaveTwoFactorCode(user, code);
        twoFactorCodeService.sendTwoFactorCode(user.getEmail(), code);
        var token = preAuthTokenService.generatePreAuthToken(user.getEmail());
        return ResponseEntity.ok(new LoginResponseDTO(token));
    }

    @PostMapping("/verify-2fa")
    public ResponseEntity<LoginResponseDTO> verifyTwoFactorCode(@RequestBody @Valid TwoFactorDTO data, @AuthenticationPrincipal User user) {
        boolean isValid = twoFactorCodeService.validTwoFactorCode(user, data.code());

        var token = tokenService.generateToken(user.getEmail());
        return ResponseEntity.ok(new LoginResponseDTO(token));
    }
}
