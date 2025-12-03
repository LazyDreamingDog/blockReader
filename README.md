# 区块链浏览器后端 - 私链数据获取Demo

基于 **Java + Spring Boot + Web3j** 的以太坊私链数据获取示例项目。展示如何从魔改的以太坊私链获取区块、交易和自定义数据。

> **项目目的**: 提供一个完整的代码参考，展示如何与魔改后的以太坊私链交互，获取标准数据和自定义扩展字段。

## 快速开始

### 方式一：启用数据库（推荐）

```bash
# 1. 克隆项目
git clone git@github.com:LazyDreamingDog/blockReader.git
cd blockReader

# 2. 创建数据库
mysql -u root -p
CREATE DATABASE blockchain_explorer CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 3. 配置数据库连接
# 编辑 src/main/resources/application.yml，修改数据库配置：
# spring.datasource.url, username, password

# 4. 启动应用（自动同步区块数据）
mvn spring-boot:run

# 5. 测试RPC连接
curl http://localhost:8080/api/test/connection

# 6. 查询已同步的区块
curl http://localhost:8080/api/blocks/latest
```

### 方式二：仅测试RPC（不使用数据库）

```bash
# 1. 克隆项目
git clone git@github.com:LazyDreamingDog/blockReader.git
cd blockReader

# 2. 禁用数据库
# 编辑 application.yml，注释掉数据库相关配置

# 3. 启动应用
mvn spring-boot:run

# 4. 测试RPC连接
curl http://localhost:8080/api/test/connection

# 5. 获取区块数据（直接从RPC）
curl http://localhost:8080/api/test/block/100

# 6. 获取私链自定义字段
curl http://localhost:8080/api/test/extended-block/100
```

---

## 项目结构说明

```
blockReader/
├── pom.xml                                 # Maven依赖配置
├── src/main/
│   ├── resources/
│   │   └── application.yml                # 应用配置（RPC地址、Chain ID）
│   └── java/com/blockchain/explorer/
│       ├── BlockchainExplorerApplication.java  # 主应用入口
│       │
│       ├── config/                         # 配置类
│       │   └── Web3jConfig.java           # Web3j配置（连接RPC节点）
│       │
│       ├── dto/                            # 数据传输对象
│       │   ├── EthGetSecurityLevel.java   # 自定义RPC响应：eth_getSecurityLevel
│       │   └── ExtendedBlockData.java     # 扩展区块数据（包含15个自定义字段）
│       │
│       ├── entity/                         # 数据库实体
│       │   ├── Block.java                 # 区块实体（含11个自定义字段）
│       │   ├── Transaction.java           # 交易实体（含txType字段）
│       │   └── AccountState.java          # 账户状态（含securityLevel）
│       │
│       ├── repository/                     # 数据访问层
│       │   ├── BlockRepository.java       # 区块数据Repository
│       │   ├── TransactionRepository.java # 交易数据Repository
│       │   └── AccountStateRepository.java# 账户状态Repository
│       │
│       ├── service/                        # 业务逻辑层
│       │   ├── CustomRpcService.java      # ⭐ 自定义RPC调用服务
│       │   ├── ExtendedBlockService.java  # ⭐ 扩展区块数据服务
│       │   └── BlockSyncService.java      # 区块同步服务（已禁用）
│       │
│       └── controller/                     # REST API
│           ├── TestController.java        # ⭐ 测试端点（重点参考）
│           ├── BlockController.java       # 区块查询API
│           ├── TransactionController.java # 交易查询API
│           └── AccountController.java     # 账户查询API
```

---

## 核心组件详解

### 1. CustomRpcService - 自定义RPC调用

**文件**: `service/CustomRpcService.java`

**用途**: 调用私链新增的自定义RPC方法（如 `eth_getSecurityLevel`）

**核心代码**:
```java
public BigInteger getSecurityLevel(String address) {
    Request<?, EthGetSecurityLevel> request = new Request<>(
        "eth_getSecurityLevel",           // 自定义RPC方法名
        Arrays.asList(address, "latest"), // 参数列表
        getWeb3jService(),
        EthGetSecurityLevel.class         // 自定义响应类
    );
    return request.send().getSecurityLevel();
}
```

