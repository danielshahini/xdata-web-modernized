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
    
    // Bei 401 (Unauthorized) loggen wir den User aus (Token abgelaufen)
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
    }
    
    // On 403 (Forbidden) we just re-throw the error so the component can catch it
    // A redirect here is often too aggressive and leads to infinite loops
    
    return Promise.reject(error);
  }
);

export default api;
