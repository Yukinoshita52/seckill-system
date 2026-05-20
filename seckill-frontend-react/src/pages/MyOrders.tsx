import { useEffect, useState } from 'react';
import { Card, Typography, Tag, Button } from '@douyinfe/semi-ui';
import { orderApi } from '../api/order';
import { OrderStatusResponse } from '../types/order';
import { formatPrice } from '../utils/format';

const { Title, Text } = Typography;

function getStatusTagType(status: string) {
  return status === 'SUCCESS' ? 'green' : status === 'UNPAID' ? 'yellow' : 'red';
}

function getStatusText(status: string) {
  const map: Record<string, string> = {
    PENDING: '待处理',
    UNPAID: '待支付',
    SUCCESS: '已支付',
    FAILED: '已失败',
    TIMEOUT: '已超时',
    UNKNOWN: '未知',
  };
  return map[status] || status;
}

export default function MyOrders() {
  const [orders, setOrders] = useState<OrderStatusResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    orderApi.listOrders()
      .then((res) => setOrders(res.data || []))
      .catch((err) => setError((err as Error).message))
      .finally(() => setLoading(false));
  }, []);

  const handlePay = async (orderNo: string) => {
    try {
      await orderApi.payOrder(orderNo);
      setOrders((prev) =>
        prev.map((o) => (o.orderNo === orderNo ? { ...o, status: 'SUCCESS' } : o))
      );
    } catch (err) {
      alert((err as Error).message);
    }
  };

  const handleCancel = async (orderNo: string) => {
    try {
      await orderApi.cancelOrder(orderNo);
      setOrders((prev) =>
        prev.map((o) => (o.orderNo === orderNo ? { ...o, status: 'FAILED' } : o))
      );
    } catch (err) {
      alert((err as Error).message);
    }
  };

  return (
    <div style={{ maxWidth: '720px', margin: '0 auto' }}>
      <Title heading={3} style={{ marginBottom: '24px' }}>我的订单</Title>

      {loading ? (
        <div style={{ color: 'var(--color-text-muted)', padding: '48px 0', textAlign: 'center' }}>加载中...</div>
      ) : error ? (
        <div style={{ color: '#f87171', padding: '16px', background: 'rgba(239,68,68,0.08)', borderRadius: '8px' }}>{error}</div>
      ) : orders.length === 0 ? (
        <Card style={{ textAlign: 'center', padding: '48px', color: 'var(--color-text-muted)' }}>
          暂无订单记录
        </Card>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
          {orders.map((order) => (
            <Card key={order.orderNo} style={{ border: '1px solid var(--color-border)', borderRadius: '12px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                <div>
                  <Text type="secondary" style={{ fontSize: '12px', fontFamily: 'var(--font-mono)' }}>{order.orderNo}</Text>
                  <Title heading={5} style={{ margin: '4px 0 8px' }}>{order.goodsName}</Title>
                  <Text style={{ fontSize: '16px', fontFamily: 'var(--font-mono)', color: 'var(--color-accent)' }}>
                    {formatPrice(order.seckillPrice)}
                  </Text>
                </div>
                <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: '8px' }}>
                  <Tag color={getStatusTagType(order.status)}>{getStatusText(order.status)}</Tag>
                  {order.status === 'UNPAID' && (
                    <div style={{ display: 'flex', gap: '8px' }}>
                      <Button type="primary" size="small" onClick={() => handlePay(order.orderNo)}>支付</Button>
                      <Button type="danger" size="small" onClick={() => handleCancel(order.orderNo)}>取消</Button>
                    </div>
                  )}
                </div>
              </div>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}