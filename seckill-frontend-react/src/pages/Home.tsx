import { useEffect } from 'react';
import { Card, Typography, Spin, Empty, Tag } from '@douyinfe/semi-ui';
import { useNavigate } from 'react-router-dom';
import { useSeckillStore } from '../stores/useSeckillStore';
import Countdown from '../components/Countdown';
import StockProgress from '../components/StockProgress';
import SeckillButton from '../components/SeckillButton';
import { formatPrice, getStatusText, getStatusTagType } from '../utils/format';

const { Title, Text } = Typography;

export default function Home() {
  const navigate = useNavigate();
  const { activities, loading, error, fetchActivities } = useSeckillStore();

  useEffect(() => {
    fetchActivities();
  }, [fetchActivities]);

  const handleSeckill = (id: number) => {
    navigate(`/activity/${id}`);
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center h-64">
        <Spin size="large" />
      </div>
    );
  }

  if (error) {
    return <div className="text-center text-red-500 py-8">{error}</div>;
  }

  if (activities.length === 0) {
    return <Empty description="暂无秒杀活动" />;
  }

  return (
    <div className="space-y-6">
      <Title heading={3}>限时秒杀</Title>
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {activities.map((activity) => (
          <div
            key={activity.id}
            className="hover:shadow-lg transition-shadow cursor-pointer"
            onClick={() => navigate(`/activity/${activity.id}`)}
          >
            <Card>
              <div className="space-y-4">
                <div className="flex justify-between items-start">
                  <Title heading={5} className="!mb-0">
                    {activity.activityName}
                  </Title>
                  <Tag color={getStatusTagType(activity.status)}>
                    {getStatusText(activity.status)}
                  </Tag>
                </div>
                <Text type="secondary">{activity.goodsName}</Text>
                <div className="flex items-baseline gap-2">
                  <span className="text-2xl font-bold text-red-500">
                    {formatPrice(activity.seckillPrice)}
                  </span>
                  <span className="text-sm text-gray-400 line-through">
                    {formatPrice(activity.originalPrice)}
                  </span>
                </div>
                <StockProgress
                  totalStock={activity.totalStock}
                  remainStock={activity.remainStock}
                />
                {activity.status === 0 && (
                  <Countdown targetTime={activity.startTime} />
                )}
                <SeckillButton activity={activity} onSeckill={handleSeckill} />
              </div>
            </Card>
          </div>
        ))}
      </div>
    </div>
  );
}
