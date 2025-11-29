package com.blockchain.explorer.repository;

import com.blockchain.explorer.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

/**
 * 交易数据访问接口
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * 根据交易哈希查询
     */
    Optional<Transaction> findByTxHash(String txHash);

    /**
     * 根据区块号查询所有交易
     */
    List<Transaction> findByBlockNumberOrderByTransactionIndexAsc(BigInteger blockNumber);

    /**
     * 根据发送方地址查询交易（分页）
     */
    Page<Transaction> findByFromAddressOrderByTimestampDesc(String fromAddress, Pageable pageable);

    /**
     * 根据接收方地址查询交易（分页）
     */
    Page<Transaction> findByToAddressOrderByTimestampDesc(String toAddress, Pageable pageable);

    /**
     * 查询地址相关的所有交易（发送或接收）
     */
    @Query("SELECT t FROM Transaction t WHERE t.fromAddress = :address OR t.toAddress = :address ORDER BY t.timestamp DESC")
    Page<Transaction> findByAddress(@Param("address") String address, Pageable pageable);
}
