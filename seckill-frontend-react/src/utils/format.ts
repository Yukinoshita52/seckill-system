export function formatPrice(price: number): string {
  return `¥${price.toFixed(2)}`;
}

export function padZero(num: number): string {
  return num.toString().padStart(2, '0');
}

export function formatCountdown(hours: number, minutes: number, seconds: number): string {
  return `${padZero(hours)}:${padZero(minutes)}:${padZero(seconds)}`;
}

export function getStatusText(status: number): string {
  switch (status) {
    case 0:
      return '未开始';
    case 1:
      return '进行中';
    case 2:
      return '已结束';
    default:
      return '未知';
  }
}

export function getStatusTagType(status: number): 'orange' | 'green' | 'red' {
  switch (status) {
    case 0:
      return 'orange';
    case 1:
      return 'green';
    case 2:
      return 'red';
    default:
      return 'orange';
  }
}
