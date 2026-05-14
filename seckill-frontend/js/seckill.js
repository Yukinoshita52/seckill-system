// 全局状态
let activities = [];
let countdownTimers = {};

// 页面加载完成后初始化
document.addEventListener('DOMContentLoaded', () => {
  checkLoginStatus();
  loadActivities();
});

// 检查登录状态
function checkLoginStatus() {
  const user = getUserInfo();
  const loginBtn = document.getElementById('loginBtn');
  const logoutBtn = document.getElementById('logoutBtn');
  const userInfo = document.getElementById('userInfo');

  if (user) {
    loginBtn.style.display = 'none';
    logoutBtn.style.display = 'block';
    userInfo.textContent = `欢迎, ${user.username}`;
  } else {
    loginBtn.style.display = 'block';
    logoutBtn.style.display = 'none';
    userInfo.textContent = '';
  }
}

// 加载活动列表
async function loadActivities() {
  const activityList = document.getElementById('activityList');

  try {
    const response = await api.getActivities();
    activities = response.data || [];

    if (activities.length === 0) {
      activityList.innerHTML = '<div class="empty">暂无秒杀活动</div>';
      return;
    }

    renderActivities(activities);
  } catch (error) {
    activityList.innerHTML = `<div class="empty">加载失败: ${error.message}</div>`;
  }
}

// 渲染活动列表
function renderActivities(activities) {
  const activityList = document.getElementById('activityList');

  // 清除之前的倒计时
  Object.values(countdownTimers).forEach(timer => clearCountdown(timer));
  countdownTimers = {};

  activityList.innerHTML = activities.map(activity => {
    const stockPercent = activity.totalStock > 0
      ? Math.round((activity.remainStock / activity.totalStock) * 100)
      : 0;

    const statusClass = activity.status === 0 ? 'status-pending'
      : activity.status === 1 ? 'status-active'
      : 'status-ended';

    const statusText = activity.status === 0 ? '未开始'
      : activity.status === 1 ? '进行中'
      : '已结束';

    const buttonDisabled = activity.status !== 1 || activity.remainStock <= 0;
    const buttonText = activity.status === 0 ? '未开始'
      : activity.status === 1 ? (activity.remainStock > 0 ? '立即抢购' : '已售罄')
      : '已结束';

    return `
      <div class="activity-card">
        <div class="activity-image">🎁</div>
        <div class="activity-info">
          <div class="activity-name">${activity.activityName}</div>
          <div class="activity-goods">${activity.goodsName}</div>
          <div class="price-row">
            <span class="seckill-price">¥${activity.seckillPrice}</span>
            <span class="original-price">¥${activity.originalPrice}</span>
          </div>
          <div class="stock-row">
            <div class="stock-info">
              <span>已售 ${activity.totalStock - activity.remainStock}</span>
              <span>库存 ${activity.remainStock}</span>
            </div>
            <div class="stock-bar">
              <div class="stock-progress" style="width: ${stockPercent}%"></div>
            </div>
          </div>
          <div class="time-row">
            <span class="countdown" id="countdown-${activity.id}"></span>
            <span class="status-tag ${statusClass}">${statusText}</span>
          </div>
          <div class="action-row">
            <button class="btn-seckill"
                    onclick="handleSeckill(${activity.id})"
                    ${buttonDisabled ? 'disabled' : ''}>
              ${buttonText}
            </button>
          </div>
        </div>
      </div>
    `;
  }).join('');

  // 启动倒计时
  activities.forEach(activity => {
    if (activity.status === 0) {
      const countdownEl = document.getElementById(`countdown-${activity.id}`);
      if (countdownEl) {
        countdownTimers[activity.id] = startCountdown(
          activity.startTime,
          countdownEl,
          () => {
            // 倒计时结束，刷新活动状态
            loadActivities();
          }
        );
      }
    }
  });
}

// 处理秒杀
async function handleSeckill(activityId) {
  // 检查登录状态
  const user = getUserInfo();
  if (!user) {
    showLoginDialog();
    return;
  }

  // 显示秒杀结果弹窗
  showSeckillDialog('排队中...', '<div class="result-icon">⏳</div><div class="result-message result-waiting">正在处理中，请稍候...</div>');

  try {
    // 发起秒杀请求
    const response = await api.placeOrder(activityId, null, null);
    const orderNo = response.data.orderNo;

    // 轮询订单状态
    pollOrderStatus(orderNo);
  } catch (error) {
    showSeckillDialog('秒杀失败', `
      <div class="result-icon">❌</div>
      <div class="result-message result-fail">${error.message}</div>
    `);
  }
}

