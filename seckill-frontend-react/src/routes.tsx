import { Routes, Route } from 'react-router-dom';
import Layout from './components/Layout';
import ProtectedRoute from './components/ProtectedRoute';
import Home from './pages/Home';
import ActivityDetail from './pages/ActivityDetail';
import Login from './pages/Login';
import Register from './pages/Register';
import Dashboard from './pages/admin/Dashboard';
import ActivityManage from './pages/admin/ActivityManage';

export default function AppRoutes() {
  return (
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
  );
}
