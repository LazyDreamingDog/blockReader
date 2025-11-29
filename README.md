# 区块链浏览器后端 - 私链数据获取Demo

基于 **Java + Spring Boot + Web3j** 的以太坊私链数据获取示例项目。展示如何从魔改的以太坊私链获取区块、交易和自定义数据。

> **项目目的**: 提供一个完整的代码参考，展示如何与魔改后的以太坊私链交互，获取标准数据和自定义扩展字段。

## 快速开始

```bash
# 1. 克隆项目
git clone git@github.com:LazyDreamingDog/blockReader.git
cd blockReader

# 2. 启动应用（无需配置数据库）
mvn spring-boot:run

#3. 测试RPC连接
curl http://localhost:8080/api/test/connection

# 4. 获取区块数据
curl http://localhost:8080/api/test/block/100

# 5. 获取私链自定义字段
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

### 数据库配置（可选）

当前已禁用数据库，无需配置即可运行。如需启用：

1. 创建数据库: `CREATE DATABASE blockchain_explorer;`
2. 修改 `application.yml`: `ddl-auto: update`
3. 取消 `BlockSyncService` 中定时任务注释

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

### Q: 如何添加更多自定义字段？
A: 在 `ExtendedBlockData.java` 中添加字段，使用 `@JsonProperty` 注解指定JSON字段名。

### Q: 如何支持新的交易类型？
A: `Transaction` 实体已包含 `txType` 字段，自动识别所有类型（0x00-0x07）。

### Q: 如何启用数据库存储？
A: 取消 `BlockSyncService` 的定时任务注释，修改 `application.yml` 中 `ddl-auto: update`。

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

祝开发顺利！🚀
