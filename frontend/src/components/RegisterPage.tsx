import { useState } from 'react';
import { useAuth } from '../contexts/AuthContext';
import './LoginPage.css';

function RegisterPage() {
  const { register, setAuthPage } = useAuth();
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [passwordConfirm, setPasswordConfirm] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    if (password !== passwordConfirm) {
      setError('비밀번호가 일치하지 않습니다.');
      return;
    }

    setLoading(true);
    try {
      await register({ username, password, email });
    } catch (err: unknown) {
      const axiosError = err as { response?: { data?: { error?: string } } };
      setError(axiosError.response?.data?.error || '회원가입에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-page">
      <div className="login-card">
        <h1 className="login-title">회원가입</h1>
        <p className="login-subtitle">새 계정을 만들어주세요</p>

        <form className="login-form" onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="reg-username">사용자명</label>
            <input
              id="reg-username"
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              placeholder="3~20자"
              required
              minLength={3}
              maxLength={20}
              autoFocus
            />
          </div>

          <div className="form-group">
            <label htmlFor="reg-email">이메일</label>
            <input
              id="reg-email"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="example@email.com"
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="reg-password">비밀번호</label>
            <input
              id="reg-password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="4자 이상"
              required
              minLength={4}
            />
          </div>

          <div className="form-group">
            <label htmlFor="reg-password-confirm">비밀번호 확인</label>
            <input
              id="reg-password-confirm"
              type="password"
              value={passwordConfirm}
              onChange={(e) => setPasswordConfirm(e.target.value)}
              placeholder="비밀번호 재입력"
              required
              minLength={4}
            />
          </div>

          {error && <p className="login-error">{error}</p>}

          <button type="submit" className="login-btn" disabled={loading}>
            {loading ? '처리중...' : '회원가입'}
          </button>
        </form>

        <button className="back-btn" onClick={() => setAuthPage('login')}>
          로그인으로 돌아가기
        </button>
      </div>
    </div>
  );
}

export default RegisterPage;
