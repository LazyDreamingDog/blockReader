# 扩展账户API使用指南

## 概述

本项目支持获取私链StateAccount的所有扩展字段，包括利息、质押、后量子、统计等25个自定义字段。**自动区分EOA账户和合约账户**。

## API端点

### 1. 获取账户完整扩展信息（推荐）

自动区分EOA和合约账户，返回所有相关字段。

```bash
GET /api/account/{address}/full-info
```

**示例**:
```bash
curl http://localhost:8080/api/account/0x0e3e917ea21207f0b89befe6bde3101233fa8f90/full-info
```

**返回示例**:
```json
{
  "address": "0x0e3e917ea21207f0b89befe6bde3101233fa8f90",
  "isContract": false,
  "basicInfo": {
    "balance": "18899998613999999993700",
    "nonce": "66",
    "securityLevel": "1",
    "postQuanCounter": "0"
  },
  "interestInfo": {
    "interest": "5000",
    "currentInterest": "5500",
    "earnInterest": "500",
    "interestRate": "5"
  },
  "statsInfo": {
    "totalNumberOfGas": "210000",
    "contractCallCount": "10",
    "totalValueTx": "1000000000000000000"
  },
  "pledgeInfo": {
    "pledgedAmount": "10000000000000000000",
    "pledgeYear": "2",
    "startTime": "100",
    "stakeFlag": true,
    "pledgeInfoRaw": "..."
  }
}
```

---

### 2. 获取基础信息

```bash
GET /api/account/{address}/basic
```

包含：
- balance（余额）
- nonce
- securityLevel（安全级别）
- postQuanCounter（后量子计数器）

---

### 3. 获取利息相关信息

```bash
GET /api/account/{address}/interest
```

包含：
- interest（利息）
- currentInterest（当前利息）
- earnInterest（收益利息）
- interestRate（利率）

---

### 4. 获取统计信息

```bash
GET /api/account/{address}/stats
```

包含：
- totalNumberOfGas（Gas总量）
- contractCallCount（合约调用次数）
- totalValueTx（交易总价值）

---

### 5. 获取质押信息（仅EOA账户）

```bash
GET /api/account/{address}/pledge
```

包含：
- pledgedAmount（质押金额）
- pledgeYear（质押年限）
- startTime（开始时间/区块高度）
- stakeFlag（质押标志）
- pledgeInfoRaw（原始质押信息）

**注意**: 合约账户调用此接口会返回400错误。

---

### 6. 获取合约信息（仅合约账户）

```bash
GET /api/account/{address}/contract
```

包含：
- annualFee（合约年费）
- lastAnnualFeeTime（上次收取年费时间）
- deployedAddress（部署人地址）
- investorAddress（投资人地址）
- beneficiaryAddress（受益人地址）

**注意**: EOA账户调用此接口会返回400错误。

---

## 账户类型自动识别

系统会自动判断账户类型：
- **EOA账户**: `code == 0x` - 返回质押信息
- **合约账户**: `code != 0x` - 返回合约相关信息

---

## 完整字段列表

### 所有账户共有（25个字段）

| 分类 | 字段名 | 说明 | RPC方法 |
|------|--------|------|---------|
| **基础** | balance | 余额 | eth_getBalance |
| | nonce | 交易序号 | eth_getTransactionCount |
| | securityLevel | 安全级别(1-5) | eth_getSecurityLevel |
| **利息** | interest | 利息 | eth_getInterest |
| | lastBlockNumber | 上次计息区块号 | - |
| | currentInterest | 当前利息 | eth_getCurrentInterest |
| | earnInterest | 收益利息 | eth_getEarnInterest |
| | interestRate | 利率 | eth_getInterestRate |
| **后量子** | lastPostQuanPub | 后量子公钥 | - |
| | postQuanCounter | 后量子计数器 | eth_getPostQuanCounter |
| **统计** | totalNumberOfGas | Gas总量 | eth_getTotalNumberOfGas |
| | contractCallCount | 合约调用次数 | eth_getContractCallCount |
| | totalValueTx | 交易总价值 | eth_getTotalValueTx |

### EOA账户特有（质押信息）

| 字段名 | 说明 | RPC方法 |
|--------|------|---------|
| pledgeAmount | 质押金额 | eth_getPledgeAmount |
| pledgeYear | 质押年限 | eth_getPledgeYear |
| startTime | 开始时间 | eth_getStartTime |
| stakeFlag | 质押标志 | eth_getStakeFlag |
| pledgeInfo | 整合信息 | eth_getPledgeInfo |

### 合约账户特有（年费和角色）

| 字段名 | 说明 | RPC方法 |
|--------|------|---------|
| annualFee | 合约年费 | eth_getAnnualFee |
| lastAnnualFeeTime | 上次收年费时间 | eth_getLastAnnualFeeTime |
| contractAddress | 合约地址 | - |
| deployedAddress | 部署人地址 | eth_getDeployedAddress |
| investorAddress | 投资人地址 | eth_getInvestorAddress |
| beneficiaryAddress | 受益人地址 | eth_getBeneficiaryAddress |

---

## 测试示例

### 测试EOA账户

```bash
# 完整信息
curl http://localhost:8080/api/account/0x0e3e917ea21207f0b89befe6bde3101233fa8f90/full-info

# 仅质押信息
curl http://localhost:8080/api/account/0x0e3e917ea21207f0b89befe6bde3101233fa8f90/pledge

# 仅利息信息
curl http://localhost:8080/api/account/0x0e3e917ea21207f0b89befe6bde3101233fa8f90/interest
```

### 测试合约账户

```bash
# 完整信息
curl http://localhost:8080/api/account/0xContractAddress/full-info

# 仅合约信息
curl http://localhost:8080/api/account/0xContractAddress/contract
```

---

## 如何扩展

### 添加新的RPC方法

1. 在 `ExtendedAccountService.java` 添加方法：

```java
public BigInteger getYourNewField(String address) {
    return callBigIntegerMethod("eth_getYourNewField", address);
}
```

2. 在 `ExtendedAccountController.java` 添加到相应的信息组：

```java
info.put("yourNewField", extendedAccountService.getYourNewField(address).toString());
```

3. 更新 `AccountState.java` 实体（如需持久化）

---

## 常见问题

### Q: 如何判断账户类型？
A: 调用 `/full-info` 端点，查看 `isContract` 字段。

### Q: 如何获取单个字段？
A: 目前通过分类端点获取（basic/interest/stats/pledge/contract），暂不支持单字段查询。

### Q: 某些字段返回0是正常的吗？
A: 是的。如果账户未设置某些字段（如未质押），会返回0或默认值。

---

## 核心文件

- `ExtendedAccountService.java` - 所有自定义RPC调用
- `ExtendedAccountController.java` - RESTful API端点
- `AccountState.java` - 数据库实体（25个字段）

---

完整API测试请看：`README.md`
