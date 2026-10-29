package com.example.financas.auth.repository;

import com.example.financas.auth.domain.entity.TwoFactorCode;
import com.example.financas.user.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TwoFactorCodeRepository extends JpaRepository<TwoFactorCode, Long> {
    Optional<TwoFactorCode> findByCodeAndUserAndValidTrue(String code, User user);
    List<TwoFactorCode> findByUserAndValidTrue(User user);
}
