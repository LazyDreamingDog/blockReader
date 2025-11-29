package com.blockchain.explorer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.web3j.protocol.core.Response;

import java.math.BigInteger;

/**
 * 自定义RPC响应：获取安全级别
 */
@Data
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public class EthGetSecurityLevel extends Response<String> {

    /**
     * 获取安全级别值（十六进制字符串转BigInteger）
     */
    public BigInteger getSecurityLevel() {
        String result = getResult();
        if (result == null || result.isEmpty()) {
            return BigInteger.ZERO;
        }
        // 移除0x前缀并转换为BigInteger
        if (result.startsWith("0x") || result.startsWith("0X")) {
            return new BigInteger(result.substring(2), 16);
        }
        return new BigInteger(result);
    }
}
