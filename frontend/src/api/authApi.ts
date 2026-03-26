import axios from 'axios';

const API_BASE = import.meta.env.VITE_ORDER_API_URL || '';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  password: string;
  email: string;
}

export interface FindUsernameRequest {
  email: string;
}

export interface ResetPasswordRequest {
  username: string;
  email: string;
  newPassword: string;
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

export const findUsername = async (request: FindUsernameRequest): Promise<{ username: string }> => {
  const response = await axios.post<{ username: string }>(`${API_BASE}/api/auth/find-username`, request);
  return response.data;
};

export const resetPassword = async (request: ResetPasswordRequest): Promise<{ message: string }> => {
  const response = await axios.post<{ message: string }>(`${API_BASE}/api/auth/reset-password`, request);
  return response.data;
};

export const kakaoLogin = async (code: string): Promise<AuthResponse> => {
  const response = await axios.post<AuthResponse>(`${API_BASE}/api/auth/kakao`, { code });
  return response.data;
};

export const KAKAO_CLIENT_ID = import.meta.env.VITE_KAKAO_CLIENT_ID || '';
export const KAKAO_REDIRECT_URI = import.meta.env.VITE_KAKAO_REDIRECT_URI || window.location.origin;

export const getKakaoLoginUrl = (): string => {
  return `https://kauth.kakao.com/oauth/authorize?client_id=${KAKAO_CLIENT_ID}&redirect_uri=${encodeURIComponent(KAKAO_REDIRECT_URI)}&response_type=code`;
};
