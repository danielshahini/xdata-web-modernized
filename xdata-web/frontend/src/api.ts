import axios from 'axios';

export const BASE_URL = `${window.location.protocol}//${window.location.hostname}:8080`;
const API_URL = `${BASE_URL}/api/v1`;

const api = axios.create({
  baseURL: API_URL,
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    console.error('API Error:', error.response?.status, error.response?.data);
    if (error.response && (error.response.status === 401)) {
      // Bei 401 (Unauthorized) oder 403 (Forbidden) loggen wir den User sicherheitshalber aus
      // Da ein 403 oft bedeutet, dass das Token ungültig wurde (durch Backend-Neustart)
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/';
    }
    return Promise.reject(error);
  }
);

export default api;