**如何添加更多自定义方法**:
1. 在 `dto/` 创建响应类（如 `EthGetYourData.java`）
2. 在 `CustomRpcService` 中添加新方法
3. 使用相同的 `Request/Response` 模式

---

### 2. ExtendedBlockService - 获取完整区块数据

**文件**: `service/ExtendedBlockService.java`

**用途**: 获取包含所有自定义字段的完整区块数据

**核心思想**: 使用原始RPC调用，绕过Web3j的标准封装，获取完整JSON数据

**支持的自定义字段**:
- `randomNumber` / `randomRoot` - 随机数相关
- `powDifficulty` / `powGas` / `powPrice` - PoW相关
- `posLeader` / `posVoting` - PoS相关
- `tainted` - 污点交易标记
- `incentive` - 区块奖励
- 等15个扩展字段...

---

### 3. TestController - 测试端点（重点参考）

**文件**: `controller/TestController.java`

**用途**: 提供完整的测试端点，展示各种数据获取方法

**主要端点**:

| 端点 | 说明 | 示例 |
|------|------|------|
| `/api/test/connection` | 测试RPC连接 | 返回Chain ID、当前区块号 |
| `/api/test/block/{number}` | 获取区块头 | 基础区块信息 |
| `/api/test/block-with-transactions/{number}` | 获取区块+交易 | 包含完整交易数组 |
| `/api/test/extended-block/{number}` | 获取扩展区块 | **包含所有自定义字段** |
| `/api/test/security-level/{address}` | 获取安全级别 | 调用自定义RPC方法 |
| `/api/test/account-info/{address}` | 综合测试 | 余额+nonce+自定义字段 |

---

## 私链特有功能

### 1. 支持的自定义交易类型

```java
0x04 = PowTx           // PoW交易（新增）
0x05 = DynamicCryptoTx // 动态加密交易（新增）
0x06 = DepositTx       // 存款交易（新增）
0x07 = NestedTx        // 嵌套交易（新增）
```

### 2. 区块头扩展字段（15个）

```java
randomNumber        // 随机数
randomRoot          // 随机根
powDifficulty       // PoW难度
powGas              // PoW Gas
powPrice            // PoW价格
avgRatioNumerator   // 平均比率分子
avgRatioDenominator // 平均比率分母
avgGasNumerator     // 平均Gas分子
avgGasDenominator   // 平均Gas分母
posLeader           // PoS Leader地址
posVoting           // PoS投票数据
commitTxLength      // Commit交易长度
tainted             // 污点交易标记
incentive           // 激励
baseFee             // 基础费用
```

---

## 配置说明

### application.yml

```yaml
blockchain:
  rpc-url: http://47.243.174.71:36054  # 私链RPC地址
  chain-id: 20251101                   # Chain ID

  sync:
    sync-from-genesis: true            # 是否从创世区块开始同步
    interval: 5000                     # 同步间隔（毫秒）
    batch-size: 10                     # 批量大小
```

### 数据库配置

#### 1. 创建数据库

```sql
CREATE DATABASE blockchain_explorer CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

#### 2. 配置连接信息

编辑 `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/blockchain_explorer?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
    username: your_username  # 修改为你的MySQL用户名
    password: your_password  # 修改为你的MySQL密码
    driver-class-name: com.mysql.cj.jdbc.Driver
  
  jpa:
    hibernate:
      ddl-auto: update  # 自动创建/更新表结构
    show-sql: false     # 是否显示SQL（调试时可设为true）
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
```

#### 3. Maven依赖（已配置）

```xml
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>8.0.33</version>
</dependency>
```

> **⚠️ 注意**: 如果遇到 "Project configuration is not up-to-date" 错误，请确保使用 `mysql-connector-java` 而不是 `mysql-connector-j`。

#### 4. 数据库表结构

应用启动后会自动创建以下表：

- **blocks** - 区块信息（包含11个自定义字段）
- **transactions** - 交易信息（包含txType字段）
- **account_states** - 账户状态（包含securityLevel字段）

#### 5. 区块同步

应用启动后会自动开始同步区块数据：

- 默认从区块0开始同步
- 每5秒同步一批（默认批次大小10个区块）
- 同步进度会在日志中显示

**配置同步参数** (application.yml):

```yaml
blockchain:
  sync:
    sync-from-genesis: true  # true=从创世区块开始，false=从最新区块开始
    interval: 5000           # 同步间隔（毫秒）
    batch-size: 10           # 每批同步区块数
