package com.blockchain.explorer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 区块链浏览器后端主应用
 */
@SpringBootApplication
@EnableScheduling  // 启用定时任务支持
public class BlockchainExplorerApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlockchainExplorerApplication.class, args);
    }
}
