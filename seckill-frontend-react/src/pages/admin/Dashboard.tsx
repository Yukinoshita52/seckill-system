import { useEffect, useState } from 'react';
import { Card, Typography, Row, Col } from '@douyinfe/semi-ui';
import { IconUser, IconSetting, IconTickCircle } from '@douyinfe/semi-icons';
import { activityApi } from '../../api/activity';
import { Activity } from '../../types/activity';

const { Title, Text } = Typography;

export default function Dashboard() {
  const [activities, setActivities] = useState<Activity[]>([]);

  useEffect(() => {
    const fetchActivities = async () => {
      try {
        const response = await activityApi.getAdminActivities();
        setActivities(response.data || []);
      } catch (error) {
        console.error('Failed to fetch activities:', error);
      }
    };
    fetchActivities();
  }, []);

  const activeActivities = activities.filter((a) => a.status === 1);
  const totalStock = activities.reduce((sum, a) => sum + a.totalStock, 0);
  const remainStock = activities.reduce((sum, a) => sum + a.remainStock, 0);

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
      <Title heading={3}>管理后台</Title>
      <Row gutter={16}>
        <Col span={8}>
          <Card>
            <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
              <IconSetting size="extra-large" style={{ color: 'var(--semi-color-primary)' }} />
              <div>
                <Text type="secondary" size="small">活动总数</Text>
                <Title heading={3} style={{ margin: 0 }}>{activities.length}</Title>
              </div>
            </div>
          </Card>
        </Col>
        <Col span={8}>
          <Card>
            <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
              <IconUser size="extra-large" style={{ color: 'var(--semi-color-success)' }} />
              <div>
                <Text type="secondary" size="small">进行中活动</Text>
                <Title heading={3} style={{ margin: 0 }}>{activeActivities.length}</Title>
              </div>
            </div>
          </Card>
        </Col>
        <Col span={8}>
          <Card>
            <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
              <IconTickCircle size="extra-large" style={{ color: 'var(--semi-color-warning)' }} />
              <div>
                <Text type="secondary" size="small">总库存 / 剩余库存</Text>
                <Title heading={3} style={{ margin: 0 }}>{totalStock} / {remainStock}</Title>
              </div>
            </div>
          </Card>
        </Col>
      </Row>
    </div>
  );
}
