export interface DemoStatusCount {
  status: string;
  count: number;
}

export interface DemoMetrics {
  activityId: number;
  activityName: string;
  requestCount: number;
  pendingCount: number;
  unpaidCount: number;
  successCount: number;
  failedCount: number;
  timeoutCount: number;
  remainStock: number;
  totalStock: number;
  statusFlow: DemoStatusCount[];
}
