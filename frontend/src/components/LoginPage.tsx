import { useState } from 'react';
import { useAuth } from '../contexts/AuthContext';
import './LoginPage.css';

function LoginPage() {
  const { login, setAuthPage } = useAuth();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      await login({ username, password });
    } catch (err: unknown) {
      const axiosError = err as { response?: { data?: { error?: string } } };
      setError(axiosError.response?.data?.error || '로그인에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-page">
      <div className="login-card">
        <h1 className="login-title">로그인</h1>
        <p className="login-subtitle">주문 / 알림 시스템</p>

        <form className="login-form" onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="username">사용자명</label>
            <input
              id="username"
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              placeholder="사용자명 입력"
              required
              minLength={3}
              autoFocus
            />
          </div>

          <div className="form-group">
            <label htmlFor="password">비밀번호</label>
            <input
              id="password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="비밀번호 입력"
              required
              minLength={4}
            />
          </div>

          {error && <p className="login-error">{error}</p>}

          <button type="submit" className="login-btn" disabled={loading}>
            {loading ? '처리중...' : '로그인'}
          </button>
        </form>

        <div className="kakao-divider">
          <span>또는</span>
        </div>

        <a
          href={`${import.meta.env.VITE_ORDER_API_URL || ''}/oauth2/authorization/kakao`}
          className="kakao-btn"
        >
          <svg className="kakao-icon" viewBox="0 0 24 24" fill="currentColor">
            <path d="M12 3C6.477 3 2 6.477 2 10.5c0 2.632 1.568 4.942 3.938 6.322L5 20l3.938-2.053C10.2 18.3 11.086 18.5 12 18.5c5.523 0 10-3.477 10-7.5S17.523 3 12 3z" />
          </svg>
          카카오로 로그인
        </a>

        <div className="auth-links">
          <button className="link-btn" onClick={() => setAuthPage('find-account')}>
            아이디 / 비밀번호 찾기
          </button>
          <button className="link-btn" onClick={() => setAuthPage('register')}>
            회원가입
          </button>
        </div>
      </div>
    </div>
  );
}

export default LoginPage;
