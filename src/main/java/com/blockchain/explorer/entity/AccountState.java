package com.blockchain.explorer.entity;

import lombok.Data;
import javax.persistence.*;
import java.math.BigInteger;
import java.time.LocalDateTime;

/**
 * 账户状态实体（包含自定义字段）
 */
@Data
@Entity
@Table(name = "account_states", indexes = {
        @Index(name = "idx_address", columnList = "address", unique = true),
        @Index(name = "idx_last_updated", columnList = "lastUpdated")
})
public class AccountState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 账户地址
     */
    @Column(nullable = false, unique = true, length = 42)
    private String address;

    /**
     * 余额
     */
    @Column(nullable = false)
    private BigInteger balance;

    /**
     * 安全级别（私链自定义字段）
     */
    private BigInteger securityLevel;

    /**
     * Nonce
     */
    private BigInteger nonce;

    /**
     * 最后更新时间
     */
    @Column(nullable = false)
    private LocalDateTime lastUpdated;

    /**
     * 创建时间
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }
}
