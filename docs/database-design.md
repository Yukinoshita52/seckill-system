# 数据库设计

数据库：`seckill`，字符集 `utf8mb4`。

## 表结构

### t_seckill_activity — 秒杀活动表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK AUTO_INCREMENT | 活动ID |
| activity_name | VARCHAR(100) | 活动名称 |
| goods_id | BIGINT | 商品ID |
| goods_name | VARCHAR(200) | 商品名称 |
| original_price | DECIMAL(10,2) | 原价 |
| seckill_price | DECIMAL(10,2) | 秒杀价 |
| total_stock | INT | 总库存 |
| bucket_count | INT DEFAULT 5 | 分桶数量 |
| start_time | DATETIME | 开始时间 |
| end_time | DATETIME | 结束时间 |
| status | TINYINT DEFAULT 0 | 0-未开始 1-进行中 2-已结束 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |
| is_deleted | TINYINT DEFAULT 0 | 逻辑删除 |

索引：`idx_status_time (status, start_time, end_time)`

### t_seckill_order — 秒杀订单表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK AUTO_INCREMENT | 订单ID |
| order_no | VARCHAR(64) UNIQUE | 订单号 |
| activity_id | BIGINT | 活动ID |
| user_id | BIGINT | 用户ID |
| seckill_price | DECIMAL(10,2) | 秒杀价 |
| bucket_index | INT | 库存桶索引 |
| status | TINYINT DEFAULT 0 | 0-待支付 1-已支付 2-已取消 3-已超时 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |
| pay_time | DATETIME | 支付时间 |
| is_deleted | TINYINT DEFAULT 0 | 逻辑删除 |

索引：`uk_order_no (order_no)`, `idx_user_id (user_id)`, `idx_status (status, create_time)`

### t_stock_deduct_log — 库存扣减流水表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK AUTO_INCREMENT | 日志ID |
| activity_id | BIGINT | 活动ID |
| user_id | BIGINT | 用户ID |
| bucket_index | INT | 分桶索引 |
| deduct_time | DATETIME | 扣减时间 |
| is_deleted | TINYINT DEFAULT 0 | 逻辑删除 |

索引：`idx_activity (activity_id, deduct_time)`

### t_user — 用户表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK AUTO_INCREMENT | 用户ID |
| username | VARCHAR(50) UNIQUE | 用户名 |
| password | VARCHAR(128) | 密码(BCrypt) |
| nickname | VARCHAR(50) | 昵称 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |
| is_deleted | TINYINT DEFAULT 0 | 逻辑删除 |

## Redis Key 设计

| Key | 类型 | 说明 |
|-----|------|------|
| `stock:{activityId}:{bucketIndex}` | STRING | 各桶剩余库存 |
| `total:{activityId}` | STRING | 活动总剩余库存 |
| `bought:{activityId}` | SET | 已购买用户集合 |

## 分桶策略

库存按 `userId % bucketCount` 分桶，降低单 key 热点压力。
