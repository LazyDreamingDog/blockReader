package com.blockchain.explorer.service;

import com.blockchain.explorer.entity.AccountState;
import com.blockchain.explorer.entity.Block;
import com.blockchain.explorer.entity.Transaction;
import com.blockchain.explorer.repository.AccountStateRepository;
import com.blockchain.explorer.repository.BlockRepository;
import com.blockchain.explorer.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthGetBalance;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Set;

/**
 * 区块同步服务
 * 定时从区块链读取新区块和交易数据，存储到数据库
 */
@Slf4j
@Service
public class BlockSyncService {

    @Autowired
    private Web3j web3j;

    @Autowired
    private BlockRepository blockRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountStateRepository accountStateRepository;

    @Autowired
    private CustomRpcService customRpcService;

    @Value("${blockchain.sync.sync-from-genesis}")
    private boolean syncFromGenesis;

    @Value("${blockchain.sync.start-block}")
    private long startBlock;

    @Value("${blockchain.sync.batch-size}")
    private int batchSize;

    private volatile boolean isRunning = false;

    @PostConstruct
    public void init() {
        log.info("BlockSyncService initialized");
        log.info("Sync from genesis: {}, Start block: {}, Batch size: {}",
                syncFromGenesis, startBlock, batchSize);
    }

    /**
     * 定时同步区块任务
     * 每5秒执行一次（配置在application.yml中）
     * 暂时注释掉用于测试 - 防止没有数据库时报错
     */
    // @Scheduled(fixedDelayString = "${blockchain.sync.interval}")
    public void syncBlocks() {
        if (isRunning) {
            log.debug("Previous sync task is still running, skipping...");
            return;
        }

        try {
            isRunning = true;

            // 获取链上最新区块号
            BigInteger latestBlockNumber = web3j.ethBlockNumber().send().getBlockNumber();

            // 获取数据库中最新区块号
            BigInteger dbLatestBlockNumber = blockRepository.findMaxBlockNumber();

            // 确定起始同步区块号
            BigInteger syncFromBlockNumber;
            if (dbLatestBlockNumber == null) {
                // 数据库为空，从配置的起始区块开始
                syncFromBlockNumber = syncFromGenesis ? BigInteger.ZERO : BigInteger.valueOf(startBlock);
            } else {
                // 从数据库最新区块的下一个区块开始
                syncFromBlockNumber = dbLatestBlockNumber.add(BigInteger.ONE);
            }

            // 计算本次同步的结束区块号
            BigInteger syncToBlockNumber = syncFromBlockNumber.add(BigInteger.valueOf(batchSize - 1));
            if (syncToBlockNumber.compareTo(latestBlockNumber) > 0) {
                syncToBlockNumber = latestBlockNumber;
            }

            // 如果没有新区块，跳过
            if (syncFromBlockNumber.compareTo(latestBlockNumber) > 0) {
                log.debug("No new blocks to sync. Latest block: {}", latestBlockNumber);
                return;
            }

            log.info("Syncing blocks from {} to {} (chain latest: {})",
                    syncFromBlockNumber, syncToBlockNumber, latestBlockNumber);

            // 逐个同步区块
            for (BigInteger blockNumber = syncFromBlockNumber; blockNumber
                    .compareTo(syncToBlockNumber) <= 0; blockNumber = blockNumber.add(BigInteger.ONE)) {

                syncBlock(blockNumber);
            }

            log.info("Successfully synced blocks from {} to {}", syncFromBlockNumber, syncToBlockNumber);

        } catch (Exception e) {
            log.error("Error syncing blocks", e);
        } finally {
            isRunning = false;
        }
    }

