import apiClient from './index';
import { ApiResponse } from '../types/api';
import { DemoMetrics } from '../types/demo';
import { DemoRunRequest, DemoRunResponse } from '../types/demo-run';

export const demoApi = {
  getMetrics(activityId: number): Promise<ApiResponse<DemoMetrics>> {
    return apiClient.get(`/demo/metrics/${activityId}`);
  },

  runDemo(payload: DemoRunRequest): Promise<ApiResponse<DemoRunResponse>> {
    return apiClient.post('/demo/run', payload);
  },
};
