package com.example.financas.auth.service;

import com.example.financas.auth.domain.dto.LoginDTO;
import com.example.financas.auth.domain.dto.LoginResponseDTO;
import com.example.financas.auth.domain.dto.TwoFactorDTO;
import com.example.financas.auth.domain.entity.PasswordRecovery;
import com.example.financas.config.jwt.AcessTokenJwt;
import com.example.financas.config.jwt.PreAuthTokenJwt;
import com.example.financas.exceptions.NotFoundException;
import com.example.financas.user.domain.entity.User;
import com.example.financas.user.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.security.access.AccessDeniedException;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final AcessTokenJwt acessTokenJwt;
    private final PreAuthTokenJwt preAuthTokenJwt;
    private final TwoFactorCodeService twoFactorCodeService;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;
    private final PasswordRecoveryService passwordRecoveryService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public AuthService(
            AuthenticationManager authenticationManager,
            AcessTokenJwt acessTokenJwt,
            TwoFactorCodeService twoFactorCodeService,
            PreAuthTokenJwt preAuthTokenJwt,
            RefreshTokenService refreshTokenService,
            UserRepository userRepository,
            PasswordRecoveryService passwordRecoveryService,
            BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.authenticationManager = authenticationManager;
        this.acessTokenJwt = acessTokenJwt;
        this.preAuthTokenJwt = preAuthTokenJwt;
        this.twoFactorCodeService = twoFactorCodeService;
        this.refreshTokenService = refreshTokenService;
        this.userRepository = userRepository;
        this.passwordRecoveryService = passwordRecoveryService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public <T> T login(LoginDTO data) {
        var usernamePassword = new UsernamePasswordAuthenticationToken(data.email(), data.password());
        var auth = this.authenticationManager.authenticate(usernamePassword);

        User user = (User) auth.getPrincipal();

        if (!user.isTwoFactorEnabled()) {
            this.refreshTokenService.invalidRefreshToken(user);
            var refreshToken = this.refreshTokenService.createRefreshToken(user.getEmail());
            this.refreshTokenService.saveRefreshToken(refreshToken, user);
            var acessToken = acessTokenJwt.generateToken(user.getEmail());
            return (T) new LoginResponseDTO(acessToken, refreshToken);
        }

        String code = twoFactorCodeService.genareteTwoFactorCode();
        twoFactorCodeService.SaveTwoFactorCode(user, code);
        twoFactorCodeService.sendTwoFactorCode(user.getEmail(), code);
        var twoFactorToken = preAuthTokenJwt.generatePreAuthToken(user.getEmail());
        return (T) new TwoFactorDTO(twoFactorToken);
    }

    public LoginResponseDTO verifyTwoFactorCode(String code, User user) {
        boolean isValid = twoFactorCodeService.validTwoFactorCode(user, code);

        if (!isValid) {
            throw new RuntimeException("Invalid two-factor code");
        }

        this.refreshTokenService.invalidRefreshToken(user);
        var refreshToken = this.refreshTokenService.createRefreshToken(user.getEmail());
        this.refreshTokenService.saveRefreshToken(refreshToken, user);
        var acessToken = acessTokenJwt.generateToken(user.getEmail());
        return new LoginResponseDTO(acessToken, refreshToken);
    }

    public LoginResponseDTO refreshToken(String refreshToken) {
        String email = this.refreshTokenService.validateRefreshToken(refreshToken);
        if (email == null) {
            throw new RuntimeException("Invalid refresh token");
        }

        Optional<User> auxUser = this.userRepository.findByEmail(email);

        if (auxUser.isEmpty()) {
            throw new NotFoundException("User not found");
        }

        User user = auxUser.get();

        this.refreshTokenService.invalidRefreshToken(user);
        var newRefreshToken = this.refreshTokenService.createRefreshToken(email);
        this.refreshTokenService.saveRefreshToken(newRefreshToken, user);
        var acessToken = acessTokenJwt.generateToken(email);
        return new LoginResponseDTO(acessToken, newRefreshToken);
    }

    @Transactional
    public void forgorPassword(String email) {
        Optional<User> user = this.userRepository.findByEmail(email);
        if (user.isEmpty()) {
            return;
        }

        User forgotUser = user.get();

        this.passwordRecoveryService.invalidAllTokens(forgotUser);
        String token = this.passwordRecoveryService.generateRecoveryToken(forgotUser);
        this.passwordRecoveryService.sendRecoveryEmail(forgotUser, token);
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordRecovery passwordRecovery = this.passwordRecoveryService.validateAndGetToken(token);

        User user = passwordRecovery.getUser();
        String hashPassword = this.bCryptPasswordEncoder.encode(newPassword);
        user.setPassword(hashPassword);
        userRepository.save(user);
        this.passwordRecoveryService.completeRecovery(passwordRecovery);
    }

    public void validateUser(UUID featId) {
        User authenticatedUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        boolean isAdmin = authenticatedUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isEqual = authenticatedUser.getId().equals(featId);

        if (!isAdmin || !isEqual) {
            throw new AccessDeniedException("Acess denied");
        }
    }
}
