package com.blockchain.explorer.controller;

import com.blockchain.explorer.entity.Block;
import com.blockchain.explorer.repository.BlockRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;

/**
 * 区块查询API
 */
@Slf4j
@RestController
@RequestMapping("/api/blocks")
public class BlockController {

    @Autowired
    private BlockRepository blockRepository;

    /**
     * 获取最新区块
     */
    @GetMapping("/latest")
    public ResponseEntity<Block> getLatestBlock() {
        BigInteger maxBlockNumber = blockRepository.findMaxBlockNumber();
        if (maxBlockNumber == null) {
            return ResponseEntity.notFound().build();
        }

        return blockRepository.findByBlockNumber(maxBlockNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 按区块号查询
     */
    @GetMapping("/{blockNumber}")
    public ResponseEntity<Block> getBlockByNumber(@PathVariable String blockNumber) {
        try {
            BigInteger number = new BigInteger(blockNumber);
            return blockRepository.findByBlockNumber(number)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 按区块哈希查询
     */
    @GetMapping("/hash/{blockHash}")
    public ResponseEntity<Block> getBlockByHash(@PathVariable String blockHash) {
        return blockRepository.findByBlockHash(blockHash)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 分页查询区块列表
     * 
     * @param page 页码（从0开始）
     * @param size 每页大小
     */
    @GetMapping
    public ResponseEntity<Page<Block>> getBlocks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("blockNumber").descending());
        Page<Block> blocks = blockRepository.findAll(pageable);
        return ResponseEntity.ok(blocks);
    }
}
