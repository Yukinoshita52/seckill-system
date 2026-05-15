export interface DemoRunRequest {
  activityId: number;
  requestCount: number;
}

export interface DemoRunResponse {
  activityId: number;
  requestCount: number;
  acceptedCount: number;
  rejectedCount: number;
  message: string;
}
