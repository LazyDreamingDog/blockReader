package com.blockchain.explorer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigInteger;

/**
 * 自定义扩展区块数据
 * 适配魔改私链的额外字段
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExtendedBlockData {

    // 基础字段
    @JsonProperty("number")
    private String number;

    @JsonProperty("hash")
    private String hash;

    @JsonProperty("parentHash")
    private String parentHash;

    @JsonProperty("sha3Uncles")
    private String uncleHash;

    @JsonProperty("miner")
    private String coinbase;

    @JsonProperty("stateRoot")
    private String root;

    @JsonProperty("transactionsRoot")
    private String txHash;

    @JsonProperty("receiptsRoot")
    private String receiptHash;

    @JsonProperty("logsBloom")
    private String bloom;

    @JsonProperty("difficulty")
    private String difficulty;

    @JsonProperty("gasLimit")
    private String gasLimit;

    @JsonProperty("gasUsed")
    private String gasUsed;

    @JsonProperty("timestamp")
    private String time;

    @JsonProperty("extraData")
    private String extra;

    @JsonProperty("mixHash")
    private String mixDigest;

    @JsonProperty("nonce")
    private String nonce;

    // ===== 自定义字段 =====

    /**
     * 随机数（私链新增）
     */
    @JsonProperty("randomNumber")
    private String randomNumber;

    /**
     * 随机根（私链新增）
     */
    @JsonProperty("randomRoot")
    private String randomRoot;

    /**
     * PoW难度（可选字段）
     */
    @JsonProperty("powDifficulty")
    private String powDifficulty;

    /**
     * PoW Gas（可选字段）
     */
    @JsonProperty("powGas")
    private String powGas;

    /**
     * PoW价格（可选字段）
     */
    @JsonProperty("powPrice")
    private String powPrice;

    /**
     * 平均比率分子
     */
    @JsonProperty("avgRatioNumerator")
    private String avgRatioNumerator;

    /**
     * 平均比率分母
     */
    @JsonProperty("avgRatioDenominator")
    private String avgRatioDenominator;

    /**
     * 平均Gas分子
     */
    @JsonProperty("avgGasNumerator")
    private String avgGasNumerator;

    /**
     * 平均Gas分母
     */
    @JsonProperty("avgGasDenominator")
    private String avgGasDenominator;

    /**
     * PoS Leader地址
     */
    @JsonProperty("posLeader")
    private String posLeader;

    /**
     * PoS投票数据
     */
    @JsonProperty("posVoting")
    private String posVoting;

    /**
     * Commit交易长度
     */
    @JsonProperty("commitTxLength")
    private String commitTxLength;

    /**
     * 污点交易标记
     */
    @JsonProperty("tainted")
    private String tainted;

    /**
     * 激励（区块生产者奖励）
     */
    @JsonProperty("incentive")
    private String incentive;

    /**
     * 基础费用（EIP-1559）
     */
    @JsonProperty("baseFeePerGas")
    private String baseFee;

    /**
     * 提款根哈希（EIP-4895）
     */
    @JsonProperty("withdrawalsRoot")
    private String withdrawalsHash;

    /**
     * Blob Gas使用量（EIP-4844）
     */
    @JsonProperty("blobGasUsed")
    private String blobGasUsed;

    /**
     * 超额Blob Gas（EIP-4844）
     */
    @JsonProperty("excessBlobGas")
    private String excessBlobGas;

    /**
     * 父信标根（EIP-4788）
     */
    @JsonProperty("parentBeaconBlockRoot")
    private String parentBeaconRoot;

    /**
     * 交易列表
     */
    @JsonProperty("transactions")
    private Object transactions;

    /**
     * 转换为BigInteger（处理null和空值）
     */
    public static BigInteger toBigInteger(String value) {
        if (value == null || value.isEmpty() || value.equals("0x")) {
            return BigInteger.ZERO;
        }
        if (value.startsWith("0x") || value.startsWith("0X")) {
            return new BigInteger(value.substring(2), 16);
        }
        return new BigInteger(value);
    }

    /**
     * 转换为Long（处理null和空值）
     */
    public static Long toLong(String value) {
        if (value == null || value.isEmpty() || value.equals("0x")) {
            return 0L;
        }
        if (value.startsWith("0x") || value.startsWith("0X")) {
            return Long.parseLong(value.substring(2), 16);
        }
        return Long.parseLong(value);
    }
}
