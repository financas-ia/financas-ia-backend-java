package com.example.financas.auth.repository;

import com.example.financas.auth.entity.entity.PasswordRecovery;
import com.example.financas.user.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PasswordRecoveryRepository extends JpaRepository<PasswordRecovery, UUID> {
    Optional<PasswordRecovery> findByTokenAndValidTrue(String token);
    List<PasswordRecovery> findByUserAndValidTrue(User user);
}
