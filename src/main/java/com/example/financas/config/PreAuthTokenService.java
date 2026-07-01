package com.example.financas.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class PreAuthTokenService {

    @Value("${api.security.token.preauth}")
    private String preAuthToken;

    public String generatePreAuthToken(String email) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(preAuthToken);
            return JWT.create()
                    .withIssuer("financas-api")
                    .withSubject(email)
                    .withExpiresAt(genExpirationDate())
                    .sign(algorithm);
        } catch (JWTCreationException exception) {
            throw new RuntimeException("Error generating pre-auth token", exception);
        }
    }

    public String validatePreAuthToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(preAuthToken);
            return JWT.require(algorithm)
                    .withIssuer("financas-api")
                    .build()
                    .verify(token)
                    .getSubject();
        } catch (JWTVerificationException exception) {
            throw new RuntimeException("Invalid pre-auth token", exception);
        }
    }

    public Instant genExpirationDate() {
        return LocalDateTime.now().plusMinutes(10).toInstant(ZoneOffset.of("-03:00"));
    }

}
