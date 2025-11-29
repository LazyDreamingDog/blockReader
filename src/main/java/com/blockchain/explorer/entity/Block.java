package com.blockchain.explorer.entity;

import lombok.Data;
import javax.persistence.*;
import java.math.BigInteger;
import java.time.LocalDateTime;

/**
 * 区块实体
 */
@Data
@Entity
@Table(name = "blocks", indexes = {
        @Index(name = "idx_block_number", columnList = "blockNumber", unique = true),
        @Index(name = "idx_block_hash", columnList = "blockHash", unique = true),
        @Index(name = "idx_timestamp", columnList = "timestamp")
})
public class Block {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 区块号
     */
    @Column(nullable = false, unique = true)
    private BigInteger blockNumber;

    /**
     * 区块哈希
     */
    @Column(nullable = false, unique = true, length = 66)
    private String blockHash;

    /**
     * 父区块哈希
     */
    @Column(length = 66)
    private String parentHash;

    /**
     * 时间戳
     */
    @Column(nullable = false)
    private LocalDateTime timestamp;

    /**
     * 矿工地址
     */
    @Column(length = 42)
    private String miner;

    /**
     * Gas使用量
     */
    private BigInteger gasUsed;

    /**
     * Gas限制
     */
    private BigInteger gasLimit;

    /**
     * 难度
     */
    private BigInteger difficulty;

    /**
     * Nonce
     */
    private BigInteger nonce;

    /**
     * 交易数量
     */
    private Integer transactionCount;

    // ===== 私链自定义字段 =====

    /**
     * 随机数（私链新增）
     */
    @Column(length = 66)
    private String randomNumber;

    /**
     * 随机根（私链新增）
     */
    @Column(length = 66)
    private String randomRoot;

    /**
     * PoW难度
     */
    private BigInteger powDifficulty;

    /**
     * PoW Gas
     */
    private BigInteger powGas;

    /**
     * PoW价格
     */
    private BigInteger powPrice;

    /**
     * PoS Leader地址
     */
    @Column(length = 42)
    private String posLeader;

    /**
     * Commit交易长度
     */
    private BigInteger commitTxLength;

    /**
     * 污点交易标记
     */
    @Column(columnDefinition = "TEXT")
    private String tainted;

    /**
     * 激励（区块生产者奖励）
     */
    private BigInteger incentive;

    /**
     * 基础费用（EIP-1559）
     */
    private BigInteger baseFee;

    /**
     * 创建时间
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
