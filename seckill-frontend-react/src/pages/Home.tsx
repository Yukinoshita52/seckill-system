import { useEffect, useMemo, useState } from 'react';
import { Button, Card, Typography, Spin, Tag } from '@douyinfe/semi-ui';
import { useNavigate } from 'react-router-dom';
import { useSeckillStore } from '../stores/useSeckillStore';
import Countdown from '../components/Countdown';
import StockProgress from '../components/StockProgress';
import SeckillButton from '../components/SeckillButton';
import { demoApi } from '../api/demo';
import { DemoMetrics } from '../types/demo';
import { formatPrice, getStatusText, getStatusTagType } from '../utils/format';

const { Text } = Typography;
const DEMO_REQUEST_COUNT = 100;

export default function Home() {
  const navigate = useNavigate();
  const { activities, loading, error, fetchActivities } = useSeckillStore();
  const [demoMetrics, setDemoMetrics] = useState<DemoMetrics | null>(null);
  const [demoLoading, setDemoLoading] = useState(false);
  const [demoError, setDemoError] = useState<string | null>(null);
  const [demoRunLoading, setDemoRunLoading] = useState<number | null>(null);
  const [demoRunMessage, setDemoRunMessage] = useState<string | null>(null);
  const activeCount = activities.filter((activity) => activity.status === 1).length;
  const pendingCount = activities.filter((activity) => activity.status === 0).length;
  const remainStock = activities.reduce((sum, activity) => sum + activity.remainStock, 0);
  const finishedCount = activities.filter((activity) => activity.status === 2).length;
  const demoActivity = useMemo(
    () => activities.find((activity) => activity.status === 1) ?? activities[0] ?? null,
    [activities]
  );

  useEffect(() => {
    fetchActivities();
  }, [fetchActivities]);

  useEffect(() => {
    if (!demoActivity) {
      setDemoMetrics(null);
      setDemoError(null);
      return;
    }

    let disposed = false;
    let intervalId: ReturnType<typeof setInterval> | undefined;

    const loadDemoMetrics = async (silent = false) => {
      try {
        if (!silent) {
          setDemoLoading(true);
        }
        setDemoError(null);
        const response = await demoApi.getMetrics(demoActivity.id);
        if (!disposed) {
          setDemoMetrics(response.data);
        }
      } catch (err) {
        if (!disposed) {
          setDemoError((err as Error).message);
        }
      } finally {
        if (!disposed && !silent) {
          setDemoLoading(false);
        }
      }
    };

    loadDemoMetrics();
    intervalId = setInterval(() => {
      void loadDemoMetrics(true);
    }, 5000);

    return () => {
      disposed = true;
      if (intervalId) {
        clearInterval(intervalId);
      }
    };
  }, [demoActivity]);

  const loadDemoMetricsOnce = async () => {
    if (!demoActivity) {
      return;
    }
    try {
      setDemoError(null);
      const response = await demoApi.getMetrics(demoActivity.id);
      setDemoMetrics(response.data);
    } catch (err) {
      setDemoError((err as Error).message);
    }
  };

  const handleSeckill = (id: number) => {
    navigate(`/activity/${id}`);
  };

  const handleRunDemo = async (requestCount: number) => {
    if (!demoActivity) {
      return;
    }
    try {
      setDemoRunLoading(requestCount);
      setDemoRunMessage(null);
      setDemoError(null);
      const response = await demoApi.runDemo({
        activityId: demoActivity.id,
        requestCount,
      });
      setDemoRunMessage(response.data.message);
      await loadDemoMetricsOnce();
    } catch (err) {
      setDemoError((err as Error).message);
    } finally {
      setDemoRunLoading(null);
    }
  };

  const stockConsumed = demoMetrics ? Math.max(demoMetrics.totalStock - demoMetrics.remainStock, 0) : null;

  if (loading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '60vh' }}>
        <Spin size="large" />
      </div>
    );
  }

  if (error) {
    return (
      <div className="text-center" style={{ color: 'var(--color-accent)', padding: '48px 0', fontSize: '14px' }}>
        {error}
      </div>
    );
  }

  if (activities.length === 0) {
    return (
      <div className="empty-container">
        <svg className="empty-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1">
          <path d="M3 3h18v18H3V3z" strokeLinecap="round" strokeLinejoin="round" />
          <path d="M3 9h18M9 21V9" strokeLinecap="round" />
        </svg>
        <div className="empty-title">暂无秒杀活动</div>
        <div className="empty-desc">请前往管理后台创建秒杀活动</div>
      </div>
    );
  }

  return (
    <div>
      <section className="hero-showcase stagger-children">
        <div className="hero-copy">
          <div className="section-label">Resume Project / Architecture Showcase</div>
          <h1 className="hero-title" style={{ marginBottom: '18px' }}>
            面向简历展示的
            <br />
            <span className="hero-accent-text">高并发秒杀系统</span>
          </h1>
          <p className="hero-subtitle hero-copy-subtitle">
            这个页面不是普通电商首页，而是把 Redis 分桶库存、四层过滤、RocketMQ 异步削峰和订单状态轮询直接展示给面试官看的系统演示页。
          </p>

          <div className="hero-pill-row">
            <span className="hero-pill">Redis 分桶库存</span>
            <span className="hero-pill">Lua 原子扣减</span>
            <span className="hero-pill">RocketMQ 异步削峰</span>
            <span className="hero-pill">Sentinel 限流</span>
          </div>

          <div className="hero-flow-inline">
            <div className="hero-flow-step">
              <span className="hero-flow-number">01</span>
              <div>
                <div className="hero-flow-title">四层过滤</div>
                <div className="hero-flow-copy">活动状态、重复购买、总库存预检、入口限流。</div>
              </div>
            </div>
            <div className="hero-flow-step">
              <span className="hero-flow-number">02</span>
              <div>
                <div className="hero-flow-title">PENDING 受理</div>
                <div className="hero-flow-copy">请求先返回排队中，避免同步阻塞。</div>
              </div>
            </div>
            <div className="hero-flow-step">
              <span className="hero-flow-number">03</span>
              <div>
                <div className="hero-flow-title">MQ 异步扣库存</div>
                <div className="hero-flow-copy">消费者执行 Lua 扣减并推进订单状态。</div>
              </div>
            </div>
          </div>

          <div className="hero-cta-row">
            <Button
              type="primary"
              theme="solid"
              className="hero-primary-button"
              onClick={() => document.getElementById('activity-grid')?.scrollIntoView({ behavior: 'smooth' })}
            >
              查看活动列表
            </Button>
            <Button type="secondary" className="hero-secondary-button" onClick={() => navigate('/login')}>
              登录体验完整流程
            </Button>
          </div>

          <div className="hero-stats hero-stats-left">
            <div className="hero-stat-card hero-stat-card-compact">
              <span className="hero-stat-value">{activities.length}</span>
              <span className="hero-stat-label">活动总数</span>
            </div>
            <div className="hero-stat-card hero-stat-card-compact">
              <span className="hero-stat-value">{activeCount}</span>
              <span className="hero-stat-label">进行中活动</span>
            </div>
            <div className="hero-stat-card hero-stat-card-compact">
              <span className="hero-stat-value">{remainStock}</span>
              <span className="hero-stat-label">剩余库存</span>
            </div>
            <div className="hero-stat-card hero-stat-card-compact">
              <span className="hero-stat-value">{pendingCount + finishedCount}</span>
              <span className="hero-stat-label">非进行中活动</span>
            </div>
          </div>
        </div>

        <div className="hero-board animate-fade-in-up">
          <div className="hero-demo-card hero-demo-card-primary">
            <div className="hero-demo-copy">
              <div className="hero-demo-topline">
                <div className="section-label" style={{ marginBottom: '10px' }}>
                  {demoMetrics ? `实时演示 / ${demoMetrics.activityName}` : '实时演示'}
                </div>
                <span className="live-dot">DEMO CONSOLE</span>
              </div>
              <h3 className="hero-demo-title">一键演示真实链路效果</h3>
              <p className="hero-demo-desc">
                当前触发的是真实秒杀业务链路，不是压测模式。点击后会批量提交一组真实请求，用于展示订单创建、MQ 异步处理、库存扣减和状态流转。
              </p>
            </div>
            <div className="demo-metrics">
              <div className="demo-metric-card">
                <span className="demo-metric-label">本轮触发数</span>
                <span className="demo-metric-value">
                  {DEMO_REQUEST_COUNT}
                </span>
              </div>
              <div className="demo-metric-card">
                <span className="demo-metric-label">订单成功数</span>
                <span className="demo-metric-value">
                  {demoLoading && !demoMetrics ? '--' : demoMetrics?.successCount ?? '--'}
                </span>
              </div>
              <div className="demo-metric-card">
                <span className="demo-metric-label">库存消耗</span>
                <span className="demo-metric-value">
                  {demoLoading && !demoMetrics ? '--' : stockConsumed ?? '--'}
                </span>
              </div>
            </div>
            <div className="demo-button-row">
              <button
                className="demo-button demo-button-active demo-button-primary"
                type="button"
                onClick={() => void handleRunDemo(DEMO_REQUEST_COUNT)}
                disabled={demoRunLoading !== null}
              >
                {demoRunLoading === DEMO_REQUEST_COUNT ? '触发中...' : '一键演示真实链路效果'}
              </button>
            </div>
            <div className="demo-placeholder">
              <span className="demo-placeholder-label">LIVE STATUS FLOW</span>
              {demoRunMessage ? (
                <span className="demo-placeholder-text">{demoRunMessage}</span>
              ) : null}
              {demoError ? (
                <span className="demo-placeholder-text">演示数据加载失败：{demoError}</span>
              ) : demoMetrics ? (
                <div className="demo-status-list">
                  {demoMetrics.statusFlow.map((item) => (
                    <div key={item.status} className="demo-status-item">
                      <span className="demo-status-name">{item.status}</span>
                      <span className="demo-status-count">{item.count}</span>
                    </div>
                  ))}
                </div>
              ) : (
                <span className="demo-placeholder-text">正在等待演示数据返回。</span>
              )}
              <span className="demo-placeholder-note">说明：该按钮用于展示真实业务链路效果，不作为压测结果口径。</span>
            </div>
          </div>
        </div>
      </section>

      <section className="feature-strip">
        <div className="feature-strip-card">
          <div className="feature-strip-title">库存热点治理</div>
          <div className="feature-strip-desc">Redis 分桶库存按用户路由，减少单 key 热点写压力。</div>
        </div>
        <div className="feature-strip-card">
          <div className="feature-strip-title">异步削峰填谷</div>
          <div className="feature-strip-desc">请求先受理再排队处理，前端轮询展示最终状态。</div>
        </div>
        <div className="feature-strip-card">
          <div className="feature-strip-title">工程权衡可讲</div>
          <div className="feature-strip-desc">当前桶空即失败，未做跨桶查找，适合面试中主动解释 tradeoff。</div>
        </div>
      </section>

      <div className="section-shell" id="activity-grid">
        <div className="section-shell-header">
          <div>
            <div className="section-label">精选活动</div>
            <h2 className="section-shell-title">本场秒杀列表</h2>
          </div>
          <div className="section-shell-note">点击卡片查看详情、倒计时、库存进度与下单状态轮询</div>
        </div>
        <div className="activity-showcase-grid">
          <div className="activity-card-grid stagger-children">
            {activities.map((activity, i) => (
              <div
                key={activity.id}
                className="animate-fade-in-up"
                style={{ animationDelay: `${0.1 + i * 0.07}s`, cursor: 'pointer' }}
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
                    <SeckillButton activity={activity} onSeckill={handleSeckill} />
                  </div>
                </Card>
              </div>
            ))}
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
    </div>
  );
}
