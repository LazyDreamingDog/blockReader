package com.blockchain.explorer.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.Response;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;

/**
 * 扩展账户服务
 * 用于获取私链新增的账户相关字段
 */
@Slf4j
@Service
public class ExtendedAccountService {

    @Autowired
    private Web3j web3j;

    /**
     * 获取Web3jService实例
     */
    private Web3jService getWeb3jService() {
        try {
            java.lang.reflect.Field field = web3j.getClass().getDeclaredField("web3jService");
            field.setAccessible(true);
            return (Web3jService) field.get(web3j);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get Web3jService", e);
        }
    }

    // ===== 虚拟利息相关 =====

    /**
     * 获取账户利息
     */
    public BigInteger getInterest(String address) {
        return callBigIntegerMethod("eth_getInterest", address);
    }

    // ===== 后量子相关 =====

    /**
     * 获取后量子计数器
     */
    public BigInteger getPostQuanCounter(String address) {
        return callBigIntegerMethod("eth_getPostQuanCounter", address);
    }

    // ===== 统计字段 =====

    /**
     * 获取Gas总量
     */
    public BigInteger getTotalNumberOfGas(String address) {
        return callBigIntegerMethod("eth_getTotalNumberOfGas", address);
    }

    /**
     * 获取合约调用次数
     */
    public BigInteger getContractCallCount(String address) {
        return callBigIntegerMethod("eth_getContractCallCount", address);
    }

    /**
     * 获取交易总价值
     */
    public BigInteger getTotalValueTx(String address) {
        return callBigIntegerMethod("eth_getTotalValueTx", address);
    }

    // ===== 质押信息 =====

    /**
     * 获取质押金额
     */
    public BigInteger getPledgeAmount(String address) {
        return callBigIntegerMethod("eth_getPledgeAmount", address);
    }

    /**
     * 获取质押年限
     */
    public BigInteger getPledgeYear(String address) {
        return callBigIntegerMethod("eth_getPledgeYear", address);
    }

    /**
     * 获取质押开始时间
     */
    public BigInteger getStartTime(String address) {
        return callBigIntegerMethod("eth_getStartTime", address);
    }

    /**
     * 获取利率
     */
    public BigInteger getInterestRate(String address) {
        return callBigIntegerMethod("eth_getInterestRate", address);
    }

    /**
     * 获取当前利息
     */
    public BigInteger getCurrentInterest(String address) {
        return callBigIntegerMethod("eth_getCurrentInterest", address);
    }

    /**
     * 获取收益利息
     */
    public BigInteger getEarnInterest(String address) {
        return callBigIntegerMethod("eth_getEarnInterest", address);
    }

    /**
     * 获取质押完整信息（整合接口）
     */
    public String getPledgeInfo(String address) {
        return callStringMethod("eth_getPledgeInfo", address);
    }

    // ===== 合约年费相关 =====

    /**
     * 获取合约年费
     */
    public BigInteger getAnnualFee(String address) {
        return callBigIntegerMethod("eth_getAnnualFee", address);
    }

    /**
     * 获取上次年费收取时间
     */
    public BigInteger getLastAnnualFeeTime(String address) {
        return callBigIntegerMethod("eth_getLastAnnualFeeTime", address);
    }

    // ===== 地址相关字段 =====

    /**
     * 获取部署人地址
     */
    public String getDeployedAddress(String address) {
        return callStringMethod("eth_getDeployedAddress", address);
    }

    /**
     * 获取投资人地址
     */
    public String getInvestorAddress(String address) {
        return callStringMethod("eth_getInvestorAddress", address);
    }

    /**
     * 获取受益人地址
     */
    public String getBeneficiaryAddress(String address) {
        return callStringMethod("eth_getBeneficiaryAddress", address);
    }

    // ===== 标志字段 =====

    /**
     * 获取质押标志
     */
    public Boolean getStakeFlag(String address) {
        return callBooleanMethod("eth_getStakeFlag", address);
    }

    // ===== 通用调用方法 =====

    /**
     * 调用返回BigInteger的RPC方法
     */
    private BigInteger callBigIntegerMethod(String method, String address) {
        try {
            Request<?, org.web3j.protocol.core.methods.response.EthCall> request = new Request<>(
                    method,
                    Arrays.asList(address, "latest"),
                    getWeb3jService(),
                    org.web3j.protocol.core.methods.response.EthCall.class);

            org.web3j.protocol.core.methods.response.EthCall response = request.send();

            if (response.hasError()) {
                log.warn("{} failed for address {}: {}", method, address, response.getError().getMessage());
                return BigInteger.ZERO;
            }

            String value = response.getValue();
            if (value == null || value.equals("0x") || value.equals("0x0")) {
                return BigInteger.ZERO;
            }

            // 移除0x前缀并转换为BigInteger
            String hexValue = value.startsWith("0x") ? value.substring(2) : value;
            return new BigInteger(hexValue, 16);

        } catch (IOException e) {
            log.error("Error calling {} for address {}", method, address, e);
            return BigInteger.ZERO;
        }
    }

    /**
     * 调用返回String的RPC方法
     */
    private String callStringMethod(String method, String address) {
        try {
            Request<?, org.web3j.protocol.core.methods.response.EthCall> request = new Request<>(
                    method,
                    Arrays.asList(address, "latest"),
                    getWeb3jService(),
                    org.web3j.protocol.core.methods.response.EthCall.class);

            org.web3j.protocol.core.methods.response.EthCall response = request.send();

            if (response.hasError()) {
                log.warn("{} failed for address {}: {}", method, address, response.getError().getMessage());
                return null;
            }

            return response.getValue();

        } catch (IOException e) {
            log.error("Error calling {} for address {}", method, address, e);
            return null;
        }
    }

    /**
     * 调用返回Boolean的RPC方法
     */
    private Boolean callBooleanMethod(String method, String address) {
        try {
            Request<?, org.web3j.protocol.core.methods.response.EthCall> request = new Request<>(
                    method,
                    Arrays.asList(address, "latest"),
                    getWeb3jService(),
                    org.web3j.protocol.core.methods.response.EthCall.class);

            org.web3j.protocol.core.methods.response.EthCall response = request.send();

            if (response.hasError()) {
                log.warn("{} failed for address {}: {}", method, address, response.getError().getMessage());
                return false;
            }

            String value = response.getValue();
            if (value == null) {
                return false;
            }

            // 0x1 = true, 0x0 = false
            return !value.equals("0x0") && !value.equals("0x");

        } catch (IOException e) {
            log.error("Error calling {} for address {}", method, address, e);
            return false;
        }
    }
}
