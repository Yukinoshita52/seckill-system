# 秒杀系统

面向电商高并发场景的秒杀平台，核心解决瞬时流量洪峰下的限流防护、库存防超卖与异步削峰问题。

## 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| 语言 | Java | 17 |
| 框架 | Spring Boot / Spring Cloud / Spring Cloud Alibaba | 3.0.7 / 2022.0.3 / 2022.0.0.0-RC2 |
| ORM | MyBatis-Plus | 3.5.7 |
| 缓存 | Redis + Caffeine | Redisson 3.27.2 / Caffeine 3.1.8 |
| 消息队列 | RocketMQ | 2.3.0 |
| 限流 | Sentinel | (via Spring Cloud Alibaba) |
| 注册中心 | Nacos | (via Spring Cloud Alibaba) |
| 网关 | Spring Cloud Gateway | (via Spring Cloud) |
| 认证 | JWT (jjwt) | 0.12.6 |
| 验证码 | easy-captcha | 1.6.2 |
| 前端 | React 18 + Semi UI + TypeScript + Vite + Tailwind CSS | |
| 数据库 | MySQL 8.0 | |

## 模块结构

```
seckill-system
├── seckill-framework     # 基础层：统一异常/响应/Redis序列化/JWT/用户上下文
├── seckill-engine        # 核心层：秒杀下单、库存扣减、责任链校验、MQ生产/消费
├── seckill-gateway       # 网关层：JWT鉴权、路由转发、CORS、Sentinel
├── seckill-admin         # 管理层：活动CRUD、Redis缓存预热
└── seckill-frontend-react  # 前端：活动列表、秒杀下单、订单管理
```

服务端口：Gateway(11010) → Engine(11020) / Admin(11030)

## 核心架构

### 三层限流防线

1. **网关层**：Spring Cloud Gateway JWT 鉴权过滤非法请求
2. **接口层**：Sentinel `@SentinelResource` QPS 限流 + 热点参数限流（按 activityId）
3. **应用层**：责任链校验（30s 用户去重 + 验证码人机校验 + 库存预检）

### 秒杀下单流程

```
POST /api/seckill/order
  → 责任链校验（参数→验证码→活动状态→去重→库存预检）
  → Redis Lua 原子扣库存（分桶策略）
  → 写后读校验
  → DB 插入订单(UNPAID)
  → RocketMQ syncSend → 消费者写审计日志
```

### 库存分桶策略

库存按 `userId % bucketCount` 分桶，将单 key 写热点拆散至多桶，降低 Redis 热点竞争。Lua 脚本原子执行：去重校验 → 桶库存校验 → 扣减 → 记录已购用户。

### 静态/动态数据分离

- **静态数据**（活动信息）：直接从 Redis Hash 读取，双检锁防缓存击穿、空标记防缓存穿透
- **动态数据**（库存总量）：Caffeine(3s TTL) → Redis 两级缓存，售罄时本地快速拒绝

## 快速启动

### 1. 初始化数据库

```bash
mysql -u root -p < sql/init.sql
```

### 2. 配置环境变量

各模块的 `application-dev.yml` 和 `application-test.yml` 未纳入版本控制，请参考对应的 `.template` 文件填写真实配置：

```bash
# 从模板创建本地配置
cp seckill-engine/src/main/resources/application-dev.yml.template \
   seckill-engine/src/main/resources/application-dev.yml

cp seckill-engine/src/test/resources/application-test.yml.template \
   seckill-engine/src/test/resources/application-test.yml

cp seckill-admin/src/main/resources/application-dev.yml.template \
   seckill-admin/src/main/resources/application-dev.yml

cp seckill-admin/src/test/resources/application-test.yml.template \
   seckill-admin/src/test/resources/application-test.yml

# 填写真实值
# MYSQL_HOST, MYSQL_PORT, MYSQL_PASSWORD
# REDIS_HOST, REDIS_PORT
# ROCKETMQ_NAMESERVER
# JWT_SECRET
```

### 3. 启动服务

```bash
# 启动顺序：Nacos → Redis → MySQL → RocketMQ
# 然后：
cd seckill-system
mvn clean package -DskipTests

java -jar seckill-gateway/target/seckill-gateway-0.0.1-SNAPSHOT.jar
java -jar seckill-engine/target/seckill-engine-0.0.1-SNAPSHOT.jar
java -jar seckill-admin/target/seckill-admin-0.0.1-SNAPSHOT.jar
```

### 4. 缓存预热

通过 Admin 模块初始化活动 Redis 缓存：

```
POST http://localhost:11030/api/admin/activity/{id}/init-cache
```

### 5. 前端

```bash
cd seckill-frontend-react
npm install
npm run dev
```

## 文档

- [接口文档](docs/api-design.md)
- [数据库设计](docs/database-design.md)
- [前端设计](docs/frontend-design.md)

在线接口文档：`http://localhost:11020/doc.html` (Knife4j)