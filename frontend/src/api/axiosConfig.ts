import axios from 'axios';

/** 모든 요청에 쿠키 자동 첨부 (HttpOnly access_token 쿠키 사용) */
axios.defaults.withCredentials = true;

/** 401 응답 시 logout API로 쿠키 정리 후 새로고침 */
axios.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401) {
      try {
        await axios.post(`${import.meta.env.VITE_AUTH_API_URL}/api/auth/logout`);
      } catch {
        // logout 실패해도 페이지 새로고침으로 상태 초기화
      }
      window.location.reload();
    }
    return Promise.reject(error);
  }
);
