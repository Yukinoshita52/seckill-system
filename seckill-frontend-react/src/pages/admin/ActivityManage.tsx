import { useEffect, useRef, useState } from 'react';
import { Table, Button, Modal, Form, Typography, Tag, Toast } from '@douyinfe/semi-ui';
import { activityApi } from '../../api/activity';
import { Activity, ActivityCreateRequest } from '../../types/activity';
import { formatPrice, getStatusText, getStatusTagType } from '../../utils/format';

const { Title } = Typography;

const defaultFormValues: ActivityCreateRequest = {
  activityName: '',
  goodsId: 0,
  goodsName: '',
  originalPrice: 0,
  seckillPrice: 0,
  totalStock: 0,
  bucketCount: 5,
  startTime: '',
  endTime: '',
};

export default function ActivityManage() {
  const [activities, setActivities] = useState<Activity[]>([]);
  const [loading, setLoading] = useState(false);
  const [createVisible, setCreateVisible] = useState(false);
  const formApiRef = useRef<{ submitForm: () => void } | null>(null);

  const fetchActivities = async () => {
    setLoading(true);
    try {
      const response = await activityApi.getAdminActivities();
      setActivities(response.data || []);
    } catch (error) {
      Toast.error('加载活动列表失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchActivities();
  }, []);

  const handleCreate = async (values: ActivityCreateRequest) => {
    try {
      // Convert string values to numbers for numeric fields
      const payload: ActivityCreateRequest = {
        ...values,
        goodsId: Number(values.goodsId),
        originalPrice: Number(values.originalPrice),
        seckillPrice: Number(values.seckillPrice),
        totalStock: Number(values.totalStock),
        bucketCount: Number(values.bucketCount),
      };
      await activityApi.createActivity(payload);
      setCreateVisible(false);
      Toast.success('活动创建成功');
      fetchActivities();
    } catch (error) {
      Toast.error('创建活动失败，请重试');
    }
  };

  const handleInitCache = async (activityId: number) => {
    try {
      await activityApi.initCache(activityId);
      Toast.success('缓存初始化成功');
    } catch (error) {
      Toast.error('缓存初始化失败');
    }
  };

  const columns = [
    { title: 'ID', dataIndex: 'id', key: 'id', width: 80 },
    { title: '活动名称', dataIndex: 'activityName', key: 'activityName' },
    { title: '商品', dataIndex: 'goodsName', key: 'goodsName' },
    {
      title: '秒杀价',
      dataIndex: 'seckillPrice',
      key: 'seckillPrice',
      render: (price: number) => formatPrice(price),
    },
    { title: '库存', dataIndex: 'totalStock', key: 'totalStock' },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status: number) => (
        <Tag color={getStatusTagType(status)}>{getStatusText(status)}</Tag>
      ),
    },
    {
      title: '操作',
      key: 'action',
      render: (_: any, record: Activity) => (
        <Button type="secondary" onClick={() => handleInitCache(record.id)}>
          初始化缓存
        </Button>
      ),
    },
  ];

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Title heading={3}>活动管理</Title>
        <Button type="primary" onClick={() => setCreateVisible(true)}>
          新建活动
        </Button>
      </div>

      <Table columns={columns} dataSource={activities} loading={loading} rowKey="id" />

      <Modal
        title="新建活动"
        visible={createVisible}
        onCancel={() => setCreateVisible(false)}
        onOk={() => formApiRef.current?.submitForm()}
      >
        <Form
          initValues={defaultFormValues}
          getFormApi={(api) => { formApiRef.current = api; }}
          onSubmit={handleCreate}
        >
          <Form.Input field="activityName" label="活动名称" rules={[{ required: true, message: '请输入活动名称' }]} />
          <Form.Input field="goodsId" label="商品ID" type="number" rules={[{ required: true, message: '请输入商品ID' }]} />
          <Form.Input field="goodsName" label="商品名称" rules={[{ required: true, message: '请输入商品名称' }]} />
          <Form.Input field="originalPrice" label="原价" type="number" rules={[{ required: true, message: '请输入原价' }]} />
          <Form.Input field="seckillPrice" label="秒杀价" type="number" rules={[{ required: true, message: '请输入秒杀价' }]} />
          <Form.Input field="totalStock" label="总库存" type="number" rules={[{ required: true, message: '请输入总库存' }]} />
          <Form.Input field="bucketCount" label="分桶数" type="number" rules={[{ required: true, message: '请输入分桶数' }]} />
          <Form.Input field="startTime" label="开始时间" type="datetime-local" rules={[{ required: true, message: '请选择开始时间' }]} />
          <Form.Input field="endTime" label="结束时间" type="datetime-local" rules={[{ required: true, message: '请选择结束时间' }]} />
        </Form>
      </Modal>
    </div>
  );
}
