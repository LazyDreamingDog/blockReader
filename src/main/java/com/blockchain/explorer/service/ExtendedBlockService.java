package com.blockchain.explorer.service;

import com.blockchain.explorer.dto.ExtendedBlockData;
import com.fasterxml.jackson.databind.ObjectMapper;
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

/**
 * 扩展区块服务
 * 用于获取包含自定义字段的完整区块数据
 */
@Slf4j
@Service
public class ExtendedBlockService {

    @Autowired
    private Web3j web3j;

    private final ObjectMapper objectMapper = new ObjectMapper();

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

    /**
     * 获取扩展区块数据（包含所有自定义字段）
     * 
     * @param blockNumber      区块号
     * @param fullTransactions 是否包含完整交易数据
     * @return 扩展区块数据
     */
    public ExtendedBlockData getExtendedBlock(BigInteger blockNumber, boolean fullTransactions) throws IOException {
        // 构建RPC请求
        String blockParam = "0x" + blockNumber.toString(16);
        Request<?, RawBlockResponse> request = new Request<>(
                "eth_getBlockByNumber",
                Arrays.asList(blockParam, fullTransactions),
                getWeb3jService(),
                RawBlockResponse.class);

        RawBlockResponse response = request.send();

        if (response.hasError()) {
            throw new IOException("RPC error: " + response.getError().getMessage());
        }

        // 将原始响应转换为ExtendedBlockData
        Object result = response.getResult();
        if (result == null) {
            return null;
        }

        return objectMapper.convertValue(result, ExtendedBlockData.class);
    }

    /**
     * 获取扩展区块数据（使用"latest"参数）
     */
    public ExtendedBlockData getLatestExtendedBlock(boolean fullTransactions) throws IOException {
        Request<?, RawBlockResponse> request = new Request<>(
                "eth_getBlockByNumber",
                Arrays.asList("latest", fullTransactions),
                getWeb3jService(),
                RawBlockResponse.class);

        RawBlockResponse response = request.send();

        if (response.hasError()) {
            throw new IOException("RPC error: " + response.getError().getMessage());
        }

        Object result = response.getResult();
        if (result == null) {
            return null;
        }

        return objectMapper.convertValue(result, ExtendedBlockData.class);
    }

    /**
     * 原始区块响应类
     */
    public static class RawBlockResponse extends Response<Object> {
    }
}
