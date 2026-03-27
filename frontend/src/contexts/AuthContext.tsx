import { createContext, useContext, useState, useCallback, useEffect, type ReactNode } from 'react';
import { login as apiLogin, register as apiRegister } from '../api/authApi';
import type { LoginRequest, RegisterRequest } from '../api/authApi';

type AuthPage = 'login' | 'register' | 'find-account';

interface AuthContextType {
  isLoggedIn: boolean;
  username: string | null;
  authPage: AuthPage;
  setAuthPage: (page: AuthPage) => void;
  login: (request: LoginRequest) => Promise<void>;
  register: (request: RegisterRequest) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [username, setUsername] = useState<string | null>(() => localStorage.getItem('username'));
  const [authPage, setAuthPage] = useState<AuthPage>('login');
  const isLoggedIn = username !== null;

  // 카카오 OAuth2 콜백: URL 쿼리 파라미터에서 토큰 추출
  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const kakaoToken = params.get('kakaoToken');
    const kakaoUsername = params.get('kakaoUsername');
    if (kakaoToken && kakaoUsername) {
      localStorage.setItem('token', kakaoToken);
      localStorage.setItem('username', kakaoUsername);
      setUsername(kakaoUsername);
      window.history.replaceState({}, '', window.location.pathname);
    }
  }, []);

  const login = useCallback(async (request: LoginRequest) => {
    const response = await apiLogin(request);
    localStorage.setItem('token', response.token);
    localStorage.setItem('username', response.username);
    setUsername(response.username);
  }, []);

  const register = useCallback(async (request: RegisterRequest) => {
    const response = await apiRegister(request);
    localStorage.setItem('token', response.token);
    localStorage.setItem('username', response.username);
    setUsername(response.username);
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    setUsername(null);
    setAuthPage('login');
  }, []);

  return (
    <AuthContext.Provider value={{ isLoggedIn, username, authPage, setAuthPage, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth(): AuthContextType {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return context;
}
