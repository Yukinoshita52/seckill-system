import { create } from 'zustand';
import { activityApi } from '../api/activity';
import { Activity } from '../types/activity';

interface SeckillState {
  activities: Activity[];
  loading: boolean;
  error: string | null;

  fetchActivities: () => Promise<void>;
  checkSoldOut: (activityId: number) => Promise<boolean>;
  startActivity: (id: number) => void;
}

export const useSeckillStore = create<SeckillState>((set, get) => ({
  activities: [],
  loading: false,
  error: null,

  fetchActivities: async () => {
    set({ loading: true, error: null });
    try {
      const response = await activityApi.getActivities();
      const list = (response.data || []).filter((a) => a.status !== 2 && a.status !== 3);

      // 页面加载时查询各活动的真实售罄状态
      const updatedList = await Promise.all(
        list.map(async (activity) => {
          try {
            const res = await activityApi.checkSoldOut(activity.id);
            return { ...activity, soldOut: res.data?.soldOut ?? activity.soldOut };
          } catch {
            return activity;
          }
        })
      );

      set({ activities: updatedList, loading: false });
    } catch (error) {
      set({ loading: false, error: (error as Error).message });
    }
  },

  checkSoldOut: async (activityId: number) => {
    try {
      const res = await activityApi.checkSoldOut(activityId);
      const soldOut = res.data?.soldOut ?? false;
      set((state) => ({
        activities: state.activities.map((a) =>
          a.id === activityId ? { ...a, soldOut } : a
        ),
      }));
      return soldOut;
    } catch {
      return false;
    }
  },

  startActivity: (id: number) => {
    window.location.href = `/activity/${id}`;
  },
}));
