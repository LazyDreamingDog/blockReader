package com.blockchain.explorer.service;

import com.blockchain.explorer.dto.EthGetSecurityLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.core.Request;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;

/**
 * 自定义RPC调用服务
 * 用于调用私链新增的自定义接口，无需修改web3j库
 */
@Slf4j
@Service
public class CustomRpcService {

    @Autowired
    private Web3j web3j;

    /**
     * 获取Web3jService实例
     */
    private Web3jService getWeb3jService() {
        // 通过反射获取Web3j内部的web3jService字段
        try {
            java.lang.reflect.Field field = web3j.getClass().getDeclaredField("web3jService");
            field.setAccessible(true);
            return (Web3jService) field.get(web3j);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get Web3jService", e);
        }
    }

    /**
     * 获取账户的安全级别（私链自定义方法）
     * 
     * @param address 账户地址
     * @return 安全级别
     */
    public BigInteger getSecurityLevel(String address) {
        try {
            Request<?, EthGetSecurityLevel> request = new Request<>(
                    "eth_getSecurityLevel", // 自定义RPC方法名
                    Arrays.asList(address, "latest"), // 参数列表：地址 + 区块参数
                    getWeb3jService(),
                    EthGetSecurityLevel.class);

            EthGetSecurityLevel response = request.send();

            if (response.hasError()) {
                log.error("Failed to get security level for address {}: {}",
                        address, response.getError().getMessage());
                return BigInteger.ZERO;
            }

            return response.getSecurityLevel();

        } catch (IOException e) {
            log.error("Error calling eth_getSecurityLevel for address {}", address, e);
            return BigInteger.ZERO;
        }
    }

    /**
     * 示例：添加更多自定义RPC方法
     * 
     * 如果您的私链还有其他自定义方法，可以在这里继续添加，例如：
     * 
     * public YourCustomType customMethod(String param) {
     * Request<?, YourCustomResponse> request = new Request<>(
     * "your_custom_method",
     * Arrays.asList(param),
     * getWeb3jService(),
     * YourCustomResponse.class
     * );
     * return request.send().getYourData();
     * }
     */
}
