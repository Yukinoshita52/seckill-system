import { create } from 'zustand';
import { activityApi } from '../api/activity';
import { Activity } from '../types/activity';

interface SeckillState {
  activities: Activity[];
  loading: boolean;
  error: string | null;

  fetchActivities: () => Promise<void>;
  startActivity: (id: number) => void;
}

export const useSeckillStore = create<SeckillState>((set) => ({
  activities: [],
  loading: false,
  error: null,

  fetchActivities: async () => {
    set({ loading: true, error: null });
    try {
      const response = await activityApi.getActivities();
      set({ activities: response.data || [], loading: false });
    } catch (error) {
      set({ loading: false, error: (error as Error).message });
    }
  },

  startActivity: (id: number) => {
    // 跳转到活动详情页
    window.location.href = `/activity/${id}`;
  },
}));
