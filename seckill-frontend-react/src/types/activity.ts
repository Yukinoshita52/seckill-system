export interface Activity {
  id: number;
  activityName: string;
  goodsName: string;
  originalPrice: number;
  seckillPrice: number;
  totalStock: number;
  soldOut: boolean;
  startTime: string;
  endTime: string;
  /** 0-未开始 1-进行中 2-已售罄 3-已结束 */
  status: number;
}

export interface ActivityCreateRequest {
  activityName: string;
  goodsId: number;
  goodsName: string;
  originalPrice: number;
  seckillPrice: number;
  totalStock: number;
  bucketCount: number;
  startTime: string;
  endTime: string;
}