// 轮询订单状态
function pollOrderStatus(orderNo) {
  let pollCount = 0;
  const maxPolls = 30; // 最多轮询30次，共60秒

  const timer = setInterval(async () => {
    pollCount++;

    try {
      const response = await api.getOrderStatus(orderNo);
      const order = response.data;

      if (order.status === 'UNPAID') {
        clearInterval(timer);
        showSeckillDialog('抢购成功！', `
          <div class="result-icon">✅</div>
          <div class="result-message result-success">恭喜你，抢购成功！</div>
          <div class="result-detail">订单号: ${order.orderNo}</div>
          <div class="result-detail">商品: ${order.goodsName}</div>
          <div class="result-detail">秒杀价: ¥${order.seckillPrice}</div>
          <div class="result-detail">请在15分钟内完成支付</div>
        `);
        loadActivities(); // 刷新活动列表
      } else if (order.status === 'SUCCESS') {
        clearInterval(timer);
        showSeckillDialog('已完成', `
          <div class="result-icon">✅</div>
          <div class="result-message result-success">订单已完成</div>
          <div class="result-detail">订单号: ${order.orderNo}</div>
        `);
      } else if (order.status === 'FAILED' || order.status === 'TIMEOUT') {
        clearInterval(timer);
        showSeckillDialog('抢购失败', `
          <div class="result-icon">❌</div>
          <div class="result-message result-fail">${order.status === 'FAILED' ? '库存不足或已被抢购' : '订单超时未支付'}</div>
        `);
        loadActivities(); // 刷新活动列表
      } else if (pollCount >= maxPolls) {
        clearInterval(timer);
        showSeckillDialog('查询超时', `
          <div class="result-icon">⏰</div>
          <div class="result-message result-waiting">查询超时，请稍后在订单中心查看</div>
          <div class="result-detail">订单号: ${orderNo}</div>
        `);
      }
      // PENDING 状态继续轮询
    } catch (error) {
      if (pollCount >= maxPolls) {
        clearInterval(timer);
        showSeckillDialog('查询失败', `
          <div class="result-icon">❌</div>
          <div class="result-message result-fail">查询订单状态失败: ${error.message}</div>
        `);
      }
    }
  }, 2000);
}

// 显示登录弹窗
function showLoginDialog() {
  document.getElementById('loginModal').classList.add('active');
}

// 关闭登录弹窗
function closeLoginDialog() {
  document.getElementById('loginModal').classList.remove('active');
}

// 显示注册弹窗
function showRegisterDialog() {
  closeLoginDialog();
  document.getElementById('registerModal').classList.add('active');
}

// 关闭注册弹窗
function closeRegisterDialog() {
  document.getElementById('registerModal').classList.remove('active');
}

// 显示秒杀结果弹窗
function showSeckillDialog(title, content) {
  document.getElementById('seckillTitle').textContent = title;
  document.getElementById('seckillResult').innerHTML = content;
  document.getElementById('seckillModal').classList.add('active');
}

// 关闭秒杀结果弹窗
function closeSeckillDialog() {
  document.getElementById('seckillModal').classList.remove('active');
}

// 登录
async function login() {
  const username = document.getElementById('username').value.trim();
  const password = document.getElementById('password').value.trim();

  if (!username || !password) {
    showToast('请输入用户名和密码');
    return;
  }

  try {
    const response = await api.login(username, password);
    setToken(response.data.token);
    setUserInfo(username);
    closeLoginDialog();
    checkLoginStatus();
    showToast('登录成功');
  } catch (error) {
    showToast(error.message);
  }
}

// 注册
async function register() {
  const username = document.getElementById('regUsername').value.trim();
  const password = document.getElementById('regPassword').value.trim();
  const passwordConfirm = document.getElementById('regPasswordConfirm').value.trim();

  if (!username || !password || !passwordConfirm) {
    showToast('请填写完整信息');
    return;
  }

  if (password !== passwordConfirm) {
    showToast('两次密码输入不一致');
    return;
  }

  try {
    await api.register(username, password);
    showToast('注册成功，请登录');
    closeRegisterDialog();
    showLoginDialog();
  } catch (error) {
    showToast(error.message);
  }
}

// 退出登录
function logout() {
  clearToken();
  clearUserInfo();
  checkLoginStatus();
  showToast('已退出登录');
}

// 显示提示
function showToast(message) {
  const toast = document.createElement('div');
  toast.className = 'toast';
  toast.textContent = message;
  document.body.appendChild(toast);

  setTimeout(() => {
    document.body.removeChild(toast);
  }, 3000);
}