    /**
     * 同步单个区块及其交易
     */
    @Transactional
    public void syncBlock(BigInteger blockNumber) {
        try {
            // 检查区块是否已存在
            if (blockRepository.existsByBlockNumber(blockNumber)) {
                log.debug("Block {} already exists, skipping", blockNumber);
                return;
            }

            // 从链上获取区块数据
            EthBlock.Block ethBlock = web3j.ethGetBlockByNumber(
                    DefaultBlockParameter.valueOf(blockNumber),
                    true // 包含完整交易信息
            ).send().getBlock();

            if (ethBlock == null) {
                log.warn("Block {} not found on chain", blockNumber);
                return;
            }

            // 保存区块
            Block block = convertToBlockEntity(ethBlock);
            blockRepository.save(block);
            log.info("Saved block {}", blockNumber);

            // 保存交易
            Set<String> addressesInBlock = new HashSet<>();
            if (ethBlock.getTransactions() != null && !ethBlock.getTransactions().isEmpty()) {
                for (EthBlock.TransactionResult txResult : ethBlock.getTransactions()) {
                    EthBlock.TransactionObject txObject = (EthBlock.TransactionObject) txResult.get();
                    Transaction transaction = convertToTransactionEntity(txObject, ethBlock.getTimestamp());
                    transactionRepository.save(transaction);

                    // 收集涉及的地址
                    addressesInBlock.add(transaction.getFromAddress());
                    if (transaction.getToAddress() != null) {
                        addressesInBlock.add(transaction.getToAddress());
                    }
                }
                log.info("Saved {} transactions from block {}", ethBlock.getTransactions().size(), blockNumber);
            }

            // 更新涉及地址的账户状态
            updateAccountStates(addressesInBlock);

        } catch (IOException e) {
            log.error("Error syncing block {}", blockNumber, e);
            throw new RuntimeException("Failed to sync block " + blockNumber, e);
        }
    }

    /**
     * 更新账户状态（包括自定义字段）
     */
    private void updateAccountStates(Set<String> addresses) {
        for (String address : addresses) {
            try {
                // 获取余额
                EthGetBalance balanceResponse = web3j.ethGetBalance(
                        address,
                        DefaultBlockParameter.valueOf("latest")).send();
                BigInteger balance = balanceResponse.getBalance();

                // 获取自定义字段：安全级别
                BigInteger securityLevel = customRpcService.getSecurityLevel(address);

                // 更新或创建账户状态
                AccountState accountState = accountStateRepository.findByAddress(address)
                        .orElse(new AccountState());

                accountState.setAddress(address);
                accountState.setBalance(balance);
                accountState.setSecurityLevel(securityLevel);
                accountState.setLastUpdated(LocalDateTime.now());

                accountStateRepository.save(accountState);

                log.debug("Updated account state for {}: balance={}, securityLevel={}",
                        address, balance, securityLevel);

            } catch (Exception e) {
                log.error("Error updating account state for {}", address, e);
            }
        }
    }

    /**
     * 转换区块数据
     */
    private Block convertToBlockEntity(EthBlock.Block ethBlock) {
        Block block = new Block();
        block.setBlockNumber(ethBlock.getNumber());
        block.setBlockHash(ethBlock.getHash());
        block.setParentHash(ethBlock.getParentHash());
        block.setTimestamp(convertTimestamp(ethBlock.getTimestamp()));
        block.setMiner(ethBlock.getMiner());
        block.setGasUsed(ethBlock.getGasUsed());
        block.setGasLimit(ethBlock.getGasLimit());
        block.setDifficulty(ethBlock.getDifficulty());
        block.setNonce(ethBlock.getNonce());
        block.setTransactionCount(ethBlock.getTransactions() != null ? ethBlock.getTransactions().size() : 0);
        return block;
    }

    /**
     * 转换交易数据
     */
    private Transaction convertToTransactionEntity(EthBlock.TransactionObject txObject, BigInteger blockTimestamp) {
        Transaction transaction = new Transaction();
        transaction.setTxHash(txObject.getHash());
        transaction.setBlockNumber(txObject.getBlockNumber());
        transaction.setBlockHash(txObject.getBlockHash());
        transaction.setTransactionIndex(txObject.getTransactionIndex().intValue());
        transaction.setFromAddress(txObject.getFrom());
        transaction.setToAddress(txObject.getTo());
        transaction.setValue(txObject.getValue());
        transaction.setGasPrice(txObject.getGasPrice());
        transaction.setGas(txObject.getGas());
        transaction.setInput(txObject.getInput());
        transaction.setNonce(txObject.getNonce());
        transaction.setTimestamp(convertTimestamp(blockTimestamp));
        return transaction;
    }

    /**
     * 转换时间戳
     */
    private LocalDateTime convertTimestamp(BigInteger timestamp) {
        return LocalDateTime.ofInstant(
                Instant.ofEpochSecond(timestamp.longValue()),
                ZoneId.systemDefault());
    }
}
