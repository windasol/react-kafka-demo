import axios from 'axios';

/** 모든 요청에 JWT 토큰을 자동으로 첨부하는 인터셉터 */
axios.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

/** 401 응답 시 토큰 제거 및 로그인 페이지로 리다이렉트 */
axios.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('username');
      window.location.reload();
    }
    return Promise.reject(error);
  }
);
