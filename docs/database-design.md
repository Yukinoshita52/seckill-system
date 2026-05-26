# 数据库设计

数据库名：`seckill`，字符集：`utf8mb4`

## ER 关系

```
t_user 1──N t_seckill_order N──1 t_seckill_activity
                                    │
                                    └── 1──N t_stock_deduct_log
```

## 表结构

### t_seckill_activity（秒杀活动表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK AUTO_INCREMENT | 活动ID |
| activity_name | VARCHAR(100) NOT NULL | 活动名称 |
| goods_id | BIGINT NOT NULL | 商品ID |
| goods_name | VARCHAR(200) NOT NULL | 商品名称 |
| original_price | DECIMAL(10,2) NOT NULL | 原价 |
| seckill_price | DECIMAL(10,2) NOT NULL | 秒杀价 |
| total_stock | INT NOT NULL | 总库存 |
| bucket_count | INT NOT NULL DEFAULT 5 | 分桶数量 |
| start_time | DATETIME NOT NULL | 开始时间 |
| end_time | DATETIME NOT NULL | 结束时间 |
| status | TINYINT NOT NULL DEFAULT 0 | 0-未开始 1-进行中 2-已结束 |
| create_time | DATETIME DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE | 更新时间 |
| is_deleted | TINYINT NOT NULL DEFAULT 0 | 0-正常 1-已删除 |

索引：`idx_status_time (status, start_time, end_time)`

### t_seckill_order（秒杀订单表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK AUTO_INCREMENT | 订单主键 |
| order_no | VARCHAR(64) NOT NULL | 订单号（唯一） |
| activity_id | BIGINT NOT NULL | 活动ID |
| user_id | BIGINT NOT NULL | 用户ID |
| seckill_price | DECIMAL(10,2) NOT NULL | 秒杀价 |
| bucket_index | INT NOT NULL | 分桶索引（记录从哪个桶扣减） |
| status | TINYINT NOT NULL DEFAULT 0 | 0-待支付 1-已支付 2-已取消 3-已超时 |
| create_time | DATETIME DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE | 更新时间 |
| pay_time | DATETIME | 支付时间 |
| is_deleted | TINYINT NOT NULL DEFAULT 0 | 0-正常 1-已删除 |

索引：`uk_order_no (order_no)`、`idx_user_activity (user_id, activity_id)`、`idx_status (status, create_time)`

### t_stock_deduct_log（库存扣减流水表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK AUTO_INCREMENT | 主键 |
| activity_id | BIGINT NOT NULL | 活动ID |
| user_id | BIGINT NOT NULL | 用户ID |
| bucket_index | INT NOT NULL | 分桶索引 |
| deduct_time | DATETIME DEFAULT CURRENT_TIMESTAMP | 扣减时间 |
| is_deleted | TINYINT NOT NULL DEFAULT 0 | 0-正常 1-已删除 |

索引：`idx_activity (activity_id, deduct_time)`

### t_user（用户表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK AUTO_INCREMENT | 用户ID |
| username | VARCHAR(50) NOT NULL UNIQUE | 用户名 |
| password | VARCHAR(128) NOT NULL | BCrypt 加密密码 |
| nickname | VARCHAR(50) | 昵称 |
| create_time | DATETIME DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE | 更新时间 |
| is_deleted | TINYINT NOT NULL DEFAULT 0 | 0-正常 1-已删除 |

## Redis 数据模型

### 活动信息缓存

- Key: `seckill:activity:{activityId}`
- Type: Hash
- Fields: `activityName`, `goodsName`, `originalPrice`, `seckillPrice`, `totalStock`, `bucketCount`, `startTime`, `endTime`, `status`
- TTL: 活动结束时间 + 2 小时

### 库存分桶

- 总库存 Key: `seckill:stock:total:{activityId}`
- 桶库存 Key: `seckill:stock:bucket:{activityId}:{bucketIndex}`
- 已购用户 Key: `seckill:stock:bought:{activityId}`
- Type: String（总量）/ Set（已购用户 ID）
- 库存扣减由 Lua 脚本原子执行

### 请求去重

- Key: `seckill:request:{activityId}:{userId}`
- Type: String
- TTL: 30 秒

### 订单状态

- Key: `seckill:order:status:{orderNo}`
- Type: String
- Values: `UNPAID` / `SUCCESS` / `TIMEOUT`

### 验证码

- Key: `seckill:captcha:{token}`
- Type: String（存储验证码答案，不区分大小写）
- TTL: 5 分钟，验证后立即删除（一次性使用）

## Lua 脚本

### stock_deduct.lua

原子执行：已购校验 → 总库存校验 → 桶库存校验 → 扣减 → 记录已购用户

```lua
-- KEYS[1] = bought key
-- KEYS[2] = total stock key
-- KEYS[3] = bucket stock key
-- ARGV[1] = userId
-- ARGV[2] = bucketIndex
local bought = redis.call('SISMEMBER', KEYS[1], ARGV[1])
if bought == 1 then return -1 end
local total = tonumber(redis.call('GET', KEYS[2]))
if not total or total <= 0 then return -2 end
local bucket = tonumber(redis.call('GET', KEYS[3]))
if not bucket or bucket <= 0 then return -3 end
redis.call('SADD', KEYS[1], ARGV[1])
redis.call('DECR', KEYS[2])
redis.call('DECR', KEYS[3])
return 0
```

返回值：0=成功，-1=已购买，-2=总库存不足，-3=桶库存不足
