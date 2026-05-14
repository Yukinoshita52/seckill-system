// 倒计时组件
function startCountdown(targetTime, element, onComplete) {
  const timer = setInterval(() => {
    const now = new Date().getTime();
    const target = new Date(targetTime).getTime();
    const diff = target - now;

    if (diff <= 0) {
      clearInterval(timer);
      element.textContent = '已开始';
      if (onComplete) {
        onComplete();
      }
      return;
    }

    const hours = Math.floor(diff / (1000 * 60 * 60));
    const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
    const seconds = Math.floor((diff % (1000 * 60)) / 1000);

    element.textContent = `距开始 ${pad(hours)}:${pad(minutes)}:${pad(seconds)}`;
  }, 1000);

  return timer;
}

// 补零
function pad(num) {
  return num.toString().padStart(2, '0');
}

// 清除倒计时
function clearCountdown(timer) {
  if (timer) {
    clearInterval(timer);
  }
}
