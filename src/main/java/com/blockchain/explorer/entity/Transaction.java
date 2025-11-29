package com.blockchain.explorer.entity;

import lombok.Data;
import javax.persistence.*;
import java.math.BigInteger;
import java.time.LocalDateTime;

/**
 * 交易实体
 */
@Data
@Entity
@Table(name = "transactions", indexes = {
        @Index(name = "idx_tx_hash", columnList = "txHash", unique = true),
        @Index(name = "idx_block_number", columnList = "blockNumber"),
        @Index(name = "idx_from_address", columnList = "fromAddress"),
        @Index(name = "idx_to_address", columnList = "toAddress"),
        @Index(name = "idx_timestamp", columnList = "timestamp")
})
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 交易哈希
     */
    @Column(nullable = false, unique = true, length = 66)
    private String txHash;

    /**
     * 所属区块号
     */
    @Column(nullable = false)
    private BigInteger blockNumber;

    /**
     * 区块哈希
     */
    @Column(length = 66)
    private String blockHash;

    /**
     * 交易在区块中的索引
     */
    private Integer transactionIndex;

    /**
     * 发送方地址
     */
    @Column(nullable = false, length = 42)
    private String fromAddress;

    /**
     * 接收方地址
     */
    @Column(length = 42)
    private String toAddress;

    /**
     * 转账金额
     */
    @Column(nullable = false)
    private BigInteger value;

    /**
     * Gas价格
     */
    private BigInteger gasPrice;

    /**
     * Gas数量
     */
    private BigInteger gas;

    /**
     * 实际使用的Gas
     */
    private BigInteger gasUsed;

    /**
     * 输入数据
     */
    @Column(columnDefinition = "TEXT")
    private String input;

    /**
     * Nonce
     */
    private BigInteger nonce;

    /**
     * 交易状态（1: 成功, 0: 失败）
     */
    private Integer status;

    /**
     * 交易类型（私链扩展）
     * 0x00=Legacy, 0x01=AccessList, 0x02=DynamicFee, 0x03=Blob,
     * 0x04=Pow, 0x05=DynamicCrypto, 0x06=Deposit, 0x07=Nested
     */
    private Integer txType;

    /**
     * 时间戳
     */
    @Column(nullable = false)
    private LocalDateTime timestamp;

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
