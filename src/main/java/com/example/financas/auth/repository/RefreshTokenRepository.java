package com.example.financas.auth.repository;

import com.example.financas.auth.entity.entity.RefreshToken;
import com.example.financas.user.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    List<RefreshToken> findByUserAndIsValidTrue(User user);
    boolean existsByTokenAndValidTrue(String token);
}
