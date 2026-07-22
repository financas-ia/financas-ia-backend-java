package com.example.financas.pluggy.domain.entity;

import com.example.financas.config.interfaces.UuidV7;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "account")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class AccountEntity {

    @Id
    @UuidV7
    private UUID id;

    @Column(nullable = false, name = "pluggy_account_id")
    private String pluggyAcountId;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String subtype;

    @Column(nullable = false)
    private String number;

    @Column(nullable = false)
    private BigDecimal balance;

    @Column(nullable = false, name = "currency_code")
    private String currencyCode;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @CreatedDate
    @Column(nullable = false)
    private Instant created_at;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updated_at;
}
