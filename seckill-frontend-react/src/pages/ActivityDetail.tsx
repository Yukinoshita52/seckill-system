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
      setActivity(response.data);
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
      <div className="flex justify-center items-center" style={{ height: '60vh' }}>
        <Spin size="large" />
      </div>
    );
  }

  if (!activity) {
    return (
      <div className="text-center" style={{ padding: '80px 0', color: 'var(--color-danger)' }}>
        活动不存在
      </div>
    );
  }

  return (
    <div className="max-w-2xl mx-auto animate-fade-in-up">
      {/* Hero Card */}
      <Card
        style={{
          background: 'var(--color-surface)',
          border: '1px solid var(--color-border)',
          borderRadius: '16px',
          overflow: 'hidden',
        }}
      >
        {/* Gradient accent bar */}
        <div
          style={{
            height: '4px',
            background: 'linear-gradient(90deg, var(--color-accent) 0%, #ea580c 50%, var(--color-accent) 100%)',
            backgroundSize: '200% 100%',
            animation: 'gradient-shift 3s ease infinite',
          }}
        />

        <div style={{ padding: '32px 32px 28px' }}>
          {/* Header */}
          <div className="flex justify-between items-start gap-4" style={{ marginBottom: '20px' }}>
            <div style={{ flex: 1 }}>
              <div
                style={{
                  fontSize: '11px',
                  fontWeight: 600,
                  letterSpacing: '0.15em',
                  textTransform: 'uppercase',
                  color: 'var(--color-accent)',
                  marginBottom: '8px',
                }}
              >
                秒杀活动
              </div>
              <Title
                heading={3}
                style={{
                  color: 'var(--color-text)',
                  fontWeight: 800,
                  margin: 0,
                  fontSize: '22px',
                  letterSpacing: '-0.01em',
                  lineHeight: 1.3,
                }}
              >
                {activity.activityName}
              </Title>
            </div>
            <Tag color={getStatusTagType(activity.status)} style={{ flexShrink: 0, marginTop: '4px' }}>
              {getStatusText(activity.status)}
            </Tag>
          </div>

          {/* Goods name */}
          <Text
            type="secondary"
            style={{
              fontSize: '15px',
              color: 'var(--color-text-secondary)',
              display: 'block',
              marginBottom: '24px',
            }}
          >
            {activity.goodsName}
          </Text>

          {/* Price */}
          <div
            className="flex items-baseline gap-4"
            style={{
              padding: '20px 0',
              borderTop: '1px solid var(--color-border)',
              borderBottom: '1px solid var(--color-border)',
              marginBottom: '24px',
            }}
          >
            <span
              style={{
                fontSize: '40px',
                fontWeight: 900,
                color: 'var(--color-accent)',
                fontFamily: 'JetBrains Mono, monospace',
                letterSpacing: '-0.03em',
                lineHeight: 1,
              }}
            >
              {formatPrice(activity.seckillPrice)}
            </span>
            <span
              style={{
                fontSize: '16px',
                color: 'var(--color-text-muted)',
                textDecoration: 'line-through',
                fontFamily: 'JetBrains Mono, monospace',
              }}
            >
              {formatPrice(activity.originalPrice)}
            </span>
            <span
              style={{
                fontSize: '12px',
                color: 'var(--color-text-muted)',
                marginLeft: 'auto',
              }}
            >
              节省 {formatPrice(activity.originalPrice - activity.seckillPrice)}
            </span>
          </div>

          {/* Stock */}
          <div style={{ marginBottom: '20px' }}>
            <StockProgress
              totalStock={activity.totalStock}
              remainStock={activity.remainStock}
            />
          </div>

          {/* Countdown */}
          {activity.status === 0 && (
            <div
              style={{
                textAlign: 'center',
                padding: '20px 0',
                marginBottom: '20px',
                background: 'var(--color-surface-2)',
                borderRadius: '10px',
                border: '1px solid var(--color-border)',
              }}
            >
              <div
                style={{
                  fontSize: '11px',
                  fontWeight: 600,
                  letterSpacing: '0.1em',
                  textTransform: 'uppercase',
                  color: 'var(--color-text-muted)',
                  marginBottom: '8px',
                }}
              >
                距开始
              </div>
              <Countdown targetTime={activity.startTime} onComplete={() => fetchActivity(true)} />
            </div>
          )}

          {/* CTA Button */}
          <Button
            type="primary"
            theme="solid"
            size="large"
            disabled={activity.status !== 1 || activity.remainStock <= 0}
            loading={seckillLoading}
            onClick={handleSeckill}
            className="w-full animate-pulse-glow"
            style={{
              height: '52px',
              fontSize: '16px',
              fontWeight: 700,
              letterSpacing: '0.02em',
            }}
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