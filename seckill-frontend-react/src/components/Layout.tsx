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
    <SemiLayout className="min-h-screen">
      <Header className="bg-white shadow-sm">
        <div className="max-w-7xl mx-auto px-4 flex items-center justify-between h-16">
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
          <div className="flex items-center gap-4">
            {isAuthenticated ? (
              <>
                <Avatar size="small" color="blue">
                  {username?.charAt(0).toUpperCase()}
                </Avatar>
                <span className="text-sm text-gray-600">{username}</span>
                <Button type="tertiary" onClick={handleLogout}>
                  退出
                </Button>
              </>
            ) : (
              <Button type="primary" onClick={() => navigate('/login')}>
                登录
              </Button>
            )}
          </div>
        </div>
      </Header>
      <Content className="max-w-7xl mx-auto px-4 py-6">
        {children}
      </Content>
    </SemiLayout>
  );
}
