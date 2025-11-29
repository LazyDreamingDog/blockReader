package com.blockchain.explorer.controller;

import com.blockchain.explorer.entity.AccountState;
import com.blockchain.explorer.repository.AccountStateRepository;
import com.blockchain.explorer.service.CustomRpcService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 账户查询API（包含自定义字段）
 */
@Slf4j
@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    @Autowired
    private AccountStateRepository accountStateRepository;

    @Autowired
    private CustomRpcService customRpcService;

    @Autowired
    private Web3j web3j;

    /**
     * 查询账户完整信息（包括余额和安全级别）
     */
    @GetMapping("/{address}")
    public ResponseEntity<AccountState> getAccount(@PathVariable String address) {
        return accountStateRepository.findByAddress(address)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 仅查询余额
     */
    @GetMapping("/{address}/balance")
    public ResponseEntity<Map<String, Object>> getBalance(@PathVariable String address) {
        try {
            BigInteger balance = web3j.ethGetBalance(
                    address,
                    DefaultBlockParameter.valueOf("latest")).send().getBalance();

            Map<String, Object> result = new HashMap<>();
            result.put("address", address);
            result.put("balance", balance.toString());
            result.put("balanceInEther", balance.divide(BigInteger.TEN.pow(18)).toString());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error getting balance for {}", address, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 仅查询安全级别（私链自定义字段）
     */
    @GetMapping("/{address}/security-level")
    public ResponseEntity<Map<String, Object>> getSecurityLevel(@PathVariable String address) {
        try {
            BigInteger securityLevel = customRpcService.getSecurityLevel(address);

            Map<String, Object> result = new HashMap<>();
            result.put("address", address);
            result.put("securityLevel", securityLevel.toString());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error getting security level for {}", address, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 手动触发刷新账户状态
     */
    @PostMapping("/refresh/{address}")
    public ResponseEntity<AccountState> refreshAccount(@PathVariable String address) {
        try {
            // 获取余额
            BigInteger balance = web3j.ethGetBalance(
                    address,
                    DefaultBlockParameter.valueOf("latest")).send().getBalance();

            // 获取安全级别
            BigInteger securityLevel = customRpcService.getSecurityLevel(address);

            // 更新或创建账户状态
            AccountState accountState = accountStateRepository.findByAddress(address)
                    .orElse(new AccountState());

            accountState.setAddress(address);
            accountState.setBalance(balance);
            accountState.setSecurityLevel(securityLevel);
            accountState.setLastUpdated(LocalDateTime.now());

            accountState = accountStateRepository.save(accountState);

            log.info("Refreshed account state for {}", address);
            return ResponseEntity.ok(accountState);

        } catch (Exception e) {
            log.error("Error refreshing account state for {}", address, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
