import { useState } from 'react';
import { Button, Toast } from '@douyinfe/semi-ui';
import { Activity } from '../types/activity';
import { orderApi } from '../api/order';

interface SeckillButtonProps {
  activity: Activity;
}

type OrderStatus = 'UNPAID' | 'FAILED';

export default function SeckillButton({ activity }: SeckillButtonProps) {
  const [loading, setLoading] = useState(false);
  const [orderStatus, setOrderStatus] = useState<OrderStatus | null>(null);

  const isDisabled = activity.status !== 1 || activity.remainStock <= 0;

  const getButtonText = () => {
    if (orderStatus === 'UNPAID') return '抢购成功';
    if (orderStatus === 'FAILED') return '库存不足';
    if (activity.status === 0) return '未开始';
    if (activity.status === 1) {
      return activity.remainStock > 0 ? '立即抢购' : '已售罄';
    }
    return '已结束';
  };

  const handleClick = async (e: React.MouseEvent) => {
    e.stopPropagation();
    if (isDisabled || loading) return;

    setLoading(true);
    try {
      await orderApi.placeOrder({ activityId: activity.id });
      setOrderStatus('UNPAID');
      Toast.success({ content: '抢购成功！请前往支付', id: 'seckill-toast' });
    } catch (err) {
      setOrderStatus('FAILED');
      Toast.error({ content: (err as Error).message, id: 'seckill-toast' });
    } finally {
      setLoading(false);
    }
  };

  const isFinal = orderStatus === 'UNPAID' || orderStatus === 'FAILED';

  return (
    <Button
      type="primary"
      theme="solid"
      disabled={isDisabled || isFinal}
      loading={loading}
      onClick={handleClick}
      className="w-full"
    >
      {getButtonText()}
    </Button>
  );
}