```

---

## 测试示例

### 1. 测试RPC连接

```bash
curl http://localhost:8080/api/test/connection
```

**返回**:
```json
{
  "status": "connected",
  "chainId": "20251101",
  "currentBlockNumber": "325"
}
```

### 2. 获取区块完整交易

```bash
curl http://localhost:8080/api/test/block-with-transactions/100
```

**返回**:
```json
{
  "blockNumber": "100",
  "transactionCount": 1,
  "transactions": [
    {
      "hash": "0x...",
      "from": "0x...",
      "to": "0x...",
      "value": "1000000000000000000",
      "txType": 4  // 交易类型
    }
  ]
}
```

### 3. 获取扩展区块数据（私链特有）

```bash
curl http://localhost:8080/api/test/extended-block/100
```

**返回**:
```json
{
  "blockNumber": "0x64",
  "customFields": {
    "randomNumber": "0x0",
    "posLeader": "0x0000...0001",
    "powDifficulty": "0x0",
    "incentive": "0x96321e3f5c00",
    ...
  }
}
```

### 4. 调用自定义RPC方法

```bash
curl http://localhost:8080/api/test/security-level/0x0e3e917ea21207f0b89befe6bde3101233fa8f90
```

**返回**:
```json
{
  "address": "0x0e3e917ea21207f0b89befe6bde3101233fa8f90",
  "securityLevel": "1",
  "method": "eth_getSecurityLevel"
}
```

---

## 如何扩展新功能

### 添加新的自定义RPC方法

**步骤**:
1. 创建响应DTO (参考 `EthGetSecurityLevel.java`)
2. 在 `CustomRpcService` 添加方法（复制现有模式）
3. 在 `TestController` 添加测试端点

**示例**:
```java
// 1. 创建 dto/EthGetYourData.java
@Data
public class EthGetYourData extends Response<String> {
    public YourType getYourData() {
        return new YourType(getResult());
    }
}

// 2. 在 CustomRpcService.java 添加
public YourType yourCustomMethod(String param) {
    Request<?, EthGetYourData> request = new Request<>(
        "eth_yourMethod",
        Arrays.asList(param, "latest"),
        getWeb3jService(),
        EthGetYourData.class
    );
    return request.send().getYourData();
}

// 3. 在 TestController.java 添加测试端点
@GetMapping("/test/your-data/{param}")
public ResponseEntity<?> testYourData(@PathVariable String param) {
    YourType data = customRpcService.yourCustomMethod(param);
    return ResponseEntity.ok(data);
}
```

### 添加新的区块扩展字段

1. 更新 `ExtendedBlockData.java` - 添加新字段
2. 更新 `Block.java` 实体 - 添加数据库列（如需持久化）
3. 字段会自动从RPC响应中解析

---

## 技术要点

### 1. 如何绕过Web3j限制

Web3j只支持标准以太坊RPC方法。对于自定义方法，使用：

```java
Request<?, CustomResponse> request = new Request<>(
    "your_custom_method",  // 方法名
    params,                // 参数
    getWeb3jService(),     // 获取底层服务
    CustomResponse.class   // 响应类型
);
```

### 2. 自动JSON反序列化

使用 Jackson 的 `@JsonProperty` 注解自动映射：

```java
@JsonProperty("randomNumber")
private String randomNumber;

