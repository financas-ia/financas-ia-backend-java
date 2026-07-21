package com.example.financas.pluggy.repository;

import com.example.financas.pluggy.domain.entity.AccountEntity;
import com.example.financas.pluggy.domain.entity.Transactions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface TransactionsRepository extends JpaRepository<Transactions, UUID> {
    @Query("SELECT t FROM Transactions t WHERE t.pluggyTransactionsId IN :ids")
    List<String> findByIds(@Param("ids") List<String> ids);

    @Query("SELECT t.pluggyTransactionsId FROM Transactions t WHERE t.account IN :accounts")
    Set<String> findByAccount(@Param("account") List<AccountEntity> accounts);
}
