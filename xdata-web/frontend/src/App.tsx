import React, { Suspense, lazy } from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import Login from './components/Login';
import ProtectedRoute from './components/common/ProtectedRoute';
import Layout from './components/common/Layout';
import { useAuth } from './context/AuthContext';

// Lazy load components that use Monaco Editor to prevent conflicts during login/navigation
const StudentDashboard = lazy(() => import('./components/StudentDashboard'));
const InstructorDashboard = lazy(() => import('./components/InstructorDashboard'));
const AdminDashboard = lazy(() => import('./components/AdminDashboard'));
const SqlLab = lazy(() => import('./components/SqlLab'));
const Leaderboard = lazy(() => import('./components/Leaderboard'));

const LoadingFallback = () => (
  <div className="min-h-screen flex items-center justify-center bg-gray-50 dark:bg-gray-900">
    <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
  </div>
);

const App: React.FC = () => {
  const { user, isAuthenticated } = useAuth();

  const getDefaultRoute = () => {
    if (!user) return <Navigate to="/login" replace />;
    
    switch (user.role) {
      case 'ADMIN': return <Navigate to="/admin" replace />;
      case 'INSTRUCTOR': return <Navigate to="/instructor" replace />;
      default: return <Navigate to="/student" replace />;
    }
  };

  return (
    <Suspense fallback={<LoadingFallback />}>
      <Routes>
        <Route path="/login" element={!isAuthenticated ? <Login /> : <Navigate to="/" replace />} />
        
        <Route path="/admin/*" element={
          <ProtectedRoute allowedRoles={['ADMIN']}>
            <Layout><AdminDashboard /></Layout>
          </ProtectedRoute>
        } />
        
        <Route path="/instructor/*" element={
          <ProtectedRoute allowedRoles={['INSTRUCTOR']}>
            <Layout><InstructorDashboard /></Layout>
          </ProtectedRoute>
        } />
        
        <Route path="/student/*" element={
          <ProtectedRoute allowedRoles={['STUDENT']}>
            <Layout><StudentDashboard /></Layout>
          </ProtectedRoute>
        } />

        <Route path="/playground" element={
          <ProtectedRoute>
            <Layout><SqlLab /></Layout>
          </ProtectedRoute>
        } />

        <Route path="/leaderboard" element={
          <ProtectedRoute allowedRoles={['STUDENT', 'INSTRUCTOR']}>
            <Layout><Leaderboard /></Layout>
          </ProtectedRoute>
        } />

        <Route path="/" element={isAuthenticated ? getDefaultRoute() : <Navigate to="/login" replace />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </Suspense>
  );
}

export default App;
