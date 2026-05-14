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
    <>
      <div className="bg-grid" />
      <div className="bg-glow" />

      <Header className="header">
        <div className="header-shell">
          <div className="header-logo" onClick={() => navigate('/')}>
            <div className="header-logo-icon">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
                <path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z" fill="currentColor" style={{ color: '#000' }} />
              </svg>
            </div>
            <div className="header-brand">
              <span className="header-brand-kicker">SECKILL SHOWCASE</span>
              <span className="header-logo-text">高并发秒杀系统</span>
            </div>
          </div>

          <div className="header-nav-shell">
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
            />
          </div>

          <div className="header-actions">
            {isAuthenticated ? (
              <>
                <Avatar size="small">{username?.charAt(0).toUpperCase()}</Avatar>
                <span style={{ fontSize: '14px', color: 'var(--color-text-secondary)', fontWeight: 500 }}>
                  {username}
                </span>
                <Button type="tertiary" size="small" onClick={handleLogout} style={{ fontSize: '13px' }}>
                  退出
                </Button>
              </>
            ) : (
              <>
                <Button type="secondary" size="small" onClick={() => navigate('/admin')} className="header-secondary-button">
                  管理后台
                </Button>
                <Button type="primary" size="small" onClick={() => navigate('/login')} className="header-primary-button">
                  登录体验
                </Button>
              </>
            )}
          </div>
        </div>
      </Header>

      <Content
        style={{
          maxWidth: '1280px',
          margin: '0 auto',
          padding: '40px 24px 80px',
          background: 'transparent',
          position: 'relative',
          zIndex: 1,
        }}
      >
        {children}
      </Content>
    </>
  );
}
