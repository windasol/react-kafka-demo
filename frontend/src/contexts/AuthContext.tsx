import { createContext, useContext, useState, useCallback, type ReactNode } from 'react';
import axios from 'axios';
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
  logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | null>(null);

/** 서버가 설정한 non-HttpOnly username 쿠키에서 사용자명 읽기 */
function getUsernameFromCookie(): string | null {
  const match = document.cookie.match(/(?:^|;\s*)username=([^;]+)/);
  return match ? decodeURIComponent(match[1]) : null;
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [username, setUsername] = useState<string | null>(() => getUsernameFromCookie());
  const [authPage, setAuthPage] = useState<AuthPage>('login');

  const isLoggedIn = username !== null;

  const login = useCallback(async (request: LoginRequest) => {
    const response = await apiLogin(request);
    // 쿠키는 서버가 Set-Cookie로 자동 설정, 여기서는 상태만 갱신
    setUsername(response.username);
  }, []);

  const register = useCallback(async (request: RegisterRequest) => {
    const response = await apiRegister(request);
    setUsername(response.username);
  }, []);

  const logout = useCallback(async () => {
    await axios.post(`${import.meta.env.VITE_AUTH_API_URL}/api/auth/logout`);
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
