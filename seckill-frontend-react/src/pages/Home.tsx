import { useEffect, useMemo, useRef, useState } from 'react';
import { Select, InputNumber } from '@douyinfe/semi-ui';
import { useSeckillStore } from '../stores/useSeckillStore';
import { demoApi } from '../api/demo';
import { DemoMetrics } from '../types/demo';

function useActiveSection(sectionCount: number) {
  const [activeIndex, setActiveIndex] = useState<number | undefined>(undefined);
  const refs = useRef<(HTMLElement | null)[]>([]);
  const initializing = useRef(true);

  useEffect(() => {
    const observers: IntersectionObserver[] = [];
    refs.current.forEach((el, i) => {
      if (!el) return;
      const observer = new IntersectionObserver(
        ([entry]) => {
          if (entry.isIntersecting && entry.intersectionRatio >= 0.5) {
            setActiveIndex(i);
          }
        },
        { threshold: 0.5 }
      );
      observer.observe(el);
      observers.push(observer);
    });
    requestAnimationFrame(() => { initializing.current = false; });
    return () => observers.forEach((o) => o.disconnect());
  }, [sectionCount]);

  return { activeIndex, initializing: initializing.current, refs };
}

export default function Home() {
  const { activities, fetchActivities } = useSeckillStore();

  const [demoRequestCount, setDemoRequestCount] = useState(100);
  const [selectedActivityId, setSelectedActivityId] = useState<number | null>(null);
  const [demoMetrics, setDemoMetrics] = useState<DemoMetrics | null>(null);
  const [demoLoading, setDemoLoading] = useState(false);
  const [demoError, setDemoError] = useState<string | null>(null);
  const [demoRunLoading, setDemoRunLoading] = useState(false);
  const [demoRunMessage, setDemoRunMessage] = useState<string | null>(null);

  const SECTION_COUNT = 4;
  const { activeIndex, initializing, refs } = useActiveSection(SECTION_COUNT);
  const sectionClass = (index: number) =>
    `home-section-inner${activeIndex === undefined || activeIndex === index ? ' section-visible' : ''}${!initializing ? ' section-animated' : ''}`;

  const demoActivity = useMemo(() => {
    if (selectedActivityId != null) {
      return activities.find((a) => a.id === selectedActivityId) ?? null;
    }
    return activities.find((a) => a.status === 1) ?? activities[0] ?? null;
  }, [activities, selectedActivityId]);

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

    const loadMetrics = async (silent = false) => {
      try {
        if (!silent) setDemoLoading(true);
        setDemoError(null);
        const res = await demoApi.getMetrics(demoActivity.id);
        if (!disposed) setDemoMetrics(res.data);
      } catch (err) {
        if (!disposed) setDemoError((err as Error).message);
      } finally {
        if (!disposed && !silent) setDemoLoading(false);
      }
    };

    loadMetrics();
    intervalId = setInterval(() => void loadMetrics(true), 5000);
    return () => { disposed = true; if (intervalId) clearInterval(intervalId); };
  }, [demoActivity]);

  const loadMetricsOnce = async () => {
    if (!demoActivity) return;
    try {
      setDemoError(null);
      const res = await demoApi.getMetrics(demoActivity.id);
      setDemoMetrics(res.data);
    } catch (err) {
      setDemoError((err as Error).message);
    }
  };

  const handleRunDemo = async () => {
    if (!demoActivity) return;
    try {
      setDemoRunLoading(true);
      setDemoRunMessage(null);
      setDemoError(null);
      const res = await demoApi.runDemo({
        activityId: demoActivity.id,
        requestCount: demoRequestCount,
      });
      setDemoRunMessage(res.data.message);
      await loadMetricsOnce();
    } catch (err) {
      setDemoError((err as Error).message);
    } finally {
      setDemoRunLoading(false);
    }
  };

  const stockConsumed = demoMetrics ? Math.max(demoMetrics.totalStock - demoMetrics.remainStock, 0) : null;

  const setRef = (index: number) => (el: HTMLElement | null) => { refs.current[index] = el; };

  return (
    <div className="home-snap-container">

      {/* Section 0: Hero */}
      <section
        ref={setRef(0)}
        className="home-snap-section"
      >
        <div className={sectionClass(0)}>
          <div className="section-label" style={{ marginBottom: '16px' }}>Seckill System / Architecture Demo</div>
          <h1 style={{
            fontFamily: 'var(--font-display)',
            fontWeight: 800,
            fontSize: 'clamp(36px, 5vw, 56px)',
            lineHeight: 1.15,
            color: 'var(--color-text)',
            letterSpacing: '-0.03em',
            margin: '0 0 20px',
          }}>
            高并发秒杀系统
          </h1>
          <p style={{
            fontSize: '18px',
            lineHeight: 1.7,
            color: 'var(--color-text-secondary)',
            maxWidth: '680px',
            margin: '0 auto',
          }}>
            一个面向简历展示的完整秒杀系统，覆盖 Redis 分桶库存、Lua 原子扣减、RocketMQ 异步削峰和订单状态轮询。
          </p>
        </div>
      </section>

      {/* Section 1: Architecture Diagram */}
      <section
        ref={setRef(1)}
        className="home-snap-section"
      >
        <div className={sectionClass(1)}>
          <div style={{ textAlign: 'center', marginBottom: '32px' }}>
            <div className="section-label" style={{ marginBottom: '12px' }}>Technical Architecture</div>
            <h2 style={{
              fontFamily: 'var(--font-display)',
              fontWeight: 700,
              fontSize: '28px',
              color: 'var(--color-text)',
              margin: 0,
            }}>系统架构</h2>
          </div>
          <div style={{
            background: 'var(--color-surface)',
            border: '1px solid var(--color-border)',
            borderRadius: '20px',
            padding: '48px 32px',
            overflow: 'auto',
          }}>
            <svg viewBox="0 0 800 520" style={{ width: '100%', maxWidth: '800px', margin: '0 auto', display: 'block' }}>
              {/* Layer 1: Client — widest */}
              <rect x="100" y="10" width="600" height="64" rx="14" fill="#ff4d00" opacity="0.1" stroke="#ff4d00" strokeWidth="1.5"/>
              <text x="400" y="38" textAnchor="middle" fill="#ff4d00" fontSize="16" fontWeight="700">Client</text>
              <text x="400" y="58" textAnchor="middle" fill="#94a3b8" fontSize="12">浏览器 / 全量请求</text>
              <text x="720" y="48" textAnchor="end" fill="#ff4d00" fontSize="11" fontWeight="600" opacity="0.7">100%</text>

              {/* Funnel sides */}
              <path d="M100 74 L160 108 L640 108 L700 74" fill="none" stroke="var(--color-border)" strokeWidth="1" opacity="0.4"/>
              <line x1="400" y1="74" x2="400" y2="108" stroke="var(--color-border)" strokeWidth="1.5" markerEnd="url(#arrowhead)"/>

              {/* Layer 2: Gateway */}
              <rect x="160" y="108" width="480" height="64" rx="14" fill="#3b82f6" opacity="0.1" stroke="#3b82f6" strokeWidth="1.5"/>
              <text x="400" y="136" textAnchor="middle" fill="#93c5fd" fontSize="15" fontWeight="700">Spring Cloud Gateway</text>
              <text x="400" y="156" textAnchor="middle" fill="#94a3b8" fontSize="11">JWT 鉴权 / Sentinel 限流 / 路由转发</text>
              <text x="660" y="146" textAnchor="end" fill="#3b82f6" fontSize="11" fontWeight="600" opacity="0.7">限流后 ↓</text>

              {/* Funnel sides */}
              <path d="M160 172 L220 206 L580 206 L640 172" fill="none" stroke="var(--color-border)" strokeWidth="1" opacity="0.4"/>
              <line x1="400" y1="172" x2="400" y2="206" stroke="var(--color-border)" strokeWidth="1.5" markerEnd="url(#arrowhead)"/>

              {/* Layer 3: 4-layer validation */}
              <rect x="220" y="206" width="360" height="64" rx="14" fill="#f97316" opacity="0.1" stroke="#f97316" strokeWidth="1.5"/>
              <text x="400" y="234" textAnchor="middle" fill="#fdba74" fontSize="15" fontWeight="700">四层过滤</text>
              <text x="400" y="254" textAnchor="middle" fill="#94a3b8" fontSize="11">活动状态 / 用户去重 / 库存预检 / 入口限流</text>

              {/* Funnel sides */}
              <path d="M220 270 L270 304 L530 304 L580 270" fill="none" stroke="var(--color-border)" strokeWidth="1" opacity="0.4"/>
              <line x1="400" y1="270" x2="400" y2="304" stroke="var(--color-border)" strokeWidth="1.5" markerEnd="url(#arrowhead)"/>

              {/* Layer 4: Redis Lua deduction */}
              <rect x="270" y="304" width="260" height="64" rx="14" fill="#ef4444" opacity="0.1" stroke="#ef4444" strokeWidth="1.5"/>
              <text x="400" y="332" textAnchor="middle" fill="#fca5a5" fontSize="15" fontWeight="700">Redis Lua 原子扣减</text>
              <text x="400" y="352" textAnchor="middle" fill="#94a3b8" fontSize="11">分桶库存 DECR / 去重集合 SADD</text>

              {/* Funnel sides */}
              <path d="M270 368 L320 402 L480 402 L530 368" fill="none" stroke="var(--color-border)" strokeWidth="1" opacity="0.4"/>
              <line x1="400" y1="368" x2="400" y2="402" stroke="var(--color-border)" strokeWidth="1.5" markerEnd="url(#arrowhead)"/>
              <text x="540" y="390" textAnchor="start" fill="#ef4444" fontSize="11" fontWeight="600" opacity="0.7">仅成功 ↓</text>

              {/* Layer 5: MQ + DB — narrowest */}
              <rect x="320" y="402" width="160" height="64" rx="14" fill="#a855f7" opacity="0.1" stroke="#a855f7" strokeWidth="1.5"/>
              <text x="400" y="430" textAnchor="middle" fill="#c4b5fd" fontSize="14" fontWeight="700">RocketMQ</text>
              <text x="400" y="450" textAnchor="middle" fill="#94a3b8" fontSize="11">异步落库 MySQL</text>

              {/* Side: MQ → DB detail */}
              <line x1="480" y1="434" x2="540" y2="434" stroke="var(--color-border)" strokeWidth="1.5" markerEnd="url(#arrowhead)"/>
              <rect x="540" y="410" width="140" height="48" rx="10" fill="#22c55e" opacity="0.1" stroke="#22c55e" strokeWidth="1"/>
              <text x="610" y="432" textAnchor="middle" fill="#86efac" fontSize="13" fontWeight="600">MySQL</text>
              <text x="610" y="450" textAnchor="middle" fill="#94a3b8" fontSize="10">订单 / 扣减日志</text>

              {/* Polling arrow: Engine back to Client */}
              <path d="M270 434 Q 40 434 40 42 Q 40 10 100 42" fill="none" stroke="var(--color-accent)" strokeWidth="1.2" strokeDasharray="6 4"/>
              <text x="28" y="240" textAnchor="middle" fill="var(--color-accent)" fontSize="10" transform="rotate(-90 28 240)">状态轮询</text>

              <defs>
                <marker id="arrowhead" markerWidth="8" markerHeight="6" refX="8" refY="3" orient="auto">
                  <polygon points="0 0, 8 3, 0 6" fill="var(--color-text-muted)"/>
                </marker>
              </defs>
            </svg>
          </div>
        </div>
      </section>

      {/* Section 2: Order State Flow */}
      <section
        ref={setRef(2)}
        className="home-snap-section"
      >
        <div className={sectionClass(2)}>
          <div style={{ textAlign: 'center', marginBottom: '32px' }}>
            <div className="section-label" style={{ marginBottom: '12px' }}>Order Lifecycle</div>
            <h2 style={{
              fontFamily: 'var(--font-display)',
              fontWeight: 700,
              fontSize: '28px',
              color: 'var(--color-text)',
              margin: 0,
            }}>订单状态流转</h2>
          </div>
          <div style={{
            background: 'var(--color-surface)',
            border: '1px solid var(--color-border)',
            borderRadius: '20px',
            padding: '48px 32px',
            overflow: 'auto',
          }}>
            <svg viewBox="0 0 780 200" style={{ width: '100%', maxWidth: '780px', margin: '0 auto', display: 'block' }}>
              {/* PENDING */}
              <rect x="20" y="40" width="150" height="64" rx="14" fill="#f59e0b" opacity="0.15" stroke="#f59e0b" strokeWidth="1.5"/>
              <text x="95" y="68" textAnchor="middle" fill="#fbbf24" fontSize="15" fontWeight="700">PENDING</text>
              <text x="95" y="88" textAnchor="middle" fill="#94a3b8" fontSize="11">请求已受理</text>

              <line x1="170" y1="72" x2="230" y2="72" stroke="var(--color-text-muted)" strokeWidth="1.5" markerEnd="url(#arrow)"/>
              <text x="200" y="62" textAnchor="middle" fill="#64748b" fontSize="10">MQ 消费</text>

              {/* UNPAID */}
              <rect x="230" y="40" width="150" height="64" rx="14" fill="#22c55e" opacity="0.15" stroke="#22c55e" strokeWidth="1.5"/>
              <text x="305" y="68" textAnchor="middle" fill="#4ade80" fontSize="15" fontWeight="700">UNPAID</text>
              <text x="305" y="88" textAnchor="middle" fill="#94a3b8" fontSize="11">扣库存成功</text>

              <line x1="380" y1="72" x2="440" y2="72" stroke="var(--color-text-muted)" strokeWidth="1.5" markerEnd="url(#arrow)"/>
              <text x="410" y="62" textAnchor="middle" fill="#64748b" fontSize="10">支付</text>

              {/* SUCCESS */}
              <rect x="440" y="40" width="150" height="64" rx="14" fill="#3b82f6" opacity="0.15" stroke="#3b82f6" strokeWidth="1.5"/>
              <text x="515" y="68" textAnchor="middle" fill="#60a5fa" fontSize="15" fontWeight="700">SUCCESS</text>
              <text x="515" y="88" textAnchor="middle" fill="#94a3b8" fontSize="11">订单完成</text>

              {/* PENDING → FAILED */}
              <path d="M95 104 L95 155 L215 155 L215 125" fill="none" stroke="#ef4444" strokeWidth="1.5" markerEnd="url(#arrow-red)"/>
              <text x="155" y="148" textAnchor="middle" fill="#ef4444" fontSize="10">库存不足 / MQ 异常</text>

              {/* FAILED */}
              <rect x="140" y="125" width="150" height="64" rx="14" fill="#ef4444" opacity="0.15" stroke="#ef4444" strokeWidth="1.5"/>
              <text x="215" y="153" textAnchor="middle" fill="#f87171" fontSize="15" fontWeight="700">FAILED</text>
              <text x="215" y="173" textAnchor="middle" fill="#94a3b8" fontSize="11">扣减失败</text>

              {/* UNPAID → TIMEOUT */}
              <path d="M305 104 L305 155 L425 155 L425 125" fill="none" stroke="#f97316" strokeWidth="1.5" markerEnd="url(#arrow-orange)"/>
              <text x="365" y="148" textAnchor="middle" fill="#f97316" fontSize="10">超时未支付</text>

              {/* TIMEOUT */}
              <rect x="350" y="125" width="150" height="64" rx="14" fill="#f97316" opacity="0.15" stroke="#f97316" strokeWidth="1.5"/>
              <text x="425" y="153" textAnchor="middle" fill="#fb923c" fontSize="15" fontWeight="700">TIMEOUT</text>
              <text x="425" y="173" textAnchor="middle" fill="#94a3b8" fontSize="11">订单超时</text>

              {/* Legend */}
              <rect x="620" y="40" width="140" height="150" rx="12" fill="none" stroke="var(--color-border)" strokeWidth="1"/>
              <text x="690" y="62" textAnchor="middle" fill="var(--color-text-muted)" fontSize="11" fontWeight="600">状态说明</text>
              <circle cx="640" cy="82" r="5" fill="#f59e0b" opacity="0.6"/><text x="652" y="86" fill="#94a3b8" fontSize="10">请求受理排队中</text>
              <circle cx="640" cy="104" r="5" fill="#22c55e" opacity="0.6"/><text x="652" y="108" fill="#94a3b8" fontSize="10">Lua 扣库存成功</text>
              <circle cx="640" cy="126" r="5" fill="#3b82f6" opacity="0.6"/><text x="652" y="130" fill="#94a3b8" fontSize="10">用户支付完成</text>
              <circle cx="640" cy="148" r="5" fill="#ef4444" opacity="0.6"/><text x="652" y="152" fill="#94a3b8" fontSize="10">库存不足扣减失败</text>
              <circle cx="640" cy="170" r="5" fill="#f97316" opacity="0.6"/><text x="652" y="174" fill="#94a3b8" fontSize="10">超时未支付自动关闭</text>

              <defs>
                <marker id="arrow" markerWidth="8" markerHeight="6" refX="8" refY="3" orient="auto">
                  <polygon points="0 0, 8 3, 0 6" fill="var(--color-text-muted)"/>
                </marker>
                <marker id="arrow-red" markerWidth="8" markerHeight="6" refX="8" refY="3" orient="auto">
                  <polygon points="0 0, 8 3, 0 6" fill="#ef4444"/>
                </marker>
                <marker id="arrow-orange" markerWidth="8" markerHeight="6" refX="8" refY="3" orient="auto">
                  <polygon points="0 0, 8 3, 0 6" fill="#f97316"/>
                </marker>
              </defs>
            </svg>
          </div>
        </div>
      </section>

      {/* Section 3: Demo */}
      <section
        ref={setRef(3)}
        className="home-snap-section"
      >
        <div className={sectionClass(3)}>
          <div style={{ textAlign: 'center', marginBottom: '32px' }}>
            <div className="section-label" style={{ marginBottom: '12px' }}>Live Demo</div>
            <h2 style={{
              fontFamily: 'var(--font-display)',
              fontWeight: 700,
              fontSize: '28px',
              color: 'var(--color-text)',
              margin: 0,
            }}>一键演示真实链路</h2>
            <p style={{
              fontSize: '16px',
              color: 'var(--color-text-secondary)',
              margin: '12px 0 0',
              maxWidth: '600px',
              marginLeft: 'auto',
              marginRight: 'auto',
            }}>
              批量提交真实秒杀请求，展示从请求受理到 MQ 异步扣库存的完整链路。
            </p>
          </div>

          <div style={{
            background: 'var(--color-surface)',
            border: '1px solid var(--color-border)',
            borderRadius: '20px',
            padding: '32px',
            maxWidth: '640px',
            margin: '0 auto',
            width: '100%',
          }}>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '16px', marginBottom: '24px' }}>
              <Select
                placeholder="选择演示活动（默认自动选进行中）"
                optionList={activities.map((a) => ({ label: a.activityName, value: a.id }))}
                value={selectedActivityId ?? undefined}
                onChange={(v) => setSelectedActivityId(v as number)}
                style={{ width: '100%' }}
              />
              <div style={{ display: 'flex', gap: '12px', alignItems: 'center' }}>
                <InputNumber
                  min={1}
                  max={1000}
                  step={10}
                  value={demoRequestCount}
                  onChange={(v) => setDemoRequestCount(Number(v))}
                  style={{ flex: 1 }}
                />
                <span style={{ fontSize: '14px', color: 'var(--color-text-muted)', whiteSpace: 'nowrap' }}>次请求</span>
              </div>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '12px', marginBottom: '24px' }}>
              <div style={{ background: 'var(--color-surface-2)', borderRadius: '12px', padding: '16px', textAlign: 'center' }}>
                <div style={{ fontSize: '12px', color: 'var(--color-text-muted)', marginBottom: '6px', letterSpacing: '0.05em', textTransform: 'uppercase' }}>触发数</div>
                <div style={{ fontFamily: 'var(--font-mono)', fontSize: '24px', fontWeight: 700, color: 'var(--color-text)' }}>{demoRequestCount}</div>
              </div>
              <div style={{ background: 'var(--color-surface-2)', borderRadius: '12px', padding: '16px', textAlign: 'center' }}>
                <div style={{ fontSize: '12px', color: 'var(--color-text-muted)', marginBottom: '6px', letterSpacing: '0.05em', textTransform: 'uppercase' }}>成功数</div>
                <div style={{ fontFamily: 'var(--font-mono)', fontSize: '24px', fontWeight: 700, color: 'var(--color-text)' }}>
                  {demoLoading && !demoMetrics ? '--' : demoMetrics?.successCount ?? '--'}
                </div>
              </div>
              <div style={{ background: 'var(--color-surface-2)', borderRadius: '12px', padding: '16px', textAlign: 'center' }}>
                <div style={{ fontSize: '12px', color: 'var(--color-text-muted)', marginBottom: '6px', letterSpacing: '0.05em', textTransform: 'uppercase' }}>库存消耗</div>
                <div style={{ fontFamily: 'var(--font-mono)', fontSize: '24px', fontWeight: 700, color: 'var(--color-text)' }}>
                  {demoLoading && !demoMetrics ? '--' : stockConsumed ?? '--'}
                </div>
              </div>
            </div>

            <button
              className="demo-button demo-button-active demo-button-primary"
              type="button"
              onClick={() => void handleRunDemo()}
              disabled={demoRunLoading}
              style={{ width: '100%', height: '52px', fontSize: '16px' }}
            >
              {demoRunLoading ? '触发中...' : `触发 ${demoRequestCount} 次演示请求`}
            </button>

            <div style={{ marginTop: '20px' }}>
              {demoRunMessage ? (
                <div style={{ fontSize: '14px', color: '#4ade80', marginBottom: '8px' }}>{demoRunMessage}</div>
              ) : null}
              {demoError ? (
                <div style={{ fontSize: '14px', color: '#f87171', marginBottom: '8px' }}>加载失败：{demoError}</div>
              ) : demoMetrics ? (
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: '8px' }}>
                  {demoMetrics.statusFlow.map((item) => (
                    <span key={item.status} style={{
                      fontFamily: 'var(--font-mono)',
                      fontSize: '13px',
                      color: 'var(--color-text-secondary)',
                      background: 'var(--color-surface-2)',
                      padding: '6px 12px',
                      borderRadius: '8px',
                      border: '1px solid var(--color-border)',
                    }}>
                      {item.status}: {item.count}
                    </span>
                  ))}
                </div>
              ) : (
                <div style={{ fontSize: '14px', color: 'var(--color-text-muted)' }}>选择活动后点击触发按钮开始演示。</div>
              )}
            </div>
          </div>
        </div>
      </section>

    </div>
  );
}
