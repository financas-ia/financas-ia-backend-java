package com.example.financas.pluggy.domain.entity;

import com.example.financas.config.interfaces.UuidV7;
import com.example.financas.user.domain.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.UUID;

@Entity
@Table(name = "tb_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Item {

    @Id
    @UuidV7
    private UUID id;

    @Column(nullable = false, name = "pluggy_item_id")
    private String pluggyItemId;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private String institutionName;

    @Column(nullable = false)
    private String institutionImageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users", nullable = false)
    private User user;
}
