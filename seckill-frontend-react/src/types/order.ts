export interface OrderStatusResponse {
  orderNo: string;
  /** PENDING | UNPAID | SUCCESS | FAILED | TIMEOUT */
  status: string;
  seckillPrice: number;
  goodsName: string;
}

export interface PlaceOrderRequest {
  activityId: number;
  captchaToken?: string;
  captchaCode?: string;
}

export interface PlaceOrderResponse {
  orderNo: string;
  status: string;
  message: string;
}
