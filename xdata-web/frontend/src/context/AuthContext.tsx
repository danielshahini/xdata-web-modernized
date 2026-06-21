import React, { createContext, useContext, useState, useEffect } from 'react';
import { User } from '../types';

interface AuthContextType {
  user: User | null;
  token: string | null;
  login: (token: string, userData: any) => void;
  logout: () => void;
  isAuthenticated: boolean;
  isAdmin: boolean;
  isInstructor: boolean;
  isStudent: boolean;
  isTutor: boolean;
  isDark: boolean;
  toggleTheme: () => void;
  markPasswordChanged: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(() => {
    const savedUser = localStorage.getItem('user');
    if (savedUser) {
      try {
        const parsed = JSON.parse(savedUser);
        return parsed.user || parsed;
      } catch (e) {
        console.error("Error parsing user from localStorage", e);
        return null;
      }
    }
    return null;
  });
  const [token, setToken] = useState<string | null>(() => localStorage.getItem('token'));
  
  const [isDark, setIsDark] = useState(() => {
    const saved = localStorage.getItem('theme');
    return saved === 'dark' || (!saved && window.matchMedia('(prefers-color-scheme: dark)').matches);
  });

  useEffect(() => {
    if (isDark) {
      document.documentElement.classList.add('dark');
      localStorage.setItem('theme', 'dark');
    } else {
      document.documentElement.classList.remove('dark');
      localStorage.setItem('theme', 'light');
    }
  }, [isDark]);

  const toggleTheme = () => setIsDark(!isDark);

  const login = (newToken: string, loginResponse: any) => {
    setToken(newToken);
    const userData = loginResponse.user || loginResponse;
    setUser(userData);
    localStorage.setItem('token', newToken);
    localStorage.setItem('user', JSON.stringify(loginResponse));
  };

  const logout = () => {
    setToken(null);
    setUser(null);
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  };

  const markPasswordChanged = () => {
    setUser(prev => {
      if (!prev) return prev;
      const updated = { ...prev, mustChangePassword: false };
      try {
        const saved = JSON.parse(localStorage.getItem('user') || '{}');
        localStorage.setItem('user', JSON.stringify({ ...saved, mustChangePassword: false }));
      } catch { /* ignore */ }
      return updated;
    });
  };

  const isAdmin = user?.role?.trim().toUpperCase() === 'ADMIN';
  const isInstructor = user?.role?.trim().toUpperCase() === 'INSTRUCTOR';
  const isStudent = user?.role?.trim().toUpperCase() === 'STUDENT';
  const isTutor = user?.role?.trim().toUpperCase() === 'TUTOR';

  return (
    <AuthContext.Provider value={{ 
      user, 
      token, 
      login, 
      logout, 
      isAuthenticated: !!token,
      isAdmin,
      isInstructor,
      isStudent,
      isTutor,
      isDark,
      toggleTheme,
      markPasswordChanged
    }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
