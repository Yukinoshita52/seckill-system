import apiClient from './index';
import { PlaceOrderRequest, PlaceOrderResponse, OrderStatusResponse } from '../types/order';
import { ApiResponse } from '../types/api';

export const orderApi = {
  placeOrder(data: PlaceOrderRequest): Promise<ApiResponse<PlaceOrderResponse>> {
    return apiClient.post('/seckill/order', data);
  },

  getOrderStatus(orderNo: string): Promise<ApiResponse<OrderStatusResponse>> {
    return apiClient.get(`/order/status/${orderNo}`);
  },
};
