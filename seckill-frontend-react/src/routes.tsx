import { Suspense, lazy } from 'react';
import { Spin } from '@douyinfe/semi-ui';
import { Routes, Route } from 'react-router-dom';
import Layout from './components/Layout';
import ProtectedRoute from './components/ProtectedRoute';

const Home = lazy(() => import('./pages/Home'));
const Activities = lazy(() => import('./pages/Activities'));
const ActivityDetail = lazy(() => import('./pages/ActivityDetail'));
const Login = lazy(() => import('./pages/Login'));
const Register = lazy(() => import('./pages/Register'));
const Dashboard = lazy(() => import('./pages/admin/Dashboard'));
const ActivityManage = lazy(() => import('./pages/admin/ActivityManage'));

function RouteFallback() {
  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '40vh' }}>
      <Spin size="large" />
    </div>
  );
}

export default function AppRoutes() {
  return (
    <Suspense fallback={<RouteFallback />}>
      <Routes>
        <Route
          path="/"
          element={
            <Layout>
              <Home />
            </Layout>
          }
        />
        <Route
          path="/activity/:id"
          element={
            <Layout>
              <ActivityDetail />
            </Layout>
          }
        />
        <Route
          path="/activities"
          element={
            <Layout>
              <Activities />
            </Layout>
          }
        />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route
          path="/admin"
          element={
            <Layout>
              <ProtectedRoute />
            </Layout>
          }
        >
          <Route index element={<Dashboard />} />
          <Route path="activities" element={<ActivityManage />} />
        </Route>
      </Routes>
    </Suspense>
  );
}
