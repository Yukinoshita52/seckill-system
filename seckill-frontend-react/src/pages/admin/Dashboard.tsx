import { useEffect, useState } from 'react';
import { Card, Typography, Row, Col, Spin, Toast } from '@douyinfe/semi-ui';
import { IconUser, IconSetting, IconTickCircle } from '@douyinfe/semi-icons';
import { activityApi } from '../../api/activity';
import { Activity } from '../../types/activity';

const { Title, Text } = Typography;

export default function Dashboard() {
  const [activities, setActivities] = useState<Activity[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchActivities = async () => {
      try {
        const response = await activityApi.getAdminActivities();
        setActivities(response.data || []);
      } catch (error) {
        Toast.error('加载活动数据失败');
      } finally {
        setLoading(false);
      }
    };
    fetchActivities();
  }, []);

  const activeActivities = activities.filter((a) => a.status === 1);
  const totalStock = activities.reduce((sum, a) => sum + a.totalStock, 0);
  const remainStock = activities.reduce((sum, a) => sum + a.remainStock, 0);

  if (loading) {
    return (
      <div className="flex justify-center items-center" style={{ height: '60vh' }}>
        <Spin size="large" />
      </div>
    );
  }

  return (
    <div className="space-y-8 animate-fade-in-up">
      {/* Header */}
      <div className="dashboard-hero">
        <div>
          <div className="section-label">管理系统</div>
          <Title
            heading={3}
            style={{
              color: 'var(--color-text)',
              fontWeight: 800,
              margin: 0,
              fontSize: '26px',
              letterSpacing: '-0.01em',
            }}
          >
            管理后台
          </Title>
          <Text style={{ color: 'var(--color-text-secondary)', fontSize: '14px', marginTop: '6px', display: 'block' }}>
            实时监控系统状态
          </Text>
        </div>
        <div className="dashboard-hero-meta">
          <span className="dashboard-meta-label">活跃活动</span>
          <span className="dashboard-meta-value">{activeActivities.length}</span>
        </div>
      </div>

      {/* Stats Cards */}
      <Row gutter={16} className="stagger-children">
        <Col span={8}>
          <Card
            style={{
              background: 'var(--color-surface)',
              border: '1px solid var(--color-border)',
              borderRadius: '12px',
              padding: '24px',
            }}
          >
            <div className="flex items-center gap-4">
              <div
                className="flex items-center justify-center w-12 h-12 rounded-xl"
                style={{
                  background: 'rgba(245, 158, 11, 0.12)',
                  border: '1px solid rgba(245, 158, 11, 0.2)',
                }}
              >
                <IconSetting size="large" style={{ color: 'var(--color-accent)' }} />
              </div>
              <div>
                <Text style={{ color: 'var(--color-text-muted)', fontSize: '12px', fontWeight: 600, letterSpacing: '0.05em', textTransform: 'uppercase' }}>
                  活动总数
                </Text>
                <Title heading={3} style={{ color: 'var(--color-text)', fontWeight: 800, margin: '4px 0 0', fontSize: '28px', fontFamily: 'var(--font-mono)' }}>
                  {activities.length}
                </Title>
              </div>
            </div>
          </Card>
        </Col>

        <Col span={8}>
          <Card
            style={{
              background: 'var(--color-surface)',
              border: '1px solid var(--color-border)',
              borderRadius: '12px',
              padding: '24px',
            }}
          >
            <div className="flex items-center gap-4">
              <div
                className="flex items-center justify-center w-12 h-12 rounded-xl"
                style={{
                  background: 'rgba(34, 197, 94, 0.12)',
                  border: '1px solid rgba(34, 197, 94, 0.2)',
                }}
              >
                <IconUser size="large" style={{ color: 'var(--color-success)' }} />
              </div>
              <div>
                <Text style={{ color: 'var(--color-text-muted)', fontSize: '12px', fontWeight: 600, letterSpacing: '0.05em', textTransform: 'uppercase' }}>
                  进行中活动
                </Text>
                <Title heading={3} style={{ color: 'var(--color-text)', fontWeight: 800, margin: '4px 0 0', fontSize: '28px', fontFamily: 'var(--font-mono)' }}>
                  {activeActivities.length}
                </Title>
              </div>
            </div>
          </Card>
        </Col>

        <Col span={8}>
          <Card
            style={{
              background: 'var(--color-surface)',
              border: '1px solid var(--color-border)',
              borderRadius: '12px',
              padding: '24px',
            }}
          >
            <div className="flex items-center gap-4">
              <div
                className="flex items-center justify-center w-12 h-12 rounded-xl"
                style={{
                  background: 'rgba(168, 85, 247, 0.12)',
                  border: '1px solid rgba(168, 85, 247, 0.2)',
                }}
              >
                <IconTickCircle size="large" style={{ color: '#a855f7' }} />
              </div>
              <div>
                <Text style={{ color: 'var(--color-text-muted)', fontSize: '12px', fontWeight: 600, letterSpacing: '0.05em', textTransform: 'uppercase' }}>
                  总库存 / 剩余
                </Text>
                <Title heading={3} style={{ color: 'var(--color-text)', fontWeight: 800, margin: '4px 0 0', fontSize: '28px', fontFamily: 'var(--font-mono)' }}>
                  {totalStock} / {remainStock}
                </Title>
              </div>
            </div>
          </Card>
        </Col>
      </Row>

      <Card className="glass-card" bodyStyle={{ padding: '28px 32px' }}>
        <div className="dashboard-summary-grid">
          <div>
            <div className="summary-kicker">库存概览</div>
            <div className="summary-value">{remainStock}</div>
            <div className="summary-copy">当前系统剩余可售库存</div>
          </div>
          <div>
            <div className="summary-kicker">售罄风险</div>
            <div className="summary-value">
              {totalStock === 0 ? '0%' : `${Math.round(((totalStock - remainStock) / totalStock) * 100)}%`}
            </div>
            <div className="summary-copy">已被消耗的库存占比</div>
          </div>
          <div>
            <div className="summary-kicker">运行判断</div>
            <div className="summary-status">{activeActivities.length > 0 ? '活动正常进行中' : '暂无进行中活动'}</div>
            <div className="summary-copy">建议结合活动管理页检查时间和缓存状态</div>
          </div>
        </div>
      </Card>
    </div>
  );
}
