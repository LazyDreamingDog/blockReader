package com.blockchain.explorer.entity;

import lombok.Data;
import javax.persistence.*;
import java.math.BigInteger;
import java.time.LocalDateTime;

/**
 * 账户状态实体（包含所有自定义字段）
 */
@Data
@Entity
@Table(name = "account_states", indexes = {
        @Index(name = "idx_address", columnList = "address", unique = true),
        @Index(name = "idx_last_updated", columnList = "lastUpdated"),
        @Index(name = "idx_is_contract", columnList = "isContract")
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
     * Nonce
     */
    private BigInteger nonce;

    /**
     * 安全级别 (1-5, 0表示账户被锁定)
     */
    private BigInteger securityLevel;

    // ===== 虚拟利息相关字段 =====

    /**
     * 利息
     */
    private BigInteger interest;

    /**
     * 上次计算利息的区块号
     */
    private BigInteger lastBlockNumber;

    // ===== 后量子相关字段 =====

    /**
     * 最后一次后量子公钥
     */
    @Column(columnDefinition = "TEXT")
    private String lastPostQuanPub;

    /**
     * 后量子计数器
     */
    private BigInteger postQuanCounter;

    // ===== 统计字段 =====

    /**
     * Gas总量
     */
    private BigInteger totalNumberOfGas;

    /**
     * 合约调用次数
     */
    private BigInteger contractCallCount;

    /**
     * 交易总价值
     */
    private BigInteger totalValueTx;

    // ===== 质押信息 =====

    /**
     * 质押金额
     */
    private BigInteger pledgeAmount;

    /**
     * 质押年限
     */
    private BigInteger pledgeYear;

    /**
     * 质押开始时间（区块高度）
     */
    private BigInteger startTime;

    /**
     * 利率
     */
    private BigInteger interestRate;

    /**
     * 当前利息
     */
    private BigInteger currentInterest;

    /**
     * 收益利息（利息差）
     */
    private BigInteger earnInterest;

    // ===== 合约年费相关 =====

    /**
     * 合约部署年费
     */
    private BigInteger annualFee;

    /**
     * 上一次收取年费的时间（区块高度）
     */
    private BigInteger lastAnnualFeeTime;

    // ===== 地址相关字段 =====

    /**
     * 合约地址
     */
    @Column(length = 42)
    private String contractAddress;

    /**
     * 部署人地址
     */
    @Column(length = 42)
    private String deployedAddress;

    /**
     * 投资人地址
     */
    @Column(length = 42)
    private String investorAddress;

    /**
     * 受益人地址
     */
    @Column(length = 42)
    private String beneficiaryAddress;

    // ===== 标志字段 =====

    /**
     * 质押标志
     */
    private Boolean stakeFlag;

    /**
     * 是否为合约账户
     */
    private Boolean isContract;

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
        lastUpdated = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }
}
