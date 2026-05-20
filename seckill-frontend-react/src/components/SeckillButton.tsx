import { useState, useEffect, useRef } from 'react';
import { Button, Toast } from '@douyinfe/semi-ui';
import { Activity } from '../types/activity';
import { orderApi } from '../api/order';

interface SeckillButtonProps {
  activity: Activity;
}

type OrderStatus = 'PENDING' | 'UNPAID' | 'FAILED' | 'TIMEOUT';

export default function SeckillButton({ activity }: SeckillButtonProps) {
  const [loading, setLoading] = useState(false);
  const [orderNo, setOrderNo] = useState<string | null>(null);
  const [orderStatus, setOrderStatus] = useState<OrderStatus | null>(null);
  const [pollingInterval, setPollingInterval] = useState(500);
  const pollingTimerRef = useRef<ReturnType<typeof setInterval> | null>(null);

  const isDisabled = activity.status !== 1 || activity.remainStock <= 0;

  // 清理轮询
  const clearPolling = () => {
    if (pollingTimerRef.current) {
      clearInterval(pollingTimerRef.current);
      pollingTimerRef.current = null;
    }
  };

  // 轮询订单状态
  useEffect(() => {
    if (!orderNo || orderStatus !== 'PENDING') return;

    pollingTimerRef.current = setInterval(async () => {
      try {
        const res = await orderApi.getOrderStatus(orderNo);
        const status = res.data.status as OrderStatus;
        setOrderStatus(status);

        if (status !== 'PENDING') {
          clearPolling();
          if (status === 'UNPAID') {
            Toast.success({ content: '抢购成功，请支付', id: 'seckill-toast' });
          } else if (status === 'FAILED') {
            Toast.error({ content: '库存不足，抢购失败', id: 'seckill-toast' });
          } else if (status === 'TIMEOUT') {
            Toast.error({ content: '订单超时', id: 'seckill-toast' });
          }
          // 重置状态，30s 后允许重新抢购
          setTimeout(() => {
            setOrderNo(null);
            setOrderStatus(null);
            setPollingInterval(500);
          }, 3000);
        } else {
          // 逐渐增加轮询间隔
          setPollingInterval((prev) => Math.min(prev * 1.5, 3000));
        }
      } catch {
        clearPolling();
        Toast.error({ content: '查询订单状态失败', id: 'seckill-toast' });
        setOrderNo(null);
        setOrderStatus(null);
      }
    }, pollingInterval);

    return clearPolling;
  }, [orderNo, orderStatus, pollingInterval]);

  // 组件卸载时清理
  useEffect(() => {
    return clearPolling;
  }, []);

  const getButtonText = () => {
    if (orderStatus === 'PENDING') return '排队中...';
    if (orderStatus === 'UNPAID') return '抢购成功';
    if (orderStatus === 'FAILED') return '库存不足';
    if (orderStatus === 'TIMEOUT') return '订单超时';

    if (activity.status === 0) return '未开始';
    if (activity.status === 1) {
      return activity.remainStock > 0 ? '立即抢购' : '已售罄';
    }
    return '已结束';
  };

  const isPolling = orderStatus === 'PENDING';
  const finalStatus = orderStatus === 'UNPAID' || orderStatus === 'FAILED' || orderStatus === 'TIMEOUT';

  const handleClick = async (e: React.MouseEvent) => {
    e.stopPropagation();
    if (isDisabled || loading || isPolling) return;

    setLoading(true);
    try {
      const res = await orderApi.placeOrder({ activityId: activity.id });
      setOrderNo(res.data.orderNo);
      setOrderStatus('PENDING');
      Toast.success({ content: res.data.message || '下单成功', id: 'seckill-toast' });
    } catch (err) {
      Toast.error({ content: (err as Error).message, id: 'seckill-toast' });
    } finally {
      setLoading(false);
    }
  };

  return (
    <Button
      type="primary"
      theme="solid"
      disabled={isDisabled || isPolling || finalStatus}
      loading={loading || isPolling}
      onClick={handleClick}
      className="w-full"
    >
      {getButtonText()}
    </Button>
  );
}