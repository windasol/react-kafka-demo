import { useState } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { findUsername, resetPassword } from '../api/authApi';
import './LoginPage.css';

type Tab = 'find-id' | 'reset-pw';

function FindAccountPage() {
  const { setAuthPage } = useAuth();
  const [activeTab, setActiveTab] = useState<Tab>('find-id');

  return (
    <div className="login-page">
      <div className="login-card">
        <h1 className="login-title">계정 찾기</h1>
        <p className="login-subtitle">아이디 또는 비밀번호를 찾아보세요</p>

        <div className="tab-group">
          <button
            className={`tab-btn ${activeTab === 'find-id' ? 'active' : ''}`}
            onClick={() => setActiveTab('find-id')}
          >
            아이디 찾기
          </button>
          <button
            className={`tab-btn ${activeTab === 'reset-pw' ? 'active' : ''}`}
            onClick={() => setActiveTab('reset-pw')}
          >
            비밀번호 재설정
          </button>
        </div>

        {activeTab === 'find-id' ? <FindIdForm /> : <ResetPwForm />}

        <button className="back-btn" onClick={() => setAuthPage('login')}>
          로그인으로 돌아가기
        </button>
      </div>
    </div>
  );
}

function FindIdForm() {
  const [email, setEmail] = useState('');
  const [error, setError] = useState('');
  const [foundUsername, setFoundUsername] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setFoundUsername('');
    setLoading(true);

    try {
      const result = await findUsername({ email });
      setFoundUsername(result.username);
    } catch (err: unknown) {
      const axiosError = err as { response?: { data?: { error?: string } } };
      setError(axiosError.response?.data?.error || '아이디 찾기에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <form className="login-form" onSubmit={handleSubmit}>
      <div className="form-group">
        <label htmlFor="find-email">가입 시 등록한 이메일</label>
        <input
          id="find-email"
          type="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          placeholder="example@email.com"
          required
          autoFocus
        />
      </div>

      {error && <p className="login-error">{error}</p>}
      {foundUsername && (
        <div className="result-box">
          회원님의 아이디는 <strong>{foundUsername}</strong> 입니다.
        </div>
      )}

      <button type="submit" className="login-btn" disabled={loading}>
        {loading ? '조회중...' : '아이디 찾기'}
      </button>
    </form>
  );
}

function ResetPwForm() {
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [newPasswordConfirm, setNewPasswordConfirm] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    if (newPassword !== newPasswordConfirm) {
      setError('새 비밀번호가 일치하지 않습니다.');
      return;
    }

    setLoading(true);
    try {
      const result = await resetPassword({ username, email, newPassword });
      setSuccess(result.message);
    } catch (err: unknown) {
      const axiosError = err as { response?: { data?: { error?: string } } };
      setError(axiosError.response?.data?.error || '비밀번호 재설정에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <form className="login-form" onSubmit={handleSubmit}>
      <div className="form-group">
        <label htmlFor="reset-username">사용자명</label>
        <input
          id="reset-username"
          type="text"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          placeholder="사용자명 입력"
          required
          autoFocus
        />
      </div>

      <div className="form-group">
        <label htmlFor="reset-email">가입 시 등록한 이메일</label>
        <input
          id="reset-email"
          type="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          placeholder="example@email.com"
          required
        />
      </div>

      <div className="form-group">
        <label htmlFor="reset-new-pw">새 비밀번호</label>
        <input
          id="reset-new-pw"
          type="password"
          value={newPassword}
          onChange={(e) => setNewPassword(e.target.value)}
          placeholder="4자 이상"
          required
          minLength={4}
        />
      </div>

      <div className="form-group">
        <label htmlFor="reset-new-pw-confirm">새 비밀번호 확인</label>
        <input
          id="reset-new-pw-confirm"
          type="password"
          value={newPasswordConfirm}
          onChange={(e) => setNewPasswordConfirm(e.target.value)}
          placeholder="새 비밀번호 재입력"
          required
          minLength={4}
        />
      </div>

      {error && <p className="login-error">{error}</p>}
      {success && <p className="login-success">{success}</p>}

      <button type="submit" className="login-btn" disabled={loading}>
        {loading ? '처리중...' : '비밀번호 재설정'}
      </button>
    </form>
  );
}

export default FindAccountPage;
