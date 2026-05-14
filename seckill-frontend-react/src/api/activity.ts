import apiClient from './index';
import { Activity, ActivityCreateRequest } from '../types/activity';
import { ApiResponse } from '../types/api';

export const activityApi = {
  getActivities(): Promise<ApiResponse<Activity[]>> {
    return apiClient.get('/activity/list');
  },

  getActivity(id: number): Promise<ApiResponse<Activity>> {
    return apiClient.get(`/activity/${id}`);
  },

  // 管理后台接口
  createActivity(data: ActivityCreateRequest): Promise<ApiResponse<Activity>> {
    return apiClient.post('/admin/activity', data);
  },

  initCache(activityId: number): Promise<ApiResponse<void>> {
    return apiClient.post(`/admin/activity/${activityId}/init-cache`);
  },

  getAdminActivities(): Promise<ApiResponse<Activity[]>> {
    return apiClient.get('/admin/activity/list');
  },
};
