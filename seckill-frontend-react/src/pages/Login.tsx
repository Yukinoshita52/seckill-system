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
    <div className="auth-page">
      <div className="auth-glow" />

      <div
        className="animate-fade-in-up"
        style={{ width: '100%', maxWidth: '440px', padding: '0 24px', position: 'relative', zIndex: 1 }}
      >
        {/* Logo */}
        <div style={{ textAlign: 'center', marginBottom: '40px' }}>
          <div
            style={{
              display: 'inline-flex',
              alignItems: 'center',
              justifyContent: 'center',
              width: '56px',
              height: '56px',
              borderRadius: '14px',
              background: 'linear-gradient(135deg, var(--color-accent) 0%, #e64500 100%)',
              boxShadow: '0 8px 32px rgba(255, 77, 0, 0.4)',
              marginBottom: '20px',
            }}
          >
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none">
              <path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z" fill="currentColor" style={{ color: '#fff' }} />
            </svg>
          </div>
          <Title
            heading={3}
            style={{
              fontFamily: 'var(--font-display)',
              fontWeight: 800,
              fontSize: '28px',
              color: 'var(--color-text)',
              letterSpacing: '-0.02em',
              margin: 0,
            }}
          >
            欢迎回来
          </Title>
          <Text style={{ color: 'var(--color-text-secondary)', fontSize: '15px', marginTop: '8px', display: 'block' }}>
            登录到秒杀系统
          </Text>
        </div>

        <Card
          style={{
            background: 'var(--color-surface)',
            border: '1px solid var(--color-border)',
            borderRadius: '20px',
            padding: '32px',
            boxShadow: '0 24px 64px rgba(23, 32, 51, 0.12)',
          }}
        >
          <Form initValues={{ username: '', password: '' }} onSubmit={handleSubmit}>
            <div style={{ marginBottom: '24px' }}>
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
                style={{ marginBottom: '4px' }}
              />
            </div>

            {error && <div className="form-error">{error}</div>}

            <Button
              type="primary"
              htmlType="submit"
              loading={loading}
              className="cta-button"
              style={{ width: '100%', marginTop: '8px' }}
            >
              登录
            </Button>
          </Form>

          <div
            style={{
              textAlign: 'center',
              marginTop: '28px',
              paddingTop: '24px',
              borderTop: '1px solid var(--color-border)',
            }}
          >
            <Text style={{ color: 'var(--color-text-muted)', fontSize: '14px' }}>
              还没有账号？{' '}
              <Link
                to="/register"
                style={{ color: 'var(--color-accent)', textDecoration: 'none', fontWeight: 600 }}
              >
                立即注册
              </Link>
            </Text>
          </div>
        </Card>
      </div>
    </div>
  );
}
