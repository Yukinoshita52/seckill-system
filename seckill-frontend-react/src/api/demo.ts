import apiClient from './index';
import { ApiResponse } from '../types/api';
import { DemoMetrics } from '../types/demo';

export const demoApi = {
  getMetrics(activityId: number): Promise<ApiResponse<DemoMetrics>> {
    return apiClient.get(`/demo/metrics/${activityId}`);
  },
};
