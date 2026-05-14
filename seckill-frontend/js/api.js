// API 基础配置
const API_BASE_URL = 'http://localhost:11010';

// 获取存储的 token
function getToken() {
  return localStorage.getItem('token');
}

// 设置 token
function setToken(token) {
  localStorage.setItem('token', token);
}

// 清除 token
function clearToken() {
  localStorage.removeItem('token');
}

// 获取用户信息
function getUserInfo() {
  const username = localStorage.getItem('username');
  return username ? { username } : null;
}

// 设置用户信息
function setUserInfo(username) {
  localStorage.setItem('username', username);
}

// 清除用户信息
function clearUserInfo() {
  localStorage.removeItem('username');
}

// 通用请求方法
async function request(url, options = {}) {
  const token = getToken();
  const headers = {
    'Content-Type': 'application/json',
    ...options.headers,
  };

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  try {
    const response = await fetch(`${API_BASE_URL}${url}`, {
      ...options,
      headers,
    });

    const data = await response.json();

    // 处理业务错误码
    if (data.code && data.code !== '000000') {
      throw new Error(data.message || '请求失败');
    }

    return data;
  } catch (error) {
    if (error.message === 'Failed to fetch') {
      throw new Error('网络连接失败，请检查网络');
    }
    throw error;
  }
}

// API 方法
const api = {
  // 用户登录
  login(username, password) {
    return request('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify({ username, password }),
    });
  },

  // 用户注册
  register(username, password) {
    return request('/api/auth/register', {
      method: 'POST',
      body: JSON.stringify({ username, password }),
    });
  },

  // 获取活动列表
  getActivities() {
    return request('/api/activity/list');
  },

  // 获取活动详情
  getActivity(id) {
    return request(`/api/activity/${id}`);
  },

  // 秒杀下单
  placeOrder(activityId, captchaToken, captchaCode) {
    return request('/api/seckill/order', {
      method: 'POST',
      body: JSON.stringify({ activityId, captchaToken, captchaCode }),
    });
  },

  // 查询订单状态
  getOrderStatus(orderNo) {
    return request(`/api/order/status/${orderNo}`);
  },
};
