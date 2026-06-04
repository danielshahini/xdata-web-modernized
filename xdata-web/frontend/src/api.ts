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
    
    // Bei 403 (Forbidden) werfen wir den Fehler nur weiter, damit die Komponente ihn fangen kann
    // Ein Redirect an dieser Stelle ist oft zu aggressiv und führt zu Endlosschleifen
    
    return Promise.reject(error);
  }
);

export default api;
