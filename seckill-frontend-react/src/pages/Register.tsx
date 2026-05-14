import { Form, Button, Typography, Card } from '@douyinfe/semi-ui';
import { useNavigate, Link } from 'react-router-dom';
import { useAuthStore } from '../stores/useAuthStore';

const { Title, Text } = Typography;

export default function Register() {
  const navigate = useNavigate();
  const { register, loading, error } = useAuthStore();

  const handleSubmit = async (values: { username: string; password: string; confirmPassword: string }) => {
    if (values.password !== values.confirmPassword) return;
    try {
      await register({ username: values.username, password: values.password });
      navigate('/login');
    } catch {
      // 错误已在 store 中处理
    }
  };

  return (
    <div
      style={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        background: 'var(--color-bg)',
        padding: '24px',
      }}
    >
      <div
        style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          background: `
            radial-gradient(ellipse 80% 50% at 50% -20%, rgba(245, 158, 11, 0.12), transparent),
            radial-gradient(ellipse 60% 40% at 20% 100%, rgba(245, 158, 11, 0.06), transparent)
          `,
          pointerEvents: 'none',
        }}
      />

      <div
        className="animate-fade-in-up"
        style={{ width: '100%', maxWidth: '420px', position: 'relative' }}
      >
        <div className="text-center" style={{ marginBottom: '32px' }}>
          <div
            className="inline-flex items-center justify-center w-14 h-14 rounded-2xl mb-4"
            style={{
              background: 'linear-gradient(135deg, var(--color-accent) 0%, #ea580c 100%)',
              boxShadow: '0 8px 24px rgba(245, 158, 11, 0.3)',
            }}
          >
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none">
              <path
                d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 3c1.66 0 3 1.34 3 3s-1.34 3-3 3-3-1.34-3-3 1.34-3 3-3zm0 14.2c-2.5 0-4.71-1.28-6-3.22.03-1.99 4-3.08 6-3.08 1.99 0 5.97 1.09 6 3.08-1.29 1.94-3.5 3.22-6 3.22z"
                fill="currentColor"
                style={{ color: '#000' }}
              />
            </svg>
          </div>
          <Title
            heading={3}
            style={{
              color: 'var(--color-text)',
              fontWeight: 800,
              margin: 0,
              fontSize: '24px',
              letterSpacing: '-0.01em',
            }}
          >
            创建账号
          </Title>
          <Text style={{ color: 'var(--color-text-secondary)', fontSize: '14px', marginTop: '6px', display: 'block' }}>
            加入秒杀，抢购好物
          </Text>
        </div>

        <Card>
          <div style={{ padding: '8px 0' }}>
            <Form
              initValues={{ username: '', password: '', confirmPassword: '' }}
              onSubmit={handleSubmit}
            >
              <Form.Input
                field="username"
                label="用户名"
                placeholder="请输入用户名"
                rules={[{ required: true, message: '请输入用户名' }]}
                style={{ marginBottom: '20px' }}
              />
              <Form.Input
                field="password"
                label="密码"
                type="password"
                placeholder="请输入密码"
                rules={[{ required: true, message: '请输入密码' }]}
                style={{ marginBottom: '20px' }}
              />
              <Form.Input
                field="confirmPassword"
                label="确认密码"
                type="password"
                placeholder="请再次输入密码"
                rules={[{ required: true, message: '请确认密码' }]}
                style={{ marginBottom: '20px' }}
              />
              {error && (
                <div
                  style={{
                    color: 'var(--color-danger)',
                    fontSize: '13px',
                    marginBottom: '16px',
                    padding: '10px 12px',
                    background: 'rgba(239, 68, 68, 0.1)',
                    borderRadius: '6px',
                    border: '1px solid rgba(239, 68, 68, 0.2)',
                  }}
                >
                  {error}
                </div>
              )}
              <Button
                type="primary"
                htmlType="submit"
                loading={loading}
                className="w-full"
                style={{ height: '44px', fontSize: '15px', marginTop: '8px' }}
              >
                注册
              </Button>
            </Form>

            <div
              style={{
                textAlign: 'center',
                marginTop: '24px',
                paddingTop: '20px',
                borderTop: '1px solid var(--color-border)',
              }}
            >
              <Text style={{ color: 'var(--color-text-muted)', fontSize: '14px' }}>
                已有账号？{' '}
                <Link
                  to="/login"
                  style={{ color: 'var(--color-accent)', textDecoration: 'none', fontWeight: 600 }}
                >
                  立即登录
                </Link>
              </Text>
            </div>
          </div>
        </Card>
      </div>
    </div>
  );
}