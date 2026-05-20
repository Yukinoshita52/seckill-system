import { useState } from 'react';
import { Button, Toast } from '@douyinfe/semi-ui';
import { Activity } from '../types/activity';
import { orderApi } from '../api/order';

interface SeckillButtonProps {
  activity: Activity;
}

export default function SeckillButton({ activity }: SeckillButtonProps) {
  const [loading, setLoading] = useState(false);

  const isDisabled = activity.status !== 1 || activity.remainStock <= 0;

  const getButtonText = () => {
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
      const res = await orderApi.placeOrder({ activityId: activity.id });
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
      disabled={isDisabled}
      loading={loading}
      onClick={handleClick}
      className="w-full"
    >
      {getButtonText()}
    </Button>
  );
}
