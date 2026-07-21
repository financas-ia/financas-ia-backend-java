package com.example.financas.pluggy.repository;

import com.example.financas.pluggy.domain.entity.AccountEntity;
import com.example.financas.pluggy.domain.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<AccountEntity, UUID> {
    Optional<AccountEntity> findByPluggyAcountId(String pluggyAccountId);
    List<AccountEntity> findByItem(Item item);
    @Query("SELECT t FROM AccountEntity t WHERE t.pluggyAcountId IN :ids")
    List<AccountEntity> findByIds(@Param("ids") List<String> ids);
}
