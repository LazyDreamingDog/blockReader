package com.blockchain.explorer.repository;

import com.blockchain.explorer.entity.AccountState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 账户状态数据访问接口
 */
@Repository
public interface AccountStateRepository extends JpaRepository<AccountState, Long> {

    /**
     * 根据地址查询账户状态
     */
    Optional<AccountState> findByAddress(String address);

    /**
     * 检查地址是否存在
     */
    boolean existsByAddress(String address);
}
