package com.blockchain.explorer.controller;

import com.blockchain.explorer.service.ExtendedAccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * 扩展账户控制器
 * 提供EOA账户和合约账户的所有扩展字段查询
 */
@Slf4j
@RestController
@RequestMapping("/api/account")
public class ExtendedAccountController {

    @Autowired
    private Web3j web3j;

    @Autowired
    private ExtendedAccountService extendedAccountService;

    /**
     * 判断是否为合约账户
     */
    private boolean isContract(String address) {
        try {
            String code = web3j.ethGetCode(address, DefaultBlockParameter.valueOf("latest"))
                    .send().getCode();
            return code != null && !code.equals("0x");
        } catch (Exception e) {
            log.error("Error checking if address {} is contract", address, e);
            return false;
        }
    }

    /**
     * 获取账户完整扩展信息（自动区分EOA和合约）
     */
    @GetMapping("/{address}/full-info")
    public ResponseEntity<Map<String, Object>> getFullAccountInfo(@PathVariable String address) {
        try {
            Map<String, Object> result = new HashMap<>();
            boolean isContract = isContract(address);

            result.put("address", address);
            result.put("isContract", isContract);

            // 基础信息（所有账户共有）
            Map<String, Object> basicInfo = getBasicInfoData(address);
            result.put("basicInfo", basicInfo);

            // 利息相关（所有账户共有）
            Map<String, Object> interestInfo = getInterestInfoData(address);
            result.put("interestInfo", interestInfo);

            // 统计信息（所有账户共有）
            Map<String, Object> statsInfo = getStatsInfoData(address);
            result.put("statsInfo", statsInfo);

            // 质押信息（主要用于EOA）
            if (!isContract) {
                Map<String, Object> pledgeInfo = getPledgeInfoData(address);
                result.put("pledgeInfo", pledgeInfo);
            }

            // 合约相关信息（仅合约账户）
            if (isContract) {
                Map<String, Object> contractInfo = getContractInfoData(address);
                result.put("contractInfo", contractInfo);
            }

            log.info("Successfully fetched full info for {} (contract={})", address, isContract);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Failed to fetch full info for {}", address, e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 获取基础信息
     */
    @GetMapping("/{address}/basic")
    public ResponseEntity<Map<String, Object>> getBasicInfo(@PathVariable String address) {
        try {
            Map<String, Object> info = getBasicInfoData(address);
            return ResponseEntity.ok(info);
        } catch (Exception e) {
            log.error("Failed to fetch basic info for {}", address, e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 获取利息相关信息
     */
    @GetMapping("/{address}/interest")
    public ResponseEntity<Map<String, Object>> getInterestInfo(@PathVariable String address) {
        try {
            Map<String, Object> info = getInterestInfoData(address);
            return ResponseEntity.ok(info);
        } catch (Exception e) {
            log.error("Failed to fetch interest info for {}", address, e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 获取统计信息
     */
    @GetMapping("/{address}/stats")
    public ResponseEntity<Map<String, Object>> getStatsInfo(@PathVariable String address) {
        try {
            Map<String, Object> info = getStatsInfoData(address);
            return ResponseEntity.ok(info);
        } catch (Exception e) {
            log.error("Failed to fetch stats info for {}", address, e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 获取质押信息（EOA账户）
     */
    @GetMapping("/{address}/pledge")
    public ResponseEntity<Map<String, Object>> getPledgeInfo(@PathVariable String address) {
        try {
            if (isContract(address)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Contract accounts don't have pledge info"));
            }
            Map<String, Object> info = getPledgeInfoData(address);
            return ResponseEntity.ok(info);
        } catch (Exception e) {
            log.error("Failed to fetch pledge info for {}", address, e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 获取合约信息（合约账户）
     */
    @GetMapping("/{address}/contract")
    public ResponseEntity<Map<String, Object>> getContractInfo(@PathVariable String address) {
        try {
            if (!isContract(address)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Not a contract account"));
            }
            Map<String, Object> info = getContractInfoData(address);
            return ResponseEntity.ok(info);
        } catch (Exception e) {
            log.error("Failed to fetch contract info for {}", address, e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // ===== 私有辅助方法 =====

    private Map<String, Object> getBasicInfoData(String address) throws Exception {
        Map<String, Object> info = new HashMap<>();

        // 余额
        BigInteger balance = web3j.ethGetBalance(address, DefaultBlockParameter.valueOf("latest"))
                .send().getBalance();
        info.put("balance", balance.toString());

        // Nonce
        BigInteger nonce = web3j.ethGetTransactionCount(address, DefaultBlockParameter.valueOf("latest"))
                .send().getTransactionCount();
        info.put("nonce", nonce.toString());

        // 安全级别
        BigInteger securityLevel = extendedAccountService.getInterest(address); // 使用自定义服务
        info.put("securityLevel", securityLevel.toString());

        // 后量子计数器
        BigInteger postQuanCounter = extendedAccountService.getPostQuanCounter(address);
        info.put("postQuanCounter", postQuanCounter.toString());

        return info;
    }

    private Map<String, Object> getInterestInfoData(String address) {
        Map<String, Object> info = new HashMap<>();

        info.put("interest", extendedAccountService.getInterest(address).toString());
        info.put("currentInterest", extendedAccountService.getCurrentInterest(address).toString());
        info.put("earnInterest", extendedAccountService.getEarnInterest(address).toString());
        info.put("interestRate", extendedAccountService.getInterestRate(address).toString());

        return info;
    }

    private Map<String, Object> getStatsInfoData(String address) {
        Map<String, Object> info = new HashMap<>();

        info.put("totalNumberOfGas", extendedAccountService.getTotalNumberOfGas(address).toString());
        info.put("contractCallCount", extendedAccountService.getContractCallCount(address).toString());
        info.put("totalValueTx", extendedAccountService.getTotalValueTx(address).toString());

        return info;
    }

    private Map<String, Object> getPledgeInfoData(String address) {
        Map<String, Object> info = new HashMap<>();

        info.put("pledgedAmount", extendedAccountService.getPledgeAmount(address).toString());
        info.put("pledgeYear", extendedAccountService.getPledgeYear(address).toString());
        info.put("startTime", extendedAccountService.getStartTime(address).toString());
        info.put("stakeFlag", extendedAccountService.getStakeFlag(address));

        // 整合接口
        String pledgeInfoStr = extendedAccountService.getPledgeInfo(address);
        info.put("pledgeInfoRaw", pledgeInfoStr);

        return info;
    }

    private Map<String, Object> getContractInfoData(String address) {
        Map<String, Object> info = new HashMap<>();

        info.put("annualFee", extendedAccountService.getAnnualFee(address).toString());
        info.put("lastAnnualFeeTime", extendedAccountService.getLastAnnualFeeTime(address).toString());
        info.put("deployedAddress", extendedAccountService.getDeployedAddress(address));
        info.put("investorAddress", extendedAccountService.getInvestorAddress(address));
        info.put("beneficiaryAddress", extendedAccountService.getBeneficiaryAddress(address));

        return info;
    }
}
