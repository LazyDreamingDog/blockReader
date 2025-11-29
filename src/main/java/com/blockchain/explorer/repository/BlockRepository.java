package com.blockchain.explorer.repository;

import com.blockchain.explorer.entity.Block;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.Optional;

/**
 * 区块数据访问接口
 */
@Repository
public interface BlockRepository extends JpaRepository<Block, Long> {

    /**
     * 根据区块号查询
     */
    Optional<Block> findByBlockNumber(BigInteger blockNumber);

    /**
     * 根据区块哈希查询
     */
    Optional<Block> findByBlockHash(String blockHash);

    /**
     * 获取最大区块号
     */
    @Query("SELECT MAX(b.blockNumber) FROM Block b")
    BigInteger findMaxBlockNumber();

    /**
     * 检查区块号是否存在
     */
    boolean existsByBlockNumber(BigInteger blockNumber);
}
