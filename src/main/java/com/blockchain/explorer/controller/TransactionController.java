package com.blockchain.explorer.controller;

import com.blockchain.explorer.entity.Transaction;
import com.blockchain.explorer.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

/**
 * 交易查询API
 */
@Slf4j
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionRepository transactionRepository;

    /**
     * 按交易哈希查询
     */
    @GetMapping("/{txHash}")
    public ResponseEntity<Transaction> getTransactionByHash(@PathVariable String txHash) {
        return transactionRepository.findByTxHash(txHash)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 查询区块的所有交易
     */
    @GetMapping("/block/{blockNumber}")
    public ResponseEntity<List<Transaction>> getTransactionsByBlock(@PathVariable String blockNumber) {
        try {
            BigInteger number = new BigInteger(blockNumber);
            List<Transaction> transactions = transactionRepository.findByBlockNumberOrderByTransactionIndexAsc(number);
            return ResponseEntity.ok(transactions);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 查询地址相关的所有交易（发送或接收）
     */
    @GetMapping("/address/{address}")
    public ResponseEntity<Page<Transaction>> getTransactionsByAddress(
            @PathVariable String address,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Transaction> transactions = transactionRepository.findByAddress(address, pageable);
        return ResponseEntity.ok(transactions);
    }

    /**
     * 分页查询交易列表
     */
    @GetMapping
    public ResponseEntity<Page<Transaction>> getTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Transaction> transactions = transactionRepository.findAll(pageable);
        return ResponseEntity.ok(transactions);
    }
}
