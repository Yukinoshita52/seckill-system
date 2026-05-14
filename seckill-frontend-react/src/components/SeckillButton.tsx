import { Button } from '@douyinfe/semi-ui';
import { Activity } from '../types/activity';

interface SeckillButtonProps {
  activity: Activity;
  onSeckill: (id: number) => void;
  loading?: boolean;
}

export default function SeckillButton({ activity, onSeckill, loading }: SeckillButtonProps) {
  const isDisabled = activity.status !== 1 || activity.remainStock <= 0;

  const getButtonText = () => {
    if (activity.status === 0) return '未开始';
    if (activity.status === 1) {
      return activity.remainStock > 0 ? '立即抢购' : '已售罄';
    }
    return '已结束';
  };

  return (
    <Button
      type="primary"
      theme="solid"
      disabled={isDisabled}
      loading={loading}
      onClick={(e) => { e.stopPropagation(); onSeckill(activity.id); }}
      className="w-full"
    >
      {getButtonText()}
    </Button>
  );
}
