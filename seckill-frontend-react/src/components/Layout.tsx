import { ReactNode } from 'react';
import { Layout as SemiLayout, Nav, Button, Avatar } from '@douyinfe/semi-ui';
import { IconHome, IconSetting } from '@douyinfe/semi-icons';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuthStore } from '../stores/useAuthStore';

const { Header, Content } = SemiLayout;

interface LayoutProps {
  children: ReactNode;
}

export default function Layout({ children }: LayoutProps) {
  const navigate = useNavigate();
  const location = useLocation();
  const { isAuthenticated, username, logout } = useAuthStore();

  const isAdmin = location.pathname.startsWith('/admin');

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <SemiLayout style={{ minHeight: '100vh', background: 'var(--color-bg)' }}>
      <Header
        style={{
          background: 'rgba(20, 20, 26, 0.85)',
          backdropFilter: 'blur(20px)',
          WebkitBackdropFilter: 'blur(20px)',
          borderBottom: '1px solid var(--color-border)',
          height: '64px',
          lineHeight: '64px',
        }}
      >
        <div
          className="max-w-7xl mx-auto px-6 flex items-center justify-between"
          style={{ height: '100%' }}
        >
          {/* Logo */}
          <div
            className="flex items-center gap-3 cursor-pointer"
            onClick={() => navigate('/')}
          >
            <div
              className="flex items-center justify-center w-8 h-8 rounded-lg"
              style={{
                background: 'linear-gradient(135deg, var(--color-accent) 0%, #ea580c 100%)',
              }}
            >
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
                <path
                  d="M13 2L3 14h9l-1 8 10-12h-9l1-8z"
                  fill="currentColor"
                  style={{ color: '#000' }}
                />
              </svg>
            </div>
            <span
              style={{
                fontWeight: 700,
                fontSize: '18px',
                color: 'var(--color-text)',
                letterSpacing: '-0.01em',
              }}
            >
              秒杀
            </span>
          </div>

          <Nav
            mode="horizontal"
            selectedKeys={[location.pathname]}
            onSelect={({ selectedKeys }) => navigate(selectedKeys[0] as string)}
            items={[
              { itemKey: '/', text: '首页', icon: <IconHome /> },
              ...(isAdmin
                ? [
                    { itemKey: '/admin', text: '仪表盘', icon: <IconSetting /> },
                    { itemKey: '/admin/activities', text: '活动管理', icon: <IconSetting /> },
                  ]
                : []),
            ]}
            style={{ background: 'transparent', border: 'none' }}
          />

          <div className="flex items-center gap-3">
            {isAuthenticated ? (
              <>
                <Avatar
                  size="small"
                  style={{
                    background: 'linear-gradient(135deg, var(--color-accent), #ea580c)',
                    color: '#000',
                    fontWeight: 700,
                    fontSize: '12px',
                  }}
                >
                  {username?.charAt(0).toUpperCase()}
                </Avatar>
                <span
                  style={{
                    fontSize: '14px',
                    color: 'var(--color-text-secondary)',
                    fontWeight: 500,
                  }}
                >
                  {username}
                </span>
                <Button
                  type="tertiary"
                  size="small"
                  onClick={handleLogout}
                  style={{ fontSize: '13px' }}
                >
                  退出
                </Button>
              </>
            ) : (
              <Button
                type="primary"
                size="small"
                onClick={() => navigate('/login')}
                style={{ fontSize: '13px', fontWeight: 600 }}
              >
                登录
              </Button>
            )}
          </div>
        </div>
      </Header>
      <Content
        className="max-w-7xl mx-auto px-6 py-8"
        style={{ background: 'transparent' }}
      >
        {children}
      </Content>
    </SemiLayout>
  );
}