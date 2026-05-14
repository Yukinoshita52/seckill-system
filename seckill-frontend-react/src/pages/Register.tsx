import { Form, Button, Typography, Card } from '@douyinfe/semi-ui';
import { useNavigate, Link } from 'react-router-dom';
import { useAuthStore } from '../stores/useAuthStore';

const { Title, Text } = Typography;

export default function Register() {
  const navigate = useNavigate();
  const { register, loading, error } = useAuthStore();

  const handleSubmit = async (values: { username: string; password: string; confirmPassword: string }) => {
    if (values.password !== values.confirmPassword) {
      return;
    }
    try {
      await register({
        username: values.username,
        password: values.password,
      });
      navigate('/login');
    } catch {
      // 错误已在 store 中处理
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <Card className="w-full max-w-md">
        <div className="text-center mb-6">
          <Title heading={3}>用户注册</Title>
        </div>
        <Form initValues={{ username: '', password: '', confirmPassword: '' }} onSubmit={handleSubmit}>
          <Form.Input
            field="username"
            label="用户名"
            placeholder="请输入用户名"
            rules={[{ required: true, message: '请输入用户名' }]}
          />
          <Form.Input
            field="password"
            label="密码"
            type="password"
            placeholder="请输入密码"
            rules={[{ required: true, message: '请输入密码' }]}
          />
          <Form.Input
            field="confirmPassword"
            label="确认密码"
            type="password"
            placeholder="请再次输入密码"
            rules={[{ required: true, message: '请确认密码' }]}
            validator={(fieldValue: string, values: Record<string, any>) => {
              if (fieldValue && fieldValue !== values.password) {
                return '两次密码输入不一致';
              }
              return '';
            }}
          />
          {error && (
            <div className="text-red-500 text-sm mb-4">{error}</div>
          )}
          <Button
            type="primary"
            htmlType="submit"
            loading={loading}
            className="w-full"
          >
            注册
          </Button>
        </Form>
        <div className="text-center mt-4">
          <Text type="secondary">
            已有账号？ <Link to="/login">立即登录</Link>
          </Text>
        </div>
      </Card>
    </div>
  );
}
