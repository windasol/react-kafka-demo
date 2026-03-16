import axios from 'axios';
import { Order } from '../types';

const API_BASE = import.meta.env.VITE_ORDER_API_URL || '';

export const createOrder = async (order: Pick<Order, 'productName' | 'quantity'>): Promise<Order> => {
  const response = await axios.post<Order>(`${API_BASE}/api/orders`, order);
  return response.data;
};

export const fetchOrders = async (): Promise<Order[]> => {
  const response = await axios.get<Order[]>(`${API_BASE}/api/orders`);
  return response.data;
};
