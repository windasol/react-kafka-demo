import axios from 'axios';

const API_BASE = import.meta.env.VITE_ORDER_API_URL || '';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  username: string;
}

export const login = async (request: LoginRequest): Promise<AuthResponse> => {
  const response = await axios.post<AuthResponse>(`${API_BASE}/api/auth/login`, request);
  return response.data;
};

export const register = async (request: RegisterRequest): Promise<AuthResponse> => {
  const response = await axios.post<AuthResponse>(`${API_BASE}/api/auth/register`, request);
  return response.data;
};
