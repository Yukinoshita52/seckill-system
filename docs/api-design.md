# 接口文档

Base URL: `http://localhost:11020`

Knife4j 文档: `http://localhost:11020/doc.html`

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

## 活动接口

### 查询活动详情

```
GET /api/activity/{id}
```

**响应 data:**

```json
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
```

status: 0-未开始 1-进行中 2-已结束

---

## 秒杀接口

### 秒杀下单

```
POST /api/seckill/order
Content-Type: application/json
```

**请求体:**

```json
{
  "activityId": 1,
  "userId": 1,
  "captchaToken": "xxx",
  "captchaCode": "abcd"
}
```

**响应 data:**

```json
{
  "orderNo": "SK20260601100000001",
  "status": "PENDING",
  "message": "排队中，请稍后查询"
}
```

status: `PENDING`-排队中 / `SUCCESS`-下单成功 / `FAILED`-失败

### 查询订单状态

```
GET /api/order/status/{orderNo}
```

**响应 data:**

```json
{
  "orderNo": "SK20260601100000001",
  "status": "SUCCESS",
  "seckillPrice": 4999.00,
  "goodsName": "iPhone 16 256G"
}
```

status: `PENDING`-处理中 / `SUCCESS`-成功 / `FAILED`-失败 / `TIMEOUT`-超时

---

## 健康检查

```
GET /api/health
```

---

## 错误码

| 错误码 | 含义 |
|--------|------|
| A000100 | 客户端参数错误（活动不存在等） |
| A000400 | 用户已购买 |
| A000410 | 库存不足 |
| B000100 | 服务端异常 |
