package com.blockchain.explorer.controller;

import com.blockchain.explorer.service.CustomRpcService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthGetBalance;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 测试控制器 - 用于测试区块链数据获取，不涉及数据库
 */
@Slf4j
@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private Web3j web3j;

    @Autowired
    private CustomRpcService customRpcService;

    @Autowired
    private com.blockchain.explorer.service.ExtendedBlockService extendedBlockService;

    /**
     * 测试RPC连接
     */
    @GetMapping("/connection")
    public ResponseEntity<Map<String, Object>> testConnection() {
        try {
            Map<String, Object> result = new HashMap<>();

            // 获取客户端版本
            String clientVersion = web3j.web3ClientVersion().send().getWeb3ClientVersion();
            result.put("status", "connected");
            result.put("clientVersion", clientVersion);

            // 获取当前区块号
            BigInteger blockNumber = web3j.ethBlockNumber().send().getBlockNumber();
            result.put("currentBlockNumber", blockNumber.toString());

            // 获取Chain ID
            String chainId = web3j.ethChainId().send().getChainId().toString();
            result.put("chainId", chainId);

            log.info("RPC connection test successful: block={}, chainId={}", blockNumber, chainId);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("RPC connection test failed", e);
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * 测试获取指定区块
     */
    @GetMapping("/block/{blockNumber}")
    public ResponseEntity<Map<String, Object>> testGetBlock(@PathVariable String blockNumber) {
        try {
            BigInteger number = new BigInteger(blockNumber);
            EthBlock.Block block = web3j.ethGetBlockByNumber(
                    DefaultBlockParameter.valueOf(number),
                    false // 不包含完整交易
            ).send().getBlock();

            if (block == null) {
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> result = new HashMap<>();
            result.put("blockNumber", block.getNumber().toString());
            result.put("blockHash", block.getHash());
            result.put("parentHash", block.getParentHash());
            result.put("timestamp", block.getTimestamp().toString());
            result.put("miner", block.getMiner());
            result.put("gasUsed", block.getGasUsed().toString());
            result.put("gasLimit", block.getGasLimit().toString());
            result.put("transactionCount", block.getTransactions().size());

            log.info("Successfully fetched block {}", blockNumber);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Failed to fetch block {}", blockNumber, e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * 测试获取指定区块（包含完整交易数据）
     */
    @GetMapping("/block-with-transactions/{blockNumber}")
    public ResponseEntity<Map<String, Object>> testGetBlockWithTransactions(@PathVariable String blockNumber) {
        try {
            BigInteger number = new BigInteger(blockNumber);
            EthBlock.Block block = web3j.ethGetBlockByNumber(
                    DefaultBlockParameter.valueOf(number),
                    true // 包含完整交易数据
            ).send().getBlock();

            if (block == null) {
                return ResponseEntity.notFound().build();
            }

            // 区块头信息
            Map<String, Object> result = new HashMap<>();
            result.put("blockNumber", block.getNumber().toString());
            result.put("blockHash", block.getHash());
            result.put("parentHash", block.getParentHash());
            result.put("timestamp", block.getTimestamp().toString());
            result.put("miner", block.getMiner());
            result.put("gasUsed", block.getGasUsed().toString());
            result.put("gasLimit", block.getGasLimit().toString());
            result.put("difficulty", block.getDifficulty().toString());
            result.put("nonce", block.getNonce().toString());

            // 处理交易数据
            List<Map<String, Object>> transactions = new ArrayList<>();
            if (block.getTransactions() != null && !block.getTransactions().isEmpty()) {
                for (EthBlock.TransactionResult txResult : block.getTransactions()) {
                    if (txResult instanceof EthBlock.TransactionObject) {
                        EthBlock.TransactionObject tx = (EthBlock.TransactionObject) txResult.get();

                        Map<String, Object> txData = new HashMap<>();
                        txData.put("hash", tx.getHash());
                        txData.put("from", tx.getFrom());
                        txData.put("to", tx.getTo());
                        txData.put("value", tx.getValue().toString());
                        txData.put("gas", tx.getGas().toString());
                        txData.put("gasPrice", tx.getGasPrice().toString());
                        txData.put("nonce", tx.getNonce().toString());
                        txData.put("transactionIndex", tx.getTransactionIndex().toString());
                        txData.put("input", tx.getInput());

                        transactions.add(txData);
                    }
                }
            }

            result.put("transactionCount", transactions.size());
            result.put("transactions", transactions);

            log.info("Successfully fetched block {} with {} transactions", blockNumber, transactions.size());
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Failed to fetch block with transactions {}", blockNumber, e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * 测试获取最新区块
     */
    @GetMapping("/latest-block")
    public ResponseEntity<Map<String, Object>> testGetLatestBlock() {
        try {
            // 获取最新区块号
            BigInteger blockNumber = web3j.ethBlockNumber().send().getBlockNumber();

            // 获取最新区块详情
            EthBlock.Block block = web3j.ethGetBlockByNumber(
                    DefaultBlockParameter.valueOf(blockNumber),
                    false).send().getBlock();

            Map<String, Object> result = new HashMap<>();
            result.put("blockNumber", block.getNumber().toString());
            result.put("blockHash", block.getHash());
            result.put("timestamp", block.getTimestamp().toString());
            result.put("miner", block.getMiner());
            result.put("transactionCount", block.getTransactions().size());

            log.info("Successfully fetched latest block {}", blockNumber);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Failed to fetch latest block", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * 测试获取账户余额
     */
    @GetMapping("/balance/{address}")
    public ResponseEntity<Map<String, Object>> testGetBalance(@PathVariable String address) {
        try {
            EthGetBalance balanceResponse = web3j.ethGetBalance(
                    address,
                    DefaultBlockParameter.valueOf("latest")).send();

            BigInteger balance = balanceResponse.getBalance();

            Map<String, Object> result = new HashMap<>();
            result.put("address", address);
            result.put("balance", balance.toString());
            result.put("balanceInEther", balance.divide(BigInteger.TEN.pow(18)).toString());

            log.info("Successfully fetched balance for {}", address);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Failed to fetch balance for {}", address, e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * 测试自定义RPC方法 - 获取安全级别
     */
    @GetMapping("/security-level/{address}")
    public ResponseEntity<Map<String, Object>> testGetSecurityLevel(@PathVariable String address) {
        try {
            BigInteger securityLevel = customRpcService.getSecurityLevel(address);

            Map<String, Object> result = new HashMap<>();
            result.put("address", address);
            result.put("securityLevel", securityLevel.toString());
            result.put("method", "eth_getSecurityLevel");
            result.put("note", "This is a custom RPC method from your modified blockchain");

            log.info("Successfully fetched security level for {}: {}", address, securityLevel);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Failed to fetch security level for {}", address, e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("note", "Make sure your blockchain node supports eth_getSecurityLevel method");
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * 综合测试 - 获取账户完整信息
     */
    @GetMapping("/account-info/{address}")
    public ResponseEntity<Map<String, Object>> testGetAccountInfo(@PathVariable String address) {
        try {
            Map<String, Object> result = new HashMap<>();
            result.put("address", address);

            // 获取余额
            BigInteger balance = web3j.ethGetBalance(
                    address,
                    DefaultBlockParameter.valueOf("latest")).send().getBalance();
            result.put("balance", balance.toString());
            result.put("balanceInEther", balance.divide(BigInteger.TEN.pow(18)).toString());

            // 获取交易计数（nonce）
            BigInteger nonce = web3j.ethGetTransactionCount(
                    address,
                    DefaultBlockParameter.valueOf("latest")).send().getTransactionCount();
            result.put("nonce", nonce.toString());

            // 获取自定义字段：安全级别
            try {
                BigInteger securityLevel = customRpcService.getSecurityLevel(address);
                result.put("securityLevel", securityLevel.toString());
                result.put("customRpcStatus", "success");
            } catch (Exception e) {
                result.put("securityLevel", "N/A");
                result.put("customRpcStatus", "failed: " + e.getMessage());
            }

            log.info("Successfully fetched complete account info for {}", address);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Failed to fetch account info for {}", address, e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * 测试获取扩展区块数据（包含所有自定义字段）
     */
    @GetMapping("/extended-block/{blockNumber}")
    public ResponseEntity<Map<String, Object>> testGetExtendedBlock(@PathVariable String blockNumber) {
        try {
            BigInteger number = new BigInteger(blockNumber);

            com.blockchain.explorer.dto.ExtendedBlockData blockData = extendedBlockService.getExtendedBlock(number,
                    false);

            if (blockData == null) {
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> result = new HashMap<>();

            // 基础字段
            result.put("blockNumber", blockData.getNumber());
            result.put("hash", blockData.getHash());
            result.put("parentHash", blockData.getParentHash());
            result.put("miner", blockData.getCoinbase());
            result.put("timestamp", blockData.getTime());
            result.put("gasUsed", blockData.getGasUsed());
            result.put("gasLimit", blockData.getGasLimit());
            result.put("difficulty", blockData.getDifficulty());
            result.put("nonce", blockData.getNonce());

            // 自定义字段
            Map<String, Object> customFields = new HashMap<>();
            customFields.put("randomNumber", blockData.getRandomNumber());
            customFields.put("randomRoot", blockData.getRandomRoot());
            customFields.put("powDifficulty", blockData.getPowDifficulty());
            customFields.put("powGas", blockData.getPowGas());
            customFields.put("powPrice", blockData.getPowPrice());
            customFields.put("avgRatioNumerator", blockData.getAvgRatioNumerator());
            customFields.put("avgRatioDenominator", blockData.getAvgRatioDenominator());
            customFields.put("avgGasNumerator", blockData.getAvgGasNumerator());
            customFields.put("avgGasDenominator", blockData.getAvgGasDenominator());
            customFields.put("posLeader", blockData.getPosLeader());
            customFields.put("posVoting", blockData.getPosVoting());
            customFields.put("commitTxLength", blockData.getCommitTxLength());
            customFields.put("tainted", blockData.getTainted());
            customFields.put("incentive", blockData.getIncentive());
            customFields.put("baseFee", blockData.getBaseFee());

            result.put("customFields", customFields);

            log.info("Successfully fetched extended block {}", blockNumber);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Failed to fetch extended block {}", blockNumber, e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
}
