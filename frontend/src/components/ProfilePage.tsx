import { useEffect, useState } from 'react';
import { fetchProfile, changePassword } from '../api/authApi';
import type { AxiosError } from 'axios';

interface ProfilePageProps {
  onClose: () => void;
}

interface ProfileData {
  username: string;
  email: string;
}

export default function ProfilePage({ onClose }: ProfilePageProps) {
  const [profile, setProfile] = useState<ProfileData | null>(null);
  const [profileError, setProfileError] = useState<string | null>(null);
  const [isProfileLoading, setIsProfileLoading] = useState(true);

  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitSuccess, setSubmitSuccess] = useState<string | null>(null);
  const [submitError, setSubmitError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;

    const loadProfile = async () => {
      setIsProfileLoading(true);
      setProfileError(null);
      try {
        const data = await fetchProfile();
        if (!cancelled) {
          setProfile(data);
        }
      } catch (err) {
        if (!cancelled) {
          const axiosErr = err as AxiosError<{ message?: string }>;
          const message = axiosErr.response?.data?.message ?? '프로필을 불러오는 중 오류가 발생했습니다.';
          setProfileError(message);
        }
      } finally {
        if (!cancelled) {
          setIsProfileLoading(false);
        }
      }
    };

    loadProfile();

    return () => {
      cancelled = true;
    };
  }, []);

  const handleChangePassword = async (e: React.FormEvent) => {
    e.preventDefault();
    if (isSubmitting) return;

    setIsSubmitting(true);
    setSubmitSuccess(null);
    setSubmitError(null);

    try {
      await changePassword(currentPassword, newPassword);
      setSubmitSuccess('비밀번호가 성공적으로 변경되었습니다.');
      setCurrentPassword('');
      setNewPassword('');
    } catch (err) {
      const axiosErr = err as AxiosError<{ message?: string }>;
      const message = axiosErr.response?.data?.message ?? '일시적 오류가 발생했습니다. 다시 시도해주세요.';
      setSubmitError(message);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="profile-page">
      <div className="profile-header">
        <h1>사용자 프로필</h1>
        <button className="close-btn" onClick={onClose}>← 돌아가기</button>
      </div>

      {isProfileLoading && <p className="loading-message">프로필 불러오는 중...</p>}
      {profileError && <p className="error-message">{profileError}</p>}

      {profile && (
        <div className="profile-info">
          <div className="field">
            <span className="field-label">아이디</span>
            <span className="field-value">{profile.username}</span>
          </div>
          <div className="field">
            <span className="field-label">이메일</span>
            <span className="field-value">{profile.email}</span>
          </div>
        </div>
      )}

      <div className="change-password-form">
        <h2>비밀번호 변경</h2>
        <form onSubmit={handleChangePassword}>
          <div className="form-group">
            <label htmlFor="currentPassword">현재 비밀번호</label>
            <input
              id="currentPassword"
              type="password"
              value={currentPassword}
              onChange={(e) => setCurrentPassword(e.target.value)}
              required
              disabled={isSubmitting}
            />
          </div>
          <div className="form-group">
            <label htmlFor="newPassword">새 비밀번호</label>
            <input
              id="newPassword"
              type="password"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              required
              disabled={isSubmitting}
            />
          </div>
          {submitSuccess && <p className="success-message">{submitSuccess}</p>}
          {submitError && <p className="error-message">{submitError}</p>}
          <button type="submit" disabled={isSubmitting}>
            {isSubmitting ? '변경 중...' : '비밀번호 변경'}
          </button>
        </form>
      </div>
    </div>
  );
}
