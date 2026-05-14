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
      <div className="flex justify-center items-center h-64">
        <Spin size="large" />
      </div>
    );
  }

  if (!activity) {
    return <div>活动不存在</div>;
  }

  return (
    <div className="max-w-2xl mx-auto">
      <Card>
        <div className="space-y-6">
          <div className="flex justify-between items-start">
            <Title heading={3}>{activity.activityName}</Title>
            <Tag color={getStatusTagType(activity.status)}>
              {getStatusText(activity.status)}
            </Tag>
          </div>

          <Text type="secondary" className="text-lg">
            {activity.goodsName}
          </Text>

          <div className="flex items-baseline gap-4">
            <span className="text-4xl font-bold text-red-500">
              {formatPrice(activity.seckillPrice)}
            </span>
            <span className="text-lg text-gray-400 line-through">
              {formatPrice(activity.originalPrice)}
            </span>
          </div>

          <StockProgress
            totalStock={activity.totalStock}
            remainStock={activity.remainStock}
          />

          {activity.status === 0 && (
            <div className="text-center">
              <Countdown targetTime={activity.startTime} onComplete={() => fetchActivity(true)} />
            </div>
          )}

          <Button
            type="primary"
            theme="solid"
            size="large"
            disabled={activity.status !== 1 || activity.remainStock <= 0}
            loading={seckillLoading}
            onClick={handleSeckill}
            className="w-full"
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
