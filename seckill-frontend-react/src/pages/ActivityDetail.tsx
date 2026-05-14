import { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Card, Typography, Button, Spin, Tag } from '@douyinfe/semi-ui';
import { activityApi } from '../api/activity';
import { orderApi } from '../api/order';
import { useAuthStore } from '../stores/useAuthStore';
import Countdown from '../components/Countdown';
import StockProgress from '../components/StockProgress';
import SeckillResult from '../components/SeckillResult';
import { formatPrice, getStatusText, getStatusTagType } from '../utils/format';
import { Activity } from '../types/activity';
import { usePolling } from '../hooks/usePolling';
import { OrderStatusResponse } from '../types/order';

const { Title, Text } = Typography;

export default function ActivityDetail() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { isAuthenticated } = useAuthStore();
  const [activity, setActivity] = useState<Activity | null>(null);
  const [loading, setLoading] = useState(true);
  const [seckillLoading, setSeckillLoading] = useState(false);
  const [resultVisible, setResultVisible] = useState(false);
  const [result, setResult] = useState<{
    status: 'waiting' | 'success' | 'fail';
    title: string;
    message: string;
    orderNo?: string;
    goodsName?: string;
    seckillPrice?: number;
  } | null>(null);
  const orderNoRef = useRef<string>('');

  const { startPolling } = usePolling<OrderStatusResponse>({
    fetchFn: () => orderApi.getOrderStatus(orderNoRef.current).then((res) => res.data),
    interval: 2000,
    maxAttempts: 30,
    condition: (data) => data.status !== 'PENDING',
    onSuccess: (data) => {
      if (data.status === 'UNPAID') {
        setResult({
          status: 'success',
          title: '抢购成功！',
          message: '恭喜你，抢购成功！',
          orderNo: data.orderNo,
          goodsName: data.goodsName,
          seckillPrice: data.seckillPrice,
        });
        fetchActivity(true);
      } else if (data.status === 'FAILED' || data.status === 'TIMEOUT') {
        setResult({
          status: 'fail',
          title: '抢购失败',
          message: data.status === 'FAILED' ? '库存不足或已被抢购' : '订单超时未支付',
        });
        fetchActivity(true);
      }
    },
    onTimeout: () => {
      setResult({
        status: 'fail',
        title: '查询超时',
        message: '查询超时，请稍后在订单中心查看',
        orderNo: orderNoRef.current,
      });
    },
  });

  const fetchActivity = async (silent = false) => {
    try {
      if (!silent) setLoading(true);
      const response = await activityApi.getActivity(Number(id));
      setActivity(response.data ?? null);
    } catch (error) {
      console.error('Failed to fetch activity:', error);
    } finally {
      if (!silent) setLoading(false);
    }
  };

  useEffect(() => {
    fetchActivity();
  }, [id]);

  const handleSeckill = async () => {
    if (!isAuthenticated) {
      navigate('/login');
      return;
    }

    try {
      setSeckillLoading(true);
      const response = await orderApi.placeOrder({ activityId: Number(id) });
      const orderNo = response.data.orderNo;
      orderNoRef.current = orderNo;

      setResult({
        status: 'waiting',
        title: '排队中...',
        message: '正在处理中，请稍候...',
        orderNo,
      });
      setResultVisible(true);

      startPolling();
    } catch (error) {
      setResult({
        status: 'fail',
        title: '秒杀失败',
        message: (error as Error).message,
      });
      setResultVisible(true);
    } finally {
      setSeckillLoading(false);
    }
  };

  if (loading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '60vh' }}>
        <Spin size="large" />
      </div>
    );
  }

  if (!activity) {
    return (
      <div className="empty-container">
        <div className="empty-title">活动不存在</div>
      </div>
    );
  }

  const discount = Math.round((1 - activity.seckillPrice / activity.originalPrice) * 100);

  return (
    <div className="animate-fade-in-up" style={{ maxWidth: '680px', margin: '0 auto' }}>
      {/* Hero Card */}
      <Card
        style={{
          background: 'var(--color-surface)',
          border: '1px solid var(--color-border)',
          borderRadius: '24px',
          overflow: 'hidden',
        }}
      >
        {/* Top accent bar */}
        <div
          style={{
            height: '4px',
            background: 'linear-gradient(90deg, var(--color-accent), #ff8c5a, var(--color-accent))',
            backgroundSize: '200% 100%',
            animation: 'gradient-shift 3s ease infinite',
          }}
        />

        <div style={{ padding: '40px 40px 36px' }}>
          {/* Header */}
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '16px' }}>
            <div>
              <div className="section-label" style={{ marginBottom: '10px' }}>秒杀活动</div>
              <Title
                heading={3}
                style={{
                  fontFamily: 'var(--font-display)',
                  fontWeight: 800,
                  fontSize: '28px',
                  color: 'var(--color-text)',
                  letterSpacing: '-0.02em',
                  lineHeight: 1.2,
                  margin: 0,
                }}
              >
                {activity.activityName}
              </Title>
            </div>
            <Tag color={getStatusTagType(activity.status)} style={{ fontSize: '11px', padding: '4px 12px' }}>
              {getStatusText(activity.status)}
            </Tag>
          </div>

          {/* Goods */}
          <Text type="secondary" style={{ fontSize: '16px', color: 'var(--color-text-secondary)', display: 'block', marginBottom: '32px' }}>
            {activity.goodsName}
          </Text>

          {/* Price section */}
          <div
            style={{
              display: 'flex',
              alignItems: 'flex-end',
              gap: '16px',
              padding: '28px 0',
              marginBottom: '28px',
              borderTop: '1px solid var(--color-border)',
              borderBottom: '1px solid var(--color-border)',
            }}
          >
            <span className="price-tag" style={{ fontSize: '56px', lineHeight: 1 }}>
              {formatPrice(activity.seckillPrice)}
            </span>
            <span style={{ fontFamily: 'var(--font-mono)', fontSize: '18px', color: 'var(--color-text-muted)', textDecoration: 'line-through', paddingBottom: '8px' }}>
              {formatPrice(activity.originalPrice)}
            </span>
            <span
              style={{
                marginLeft: 'auto',
                fontFamily: 'var(--font-mono)',
                fontWeight: 600,
                fontSize: '14px',
                color: '#fff',
                background: 'linear-gradient(135deg, var(--color-accent) 0%, #e64500 100%)',
                padding: '6px 14px',
                borderRadius: '8px',
                boxShadow: '0 4px 16px rgba(255, 77, 0, 0.3)',
                paddingBottom: '6px',
              }}
            >
              -{discount}%
            </span>
          </div>

          {/* Stock */}
          <div style={{ marginBottom: '24px' }}>
            <StockProgress totalStock={activity.totalStock} remainStock={activity.remainStock} />
          </div>

          {/* Countdown */}
          {activity.status === 0 && (
            <div
              style={{
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                gap: '12px',
                padding: '28px',
                marginBottom: '24px',
                background: 'var(--color-surface-2)',
                borderRadius: '16px',
                border: '1px solid var(--color-border)',
              }}
            >
              <div className="section-label" style={{ marginBottom: '0' }}>距开始</div>
              <Countdown targetTime={activity.startTime} onComplete={() => fetchActivity(true)} />
            </div>
          )}

          {/* CTA */}
          <Button
            type="primary"
            theme="solid"
            size="large"
            disabled={activity.status !== 1 || activity.remainStock <= 0}
            loading={seckillLoading}
            onClick={handleSeckill}
            className="cta-button"
            style={{ width: '100%', height: '56px !important', fontSize: '17px !important' }}
          >
            {activity.status === 0
              ? '未开始'
              : activity.status === 1
              ? activity.remainStock > 0
                ? '立即抢购'
                : '已售罄'
              : '已结束'}
          </Button>
        </div>
      </Card>

      {result && (
        <SeckillResult
          visible={resultVisible}
          onClose={() => setResultVisible(false)}
          result={result}
        />
      )}
    </div>
  );
}
