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
      {/* Background decoration */}
      <div
        style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          background: `
            radial-gradient(ellipse 80% 50% at 50% -20%, rgba(245, 158, 11, 0.12), transparent),
            radial-gradient(ellipse 60% 40% at 80% 100%, rgba(245, 158, 11, 0.06), transparent)
          `,
          pointerEvents: 'none',
        }}
      />

      <div
        className="animate-fade-in-up"
        style={{ width: '100%', maxWidth: '420px', position: 'relative' }}
      >
        {/* Logo / Brand */}
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
                d="M13 2L3 14h9l-1 8 10-12h-9l1-8z"
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
            欢迎回来
          </Title>
          <Text style={{ color: 'var(--color-text-secondary)', fontSize: '14px', marginTop: '6px', display: 'block' }}>
            登录到秒杀系统
          </Text>
        </div>

        <Card>
          <div style={{ padding: '8px 0' }}>
            <Form
              initValues={{ username: '', password: '' }}
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
                登录
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
                还没有账号？{' '}
                <Link
                  to="/register"
                  style={{
                    color: 'var(--color-accent)',
                    textDecoration: 'none',
                    fontWeight: 600,
                  }}
                >
                  立即注册
                </Link>
              </Text>
            </div>
          </div>
        </Card>
      </div>
    </div>
  );
}