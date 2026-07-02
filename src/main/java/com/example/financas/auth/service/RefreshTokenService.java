package com.example.financas.auth.service;

import com.example.financas.auth.entity.entity.RefreshToken;
import com.example.financas.auth.repository.RefreshTokenRepository;
import com.example.financas.config.jwt.RefreshTokenJwt;
import com.example.financas.user.domain.entity.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RefreshTokenService {

    private final RefreshTokenJwt refreshTokenJwt;
    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService (RefreshTokenJwt refreshTokenJwt, RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenJwt = refreshTokenJwt;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public String createRefreshToken(String email) {
        return this.refreshTokenJwt.generateToken(email);
    }

    public void saveRefreshToken(String token, User user) {
        RefreshToken newRefreshToken = new RefreshToken();
        newRefreshToken.setToken(token);
        newRefreshToken.setUser(user);
        newRefreshToken.setValid(true);
        this.refreshTokenRepository.save(newRefreshToken);
    }

    public void invalidRefreshToken(User user) {
        List<RefreshToken>invalidsTokens = this.refreshTokenRepository.findByUserAndValidTrue(user);
        invalidsTokens.forEach(c -> c.setValid(false));
        this.refreshTokenRepository.saveAll(invalidsTokens);
    }

    public String validateRefreshToken(String token) {
        String email = this.refreshTokenJwt.validadeToken(token);

        boolean existsAndValid = this.refreshTokenRepository.existsByTokenAndValidTrue(token);
        if (!existsAndValid) {
            throw new RuntimeException("Refresh token has been invalidated");
        }

        return email;
    }




}