@JsonProperty("posLeader")
private String posLeader;
```

### 3. 反射获取Web3jService

```java
private Web3jService getWeb3jService() {
    Field field = web3j.getClass().getDeclaredField("web3jService");
    field.setAccessible(true);
    return (Web3jService) field.get(web3j);
}
```

---

## 常见问题

### Q: 遇到 "Project configuration is not up-to-date with pom.xml" 错误？
**A**: 这是Maven配置不同步的问题。解决方法：
1. 确保使用正确的MySQL驱动：`mysql-connector-java`（不是`mysql-connector-j`）
2. 在IDE中刷新Maven项目：
   - **IntelliJ IDEA**: 右键项目 → Maven → Reload Project
   - **Eclipse**: 右键项目 → Maven → Update Project
3. 如果问题持续，尝试清理重新构建：`mvn clean install`

### Q: 数据库连接失败怎么办？
**A**: 检查以下配置：
1. 确认MySQL服务已启动
2. 验证数据库名称、用户名、密码正确
3. 检查端口号（默认3306）
4. 确保MySQL允许远程连接（如需要）
5. 查看application.yml中的连接URL是否正确

### Q: 如何添加更多自定义字段？
**A**: 在 `ExtendedBlockData.java` 中添加字段，使用 `@JsonProperty` 注解指定JSON字段名。

### Q: 如何支持新的交易类型？
**A**: `Transaction` 实体已包含 `txType` 字段，自动识别所有类型（0x00-0x07）。

### Q: 如何禁用数据库功能？
**A**: 
1. 在 `application.yml` 中注释掉整个 `spring.datasource` 和 `spring.jpa` 配置
2. 在 `BlockSyncService.java` 中注释掉 `@Scheduled` 注解
3. 仅使用TestController的端点直接从RPC获取数据

### Q: 区块同步速度慢怎么办？
**A**: 调整同步参数：
- 增加 `batch-size`（建议不超过20）
- 调整 `interval`（但不要太小，避免RPC节点压力）
- 检查网络连接和RPC节点性能

---

## 技术栈

- **Java 11**
- **Spring Boot 2.7.18**
- **Web3j 4.9.8**
- **JPA + MySQL** (可选)
- **Lombok**

---

## 联系方式

有问题请提交 Issue 或查看代码注释。

**核心参考文件**:
- `TestController.java` - 完整的测试示例
- `CustomRpcService.java` - 自定义RPC调用模式
- `ExtendedBlockService.java` - 扩展数据获取

---

## 区块同步状态监控

### 查看同步日志

应用启动后，会在控制台输出同步进度：

```
INFO --- [   scheduling-1] c.b.explorer.service.BlockSyncService : Syncing blocks from 610 to 614 (chain latest: 631)
INFO --- [   scheduling-1] c.b.explorer.service.BlockSyncService : Saved block 610
INFO --- [   scheduling-1] c.b.explorer.service.BlockSyncService : Successfully synced blocks from 610 to 614
```

### 查询已同步的区块

```bash
# 查询最新区块
curl http://localhost:8080/api/blocks/latest

# 查询指定区块
curl http://localhost:8080/api/blocks/100

# 查询区块列表（分页）
curl "http://localhost:8080/api/blocks?page=0&size=10"
```

### 查询交易数据

```bash
# 通过交易哈希查询
curl http://localhost:8080/api/transactions/0x...

# 查询指定区块的所有交易
curl http://localhost:8080/api/transactions/block/100

# 查询指定地址的交易
curl http://localhost:8080/api/transactions/address/0x...
```

### 直接查询数据库

```sql
-- 查看同步进度
SELECT MAX(block_number) as latest_synced_block FROM blocks;

-- 查看区块数量
SELECT COUNT(*) FROM blocks;

-- 查看交易数量
SELECT COUNT(*) FROM transactions;

-- 查看最近10个区块
SELECT block_number, block_hash, transaction_count, timestamp 
FROM blocks 
ORDER BY block_number DESC 
LIMIT 10;

-- 查看包含自定义字段的区块
SELECT block_number, pos_leader, pow_difficulty, incentive, random_number
FROM blocks
WHERE block_number > 100
LIMIT 10;
```

---

祝开发顺利！🚀
