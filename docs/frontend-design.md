# 前端设计

技术栈：React 18 + TypeScript + Vite + Semi UI + Tailwind CSS + Zustand + Axios

## 项目结构

```
seckill-frontend-react/
├── src/
│   ├── api/              # 接口层
│   │   ├── index.ts      # Axios 实例、拦截器
│   │   ├── auth.ts       # 登录/注册
│   │   ├── activity.ts   # 活动接口
│   │   └── order.ts      # 订单/验证码接口
│   ├── components/       # 公共组件
│   │   ├── CaptchaDialog.tsx   # 验证码弹窗
│   │   ├── SeckillButton.tsx   # 抢购按钮
│   │   ├── SeckillResult.tsx   # 下单结果弹窗
│   │   └── index.ts            # 统一导出
│   ├── pages/            # 页面
│   │   ├── Home.tsx      # 首页（活动列表）
│   │   └── Login.tsx     # 登录页
│   ├── store/            # Zustand 状态管理
│   │   └── useAuthStore.ts
│   ├── types/            # TypeScript 类型定义
│   │   ├── api.ts
│   │   ├── activity.ts
│   │   └── order.ts
│   ├── App.tsx
│   └── main.tsx
├── index.html
├── package.json
├── tailwind.config.js
├── tsconfig.json
└── vite.config.ts
```

## 页面与路由

| 路径 | 页面 | 说明 |
|------|------|------|
| `/` | Home | 首页，展示活动卡片列表 |
| `/login` | Login | 登录/注册页 |

## 核心交互流程

### 秒杀下单

```
用户点击「立即抢购」
  → 弹出 CaptchaDialog
  → 自动调用 GET /seckill/captcha 获取验证码图片
  → 用户输入验证码，点击「确认抢购」
  → 携带 captchaToken + captchaCode 调用 POST /seckill/order
  → 成功：SeckillResult 弹窗展示订单信息
  → 失败：Toast 提示错误原因
```

### 验证码弹窗（CaptchaDialog）

- 打开时自动获取验证码图片
- 点击验证码图片可刷新
- 右侧刷新按钮可重新获取
- 支持回车键提交
- 验证码图片为 Base64 data URI，直接作为 `<img src>` 渲染

### 抢购按钮（SeckillButton）

按钮状态逻辑：

| 条件 | 按钮文本 | 状态 |
|------|---------|------|
| activity.status === 0 | 未开始 | disabled |
| activity.soldOut === true | 已售罄 | disabled |
| activity.status !== 1 | 已结束 | disabled |
| orderStatus === 'UNPAID' | 抢购成功 | disabled |
| orderStatus === 'FAILED' | 库存不足 | disabled |
| activity.status === 1 | 立即抢购 | 可点击 |

## API 请求

### Axios 实例配置

- Base URL: `/api`（开发环境通过 Vite proxy 转发至 Gateway）
- 请求拦截：自动附加 `Authorization: Bearer {token}` 头
- 响应拦截：统一处理 `code !== "0"` 的错误，自动 Toast 提示；401 自动跳转登录页

### 接口列表

| 方法 | 函数 | 端点 |
|------|------|------|
| POST | `authApi.login` | `/auth/login` |
| POST | `authApi.register` | `/auth/register` |
| GET | `activityApi.getList` | `/activity/list` |
| GET | `activityApi.getDetail` | `/activity/{id}` |
| GET | `activityApi.checkSoldOut` | `/activity/checkSoldOut/{id}` |
| GET | `orderApi.getCaptcha` | `/seckill/captcha` |
| POST | `orderApi.placeOrder` | `/seckill/order` |
| GET | `orderApi.getOrderStatus` | `/order/status/{orderNo}` |
| POST | `orderApi.payOrder` | `/order/pay/{orderNo}` |
| POST | `orderApi.cancelOrder` | `/order/cancel/{orderNo}` |
| GET | `orderApi.getOrderList` | `/order/list` |

## 状态管理

使用 Zustand 管理 auth 状态：

- `token`: JWT token，存储在 localStorage
- `isAuthenticated`: 是否已登录
- `login(username, password)`: 登录
- `register(username, password)`: 注册
- `logout()`: 退出登录
