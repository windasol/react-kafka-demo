import { useState } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { getKakaoLoginUrl, KAKAO_CLIENT_ID } from '../api/authApi';
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

        {KAKAO_CLIENT_ID && (
          <div className="social-login">
            <div className="divider"><span>또는</span></div>
            <button
              className="kakao-login-btn"
              onClick={() => { window.location.href = getKakaoLoginUrl(); }}
            >
              <svg className="kakao-icon" viewBox="0 0 24 24" width="20" height="20">
                <path d="M12 3C6.48 3 2 6.36 2 10.44c0 2.61 1.74 4.91 4.36 6.22l-1.1 4.07c-.1.35.31.64.62.44l4.83-3.2c.42.04.85.07 1.29.07 5.52 0 10-3.36 10-7.6C22 6.36 17.52 3 12 3z" fill="#3C1E1E"/>
              </svg>
              카카오 로그인
            </button>
          </div>
        )}

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
