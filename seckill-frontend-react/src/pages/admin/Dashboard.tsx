import { useEffect, useState } from 'react';
import { Card, Typography, Row, Col } from '@douyinfe/semi-ui';
import { IconUser, IconSetting, IconTickCircle } from '@douyinfe/semi-icons';
import { activityApi } from '../../api/activity';
import { Activity } from '../../types/activity';

const { Title, Text } = Typography;

function StatCard({ icon, bgColor, borderColor, label, value }: {
  icon: React.ReactNode; bgColor: string; borderColor: string;
  label: string; value: React.ReactNode;
}) {
  return (
    <Card style={{ background: 'var(--color-surface)', border: '1px solid var(--color-border)', borderRadius: '12px', padding: '24px' }}>
      <div className="flex items-center gap-4">
        <div className="flex items-center justify-center w-12 h-12 rounded-xl" style={{ background: bgColor, border: `1px solid ${borderColor}` }}>
          {icon}
        </div>
        <div>
          <Text style={{ color: 'var(--color-text-muted)', fontSize: '12px', fontWeight: 600, letterSpacing: '0.05em', textTransform: 'uppercase' }}>
            {label}
          </Text>
          <Title heading={3} style={{ color: 'var(--color-text)', fontWeight: 800, margin: '4px 0 0', fontSize: '28px', fontFamily: 'var(--font-mono)' }}>
            {value}
          </Title>
        </div>
      </div>
    </Card>
  );
}

export default function Dashboard() {
  const [activities, setActivities] = useState<Activity[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchActivities = async () => {
      try {
        const response = await activityApi.getAdminActivities();
        setActivities(response.data || []);
        setError(null);
      } catch (err) {
        const msg = (err as Error).message;
        if (!msg.includes('登录已过期')) setError(msg);
      } finally {
        setLoading(false);
      }
    };
    fetchActivities();
  }, []);

  const activeActivities = activities.filter((a) => a.status === 1);
  const totalStock = activities.reduce((sum, a) => sum + a.totalStock, 0);
  const remainStock = activities.reduce((sum, a) => sum + a.remainStock, 0);

  return (
    <div className="space-y-8 animate-fade-in-up">
      <div className="dashboard-hero">
        <div>
          <div className="section-label">管理系统</div>
          <Title heading={3} style={{ color: 'var(--color-text)', fontWeight: 800, margin: 0, fontSize: '26px', letterSpacing: '-0.01em' }}>
            管理后台
          </Title>
          <Text style={{ color: 'var(--color-text-secondary)', fontSize: '14px', marginTop: '6px', display: 'block' }}>
            实时监控系统状态
          </Text>
        </div>
        <div className="dashboard-hero-meta">
          <span className="dashboard-meta-label">活跃活动</span>
          <span className="dashboard-meta-value">{loading ? '--' : activeActivities.length}</span>
        </div>
      </div>

      {error && (
        <div style={{
          padding: '16px 20px',
          background: 'rgba(239, 68, 68, 0.08)',
          border: '1px solid rgba(239, 68, 68, 0.2)',
          borderRadius: '12px',
          color: '#f87171',
          fontSize: '14px',
        }}>
          {error}
        </div>
      )}

      <Row gutter={16} className="stagger-children">
        <Col span={8}>
          <StatCard
            icon={<IconSetting size="large" style={{ color: 'var(--color-accent)' }} />}
            bgColor="rgba(245, 158, 11, 0.12)" borderColor="rgba(245, 158, 11, 0.2)"
            label="活动总数"
            value={loading ? <div className="skeleton-line" style={{ width: '48px', height: '28px' }} /> : activities.length}
          />
        </Col>
        <Col span={8}>
          <StatCard
            icon={<IconUser size="large" style={{ color: 'var(--color-success)' }} />}
            bgColor="rgba(34, 197, 94, 0.12)" borderColor="rgba(34, 197, 94, 0.2)"
            label="进行中活动"
            value={loading ? <div className="skeleton-line" style={{ width: '48px', height: '28px' }} /> : activeActivities.length}
          />
        </Col>
        <Col span={8}>
          <StatCard
            icon={<IconTickCircle size="large" style={{ color: '#a855f7' }} />}
            bgColor="rgba(168, 85, 247, 0.12)" borderColor="rgba(168, 85, 247, 0.2)"
            label="总库存 / 剩余"
            value={loading ? <div className="skeleton-line" style={{ width: '80px', height: '28px' }} /> : `${totalStock} / ${remainStock}`}
          />
        </Col>
      </Row>

      <Card className="glass-card" bodyStyle={{ padding: '28px 32px' }}>
        <div className="dashboard-summary-grid">
          <div>
            <div className="summary-kicker">库存概览</div>
            <div className="summary-value">{loading ? '--' : remainStock}</div>
            <div className="summary-copy">当前系统剩余可售库存</div>
          </div>
          <div>
            <div className="summary-kicker">售罄风险</div>
            <div className="summary-value">
              {loading ? '--' : totalStock === 0 ? '0%' : `${Math.round(((totalStock - remainStock) / totalStock) * 100)}%`}
            </div>
            <div className="summary-copy">已被消耗的库存占比</div>
          </div>
          <div>
            <div className="summary-kicker">运行判断</div>
            <div className="summary-status">{loading ? '--' : activeActivities.length > 0 ? '活动正常进行中' : '暂无进行中活动'}</div>
            <div className="summary-copy">建议结合活动管理页检查时间和缓存状态</div>
          </div>
        </div>
      </Card>
    </div>
  );
}
