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
      <div className="flex justify-center items-center" style={{ height: '60vh' }}>
        <Spin size="large" />
      </div>
    );
  }

  if (error) {
    return (
      <div className="text-center" style={{ color: 'var(--color-danger)', padding: '48px 0' }}>
        {error}
      </div>
    );
  }

  if (activities.length === 0) {
    return (
      <div className="text-center" style={{ padding: '80px 0' }}>
        <Empty description={<span style={{ color: 'var(--color-text-muted)' }}>暂无秒杀活动</span>} />
      </div>
    );
  }

  return (
    <div className="space-y-8">
      {/* Hero Header */}
      <div className="text-center stagger-children" style={{ padding: '32px 0 48px' }}>
        <div
          className="inline-block"
          style={{
            fontSize: '11px',
            fontWeight: 600,
            letterSpacing: '0.2em',
            textTransform: 'uppercase',
            color: 'var(--color-accent)',
            marginBottom: '12px',
          }}
        >
          Flash Sales
        </div>
        <Title
          heading={2}
          style={{
            color: 'var(--color-text)',
            fontWeight: 800,
            fontSize: 'clamp(28px, 5vw, 42px)',
            margin: 0,
            letterSpacing: '-0.02em',
          }}
        >
          限时秒杀
        </Title>
        <Text
          style={{
            color: 'var(--color-text-secondary)',
            fontSize: '16px',
            marginTop: '8px',
            display: 'block',
          }}
        >
          热门商品，限时抢购
        </Text>
      </div>

      {/* Activity Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 stagger-children">
        {activities.map((activity) => (
          <div
            key={activity.id}
            className="animate-fade-in-up"
            style={{ cursor: 'pointer' }}
            onClick={() => navigate(`/activity/${activity.id}`)}
          >
            <Card
              style={{
                height: '100%',
                display: 'flex',
                flexDirection: 'column',
              }}
              bodyStyle={{ flex: 1, display: 'flex', flexDirection: 'column', gap: '16px' }}
            >
              {/* Status badge + Activity name */}
              <div className="flex justify-between items-start gap-2">
                <Title
                  heading={5}
                  style={{
                    color: 'var(--color-text)',
                    fontWeight: 700,
                    margin: 0,
                    fontSize: '17px',
                    lineHeight: 1.3,
                  }}
                >
                  {activity.activityName}
                </Title>
                <Tag
                  color={getStatusTagType(activity.status)}
                  style={{ flexShrink: 0 }}
                >
                  {getStatusText(activity.status)}
                </Tag>
              </div>

              {/* Goods name */}
              <Text
                type="secondary"
                style={{
                  fontSize: '14px',
                  color: 'var(--color-text-secondary)',
                }}
              >
                {activity.goodsName}
              </Text>

              {/* Price block */}
              <div className="flex items-baseline gap-3" style={{ marginTop: 'auto' }}>
                <span
                  style={{
                    fontSize: '28px',
                    fontWeight: 800,
                    color: 'var(--color-accent)',
                    fontFamily: 'JetBrains Mono, monospace',
                    letterSpacing: '-0.02em',
                  }}
                >
                  {formatPrice(activity.seckillPrice)}
                </span>
                <span
                  style={{
                    fontSize: '14px',
                    color: 'var(--color-text-muted)',
                    textDecoration: 'line-through',
                    fontFamily: 'JetBrains Mono, monospace',
                  }}
                >
                  {formatPrice(activity.originalPrice)}
                </span>
              </div>

              {/* Stock Progress */}
              <div>
                <StockProgress
                  totalStock={activity.totalStock}
                  remainStock={activity.remainStock}
                />
              </div>

              {/* Countdown */}
              {activity.status === 0 && (
                <div className="text-center" style={{ padding: '8px 0' }}>
                  <Countdown targetTime={activity.startTime} />
                </div>
              )}

              {/* Action */}
              <div onClick={(e) => e.stopPropagation()}>
                <SeckillButton activity={activity} onSeckill={handleSeckill} />
              </div>
            </Card>
          </div>
        ))}
      </div>
    </div>
  );
}