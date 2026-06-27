package com.example.financas.user.repository;

import com.example.financas.user.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findBycpf(String cpf);
    Optional<User> findByemail(String email);
    boolean existsBycpf(String cpf);
    boolean existsByemail(String email);
}
