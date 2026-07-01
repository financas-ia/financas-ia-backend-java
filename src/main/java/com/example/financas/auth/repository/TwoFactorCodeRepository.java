package com.example.financas.auth.repository;

import com.example.financas.auth.entity.entity.TwoFactorCode;
import com.example.financas.user.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TwoFactorCodeRepository extends JpaRepository<TwoFactorCode, Long> {
    Optional<TwoFactorCode> findByCodeAndUserAndIsValidTrue(String code, User user);
    List<TwoFactorCode> findByUserAndIsValidTrue(User user);
}
