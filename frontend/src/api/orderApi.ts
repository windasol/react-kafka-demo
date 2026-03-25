import axios from 'axios';
import type { Order, OrderStatus, CursorPage } from '../types';

const API_BASE = import.meta.env.VITE_ORDER_API_URL || '';

export const createOrder = async (order: { productId: number; quantity: number }): Promise<Order> => {
  const response = await axios.post<Order>(`${API_BASE}/api/orders`, order);
  return response.data;
};

export const fetchOrders = async (): Promise<Order[]> => {
  const response = await axios.get<Order[]>(`${API_BASE}/api/orders`);
  return response.data;
};

export const fetchOrder = async (orderId: number): Promise<Order> => {
  const response = await axios.get<Order>(`${API_BASE}/api/orders/${orderId}`);
  return response.data;
};

export const fetchOrdersPaged = async (cursor?: number, size = 7): Promise<CursorPage<Order>> => {
  const params = new URLSearchParams({ paged: 'true', size: String(size) });
  if (cursor != null) params.set('cursor', String(cursor));
  const response = await axios.get<CursorPage<Order>>(`${API_BASE}/api/orders?${params}`);
  return response.data;
};

export const changeOrderStatus = async (orderId: number, status: OrderStatus): Promise<Order> => {
  const response = await axios.patch<Order>(`${API_BASE}/api/orders/${orderId}/status`, { status });
  return response.data;
};

export const cancelOrder = async (orderId: number): Promise<Order> => {
  const response = await axios.patch<Order>(`${API_BASE}/api/orders/${orderId}/cancel`);
  return response.data;
};

export interface OrderSearchParams {
  cursor?: number;
  size?: number;
  keyword?: string;
  status?: OrderStatus;
  dateFrom?: string;
  dateTo?: string;
}

export const searchOrders = async (params: OrderSearchParams): Promise<CursorPage<Order>> => {
  const query = new URLSearchParams({ search: 'true' });
  if (params.cursor != null) query.set('cursor', String(params.cursor));
  if (params.size) query.set('size', String(params.size));
  if (params.keyword) query.set('keyword', params.keyword);
  if (params.status) query.set('status', params.status);
  if (params.dateFrom) query.set('dateFrom', params.dateFrom);
  if (params.dateTo) query.set('dateTo', params.dateTo);
  const response = await axios.get<CursorPage<Order>>(`${API_BASE}/api/orders?${query}`);
  return response.data;
};
