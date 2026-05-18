import { useEffect } from 'react';
import { Card, Typography, Tag } from '@douyinfe/semi-ui';
import { useNavigate } from 'react-router-dom';
import { useSeckillStore } from '../stores/useSeckillStore';
import Countdown from '../components/Countdown';
import StockProgress from '../components/StockProgress';
import SeckillButton from '../components/SeckillButton';
import { formatPrice, getStatusText, getStatusTagType } from '../utils/format';

const { Text } = Typography;

function SkeletonCard() {
  return (
    <Card
      style={{ overflow: 'visible', background: 'var(--color-surface)', border: '1px solid var(--color-border)', borderRadius: '12px' }}
      bodyStyle={{ padding: '28px', display: 'flex', flexDirection: 'column', gap: '20px' }}
    >
      <div style={{ display: 'flex', justifyContent: 'space-between' }}>
        <div style={{ flex: 1 }}>
          <div className="skeleton-line" style={{ width: '60%', height: '22px', marginBottom: '8px' }} />
          <div className="skeleton-line" style={{ width: '40%', height: '14px' }} />
        </div>
        <div className="skeleton-line" style={{ width: '52px', height: '24px', borderRadius: '4px' }} />
      </div>
      <div style={{ display: 'flex', alignItems: 'baseline', gap: '16px', padding: '20px 0', borderTop: '1px solid var(--color-border)', borderBottom: '1px solid var(--color-border)' }}>
        <div className="skeleton-line" style={{ width: '120px', height: '36px' }} />
        <div className="skeleton-line" style={{ width: '60px', height: '15px' }} />
      </div>
      <div className="skeleton-line" style={{ width: '100%', height: '8px', borderRadius: '4px' }} />
      <div className="skeleton-line" style={{ width: '100%', height: '44px', borderRadius: '8px' }} />
    </Card>
  );
}

export default function Activities() {
  const navigate = useNavigate();
  const { activities, loading, error, fetchActivities } = useSeckillStore();

  useEffect(() => {
    fetchActivities();
  }, [fetchActivities]);

  return (
    <div className="section-shell">
      <div className="section-shell-header">
        <div>
          <div className="section-label">精选活动</div>
          <h2 className="section-shell-title">本场秒杀列表</h2>
        </div>
        <div className="section-shell-note">点击卡片查看详情、倒计时、库存进度与下单状态轮询</div>
      </div>
      <div className="activity-showcase-grid">
        <div className="activity-card-grid">
          {loading ? (
            <>
              <SkeletonCard />
              <SkeletonCard />
              <SkeletonCard />
            </>
          ) : error ? (
            <div style={{ gridColumn: '1 / -1', color: 'var(--color-accent)', padding: '48px 0', fontSize: '14px', textAlign: 'center' }}>
              {error}
            </div>
          ) : activities.length === 0 ? (
            <div style={{ gridColumn: '1 / -1' }} className="empty-container">
              <svg className="empty-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1">
                <path d="M3 3h18v18H3V3z" strokeLinecap="round" strokeLinejoin="round" />
                <path d="M3 9h18M9 21V9" strokeLinecap="round" />
              </svg>
              <div className="empty-title">暂无秒杀活动</div>
              <div className="empty-desc">请前往管理后台创建秒杀活动</div>
            </div>
          ) : (
            activities.map((activity) => (
              <div
                key={activity.id}
                style={{ cursor: 'pointer' }}
                onClick={() => navigate(`/activity/${activity.id}`)}
              >
                <Card
                  className="card-glow"
                  style={{ overflow: 'visible' }}
                  bodyStyle={{
                    padding: '28px',
                    display: 'flex',
                    flexDirection: 'column',
                    gap: '20px',
                  }}
                >
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                    <div style={{ flex: 1 }}>
                      <div
                        style={{
                          fontFamily: 'var(--font-display)',
                          fontWeight: 700,
                          fontSize: '20px',
                          color: 'var(--color-text)',
                          lineHeight: 1.3,
                          letterSpacing: '-0.01em',
                          marginBottom: '6px',
                        }}
                      >
                        {activity.activityName}
                      </div>
                      <Text type="secondary" style={{ fontSize: '14px', color: 'var(--color-text-secondary)' }}>
                        {activity.goodsName}
                      </Text>
                    </div>
                    <Tag color={getStatusTagType(activity.status)} style={{ flexShrink: 0, marginLeft: '12px', marginTop: '2px' }}>
                      {getStatusText(activity.status)}
                    </Tag>
                  </div>

                  <div
                    style={{
                      display: 'flex',
                      alignItems: 'baseline',
                      gap: '16px',
                      padding: '20px 0',
                      borderTop: '1px solid var(--color-border)',
                      borderBottom: '1px solid var(--color-border)',
                    }}
                  >
                    <span className="price-tag" style={{ fontSize: '36px' }}>
                      {formatPrice(activity.seckillPrice)}
                    </span>
                    <span
                      style={{
                        fontFamily: 'var(--font-mono)',
                        fontSize: '15px',
                        color: 'var(--color-text-muted)',
                        textDecoration: 'line-through',
                      }}
                    >
                      {formatPrice(activity.originalPrice)}
                    </span>
                    <span
                      style={{
                        marginLeft: 'auto',
                        fontSize: '12px',
                        fontFamily: 'var(--font-mono)',
                        color: 'var(--color-accent)',
                        background: 'rgba(255,77,0,0.08)',
                        padding: '4px 10px',
                        borderRadius: '4px',
                        border: '1px solid rgba(255,77,0,0.2)',
                      }}
                    >
                      节省 {formatPrice(activity.originalPrice - activity.seckillPrice)}
                    </span>
                  </div>

                  <div>
                    <StockProgress totalStock={activity.totalStock} remainStock={activity.remainStock} />
                  </div>

                  {activity.status === 0 && (
                    <div
                      style={{
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        gap: '8px',
                        padding: '14px',
                        background: 'var(--color-surface-2)',
                        borderRadius: '10px',
                        border: '1px solid var(--color-border)',
                      }}
                    >
                      <span style={{ fontSize: '12px', color: 'var(--color-text-muted)', letterSpacing: '0.05em' }}>距开始</span>
                      <Countdown targetTime={activity.startTime} />
                    </div>
                  )}

                  <div onClick={(e) => e.stopPropagation()}>
                    <SeckillButton activity={activity} onSeckill={() => navigate(`/activity/${activity.id}`)} />
                  </div>
                </Card>
              </div>
            ))
          )}
        </div>
        <div className="activity-side-panel">
          <div className="activity-side-card">
            <div className="section-label">系统说明</div>
            <h3 className="activity-side-title">这组活动用于展示真实秒杀链路</h3>
            <p className="activity-side-copy">活动详情页会展示库存进度、倒计时、下单结果和订单状态轮询，适合在面试时完整演示一次请求如何进入系统并得到最终结果。</p>
          </div>
          <div className="activity-side-card">
            <div className="section-label">状态流转</div>
            <div className="activity-state-list">
              <div className="activity-state-item"><span className="activity-state-dot pending" />PENDING: 请求受理，排队处理中</div>
              <div className="activity-state-item"><span className="activity-state-dot success" />UNPAID: Lua 扣库存成功，订单待支付</div>
              <div className="activity-state-item"><span className="activity-state-dot fail" />FAILED: 扣库存失败或消息处理异常</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
