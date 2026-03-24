import axios from 'axios';
import type { Product } from '../types';

const API_BASE = import.meta.env.VITE_ORDER_API_URL || '';

export const fetchProducts = async (): Promise<Product[]> => {
  const response = await axios.get<Product[]>(`${API_BASE}/api/products`);
  return response.data;
};

export const createProduct = async (product: Pick<Product, 'name' | 'price' | 'stock'>): Promise<Product> => {
  const response = await axios.post<Product>(`${API_BASE}/api/products`, product);
  return response.data;
};

export const updateProduct = async (id: number, product: Pick<Product, 'name' | 'price' | 'stock'>): Promise<Product> => {
  const response = await axios.put<Product>(`${API_BASE}/api/products/${id}`, product);
  return response.data;
};

export const deleteProduct = async (id: number): Promise<void> => {
  await axios.delete(`${API_BASE}/api/products/${id}`);
};
