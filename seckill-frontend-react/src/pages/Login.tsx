import { Form, Button, Typography, Card } from '@douyinfe/semi-ui';
import { useNavigate, Link } from 'react-router-dom';
import { useAuthStore } from '../stores/useAuthStore';

const { Title, Text } = Typography;

export default function Login() {
  const navigate = useNavigate();
  const { login, loading, error } = useAuthStore();

  const handleSubmit = async (values: { username: string; password: string }) => {
    try {
      await login(values);
      navigate('/');
    } catch {
      // 错误已在 store 中处理
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <Card className="w-full max-w-md">
        <div className="text-center mb-6">
          <Title heading={3}>用户登录</Title>
        </div>
        <Form initValues={{ username: '', password: '' }} onSubmit={handleSubmit}>
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
          {error && (
            <div className="text-red-500 text-sm mb-4">{error}</div>
          )}
          <Button
            type="primary"
            htmlType="submit"
            loading={loading}
            className="w-full"
          >
            登录
          </Button>
        </Form>
        <div className="text-center mt-4">
          <Text type="secondary">
            还没有账号？ <Link to="/register">立即注册</Link>
          </Text>
        </div>
      </Card>
    </div>
  );
}
