import axios from 'axios';
import { ApiResponse } from '../types/api';
import { DemoMetrics } from '../types/demo';
import { DemoRunRequest, DemoRunResponse } from '../types/demo-run';

// 演示接口使用独立 axios 实例，不带 auth token
const demoClient = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' },
});

demoClient.interceptors.response.use(
  (response) => {
    const data = response.data;
    if (data.code && data.code !== '0' && data.code !== '000000') {
      return Promise.reject(new Error(data.message || '请求失败'));
    }
    return data;
  },
  (error) => {
    if (error.message === 'Network Error') {
      return Promise.reject(new Error('网络连接失败，请检查网络'));
    }
    return Promise.reject(error);
  }
);

export const demoApi = {
  getMetrics(activityId: number): Promise<ApiResponse<DemoMetrics>> {
    return demoClient.get(`/demo/metrics/${activityId}`);
  },

  runDemo(payload: DemoRunRequest): Promise<ApiResponse<DemoRunResponse>> {
    return demoClient.post('/demo/run', payload);
  },
};
