# 接口文档

Base URL: `http://localhost:11020`

Knife4j 在线文档: `http://localhost:11020/doc.html`

## 统一响应格式

```json
{
  "code": "0",
  "message": "success",
  "data": {},
  "requestId": "xxx"
}
```

code 为 `"0"` 表示成功，其他为失败。

---

## 认证接口

### 用户登录

```
POST /api/auth/login
```

**请求体:**

```json
{
  "username": "user001",
  "password": "123456"
}
```

**响应 data:**

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

### 用户注册

```
POST /api/auth/register
```

**请求体:**

```json
{
  "username": "newuser",
  "password": "123456"
}
```

---

## 活动接口

### 查询活动列表

```
GET /api/activity/list
```

**响应 data:**

```json
[
  {
    "id": 1,
    "activityName": "iPhone 16 秒杀",
    "goodsName": "iPhone 16 256G",
    "originalPrice": 6999.00,
    "seckillPrice": 4999.00,
    "totalStock": 1000,
    "remainStock": 850,
    "startTime": "2026-06-01T10:00:00",
    "endTime": "2026-06-01T12:00:00",
    "status": 1
  }
]
```

status: 0-未开始 1-进行中 2-已结束

### 查询活动详情

```
GET /api/activity/{id}
```

### 检查活动是否售罄

```
GET /api/activity/checkSoldOut/{activityId}
```

---

## 秒杀接口

### 获取验证码

```
GET /api/seckill/captcha
```

**响应 data:**

```json
{
  "captchaToken": "8967b6c6-d373-4e0f-8bb2-253ea280cc34",
  "captchaImage": "data:image/png;base64,iVBORw0KGgo..."
}
```

captchaImage 为完整的 data URI，可直接用作 `<img src>`。

### 秒杀下单

```
POST /api/seckill/order
Content-Type: application/json
```

**请求体:**

```json
{
  "activityId": 1,
  "captchaToken": "8967b6c6-d373-4e0f-8bb2-253ea280cc34",
  "captchaCode": "a3xf"
}
```

captchaToken 和 captchaCode 为必填，由验证码接口获取。

**响应 data:**

```json
{
  "orderNo": "SK20260601100000123456",
  "status": "UNPAID",
  "message": "抢购成功，请支付"
}
```

---

## 订单接口

### 查询订单状态

```
GET /api/order/status/{orderNo}
```

**响应 data:**

```json
{
  "orderNo": "SK20260601100000123456",
  "status": "UNPAID",
  "seckillPrice": 4999.00,
  "goodsName": "iPhone 16 256G"
}
```

status: UNPAID / SUCCESS / TIMEOUT

### 支付订单

```
POST /api/order/pay/{orderNo}
```

### 取消订单

```
POST /api/order/cancel/{orderNo}
```

### 我的订单列表

```
GET /api/order/list
```

---

## 管理接口

### 创建活动

```
POST /api/admin/activity
```

### 更新活动

```
PUT /api/admin/activity/{id}
```

### 查询活动详情

```
GET /api/admin/activity/{id}
```

### 活动列表

```
GET /api/admin/activity/list
```

### 初始化活动缓存（Redis 预热）

```
POST /api/admin/activity/{id}/init-cache
```

将活动信息写入 Redis Hash，按 bucketCount 分桶初始化库存 key。

---

## 健康检查

```
GET /api/health
```

---

## 错误码

| 错误码 | 含义 |
|--------|------|
| A000100 | 参数错误（活动不存在等） |
| A000200 | 活动未开始 |
| A000300 | 活动已结束 |
| A000400 | 用户已购买 |
| A000401 | 请求过于频繁（30s 内重复） |
| A000410 | 库存不足 |
| A000500 | 未登录 / 登录已过期 |
| A000600 | 验证码不能为空 |
| A000601 | 验证码已过期 |
| A000602 | 验证码错误 |
| B000100 | 服务端异常（库存扣减异常等） |
| B000200 | 系统繁忙 |
| B000300 | Sentinel 限流 |
