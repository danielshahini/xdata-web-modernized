import React, { useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { Database, User as UserIcon, LogOut, Sun, Moon, LayoutDashboard, Trophy, Menu, X, Beaker, FlaskConical } from 'lucide-react';

const Layout: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { user, logout, isDark, toggleTheme } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [isMenuOpen, setIsMenuOpen] = useState(false);

  const isActive = (path: string) => location.pathname === path;

  const getLinkClasses = (path: string) => {
    const baseClasses = "flex items-center space-x-2 px-4 py-2 rounded-lg text-sm font-medium transition-colors";
    const activeClasses = "bg-blue-50 text-blue-600 dark:bg-blue-900/30 dark:text-blue-400";
    const inactiveClasses = "text-gray-600 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700";
    
    return `${baseClasses} ${isActive(path) ? activeClasses : inactiveClasses}`;
  };

  const getMobileLinkClasses = (path: string) => {
    const baseClasses = "flex items-center space-x-3 p-3 rounded-xl transition-colors font-bold";
    const activeClasses = "bg-blue-50 text-blue-600 dark:bg-blue-900/30 dark:text-blue-400";
    const inactiveClasses = "text-gray-700 dark:text-gray-200 hover:bg-gray-50 dark:hover:bg-gray-700";
    
    return `${baseClasses} ${isActive(path) ? activeClasses : inactiveClasses}`;
  };


  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900 transition-colors duration-300 flex flex-col">
      <nav className="bg-white dark:bg-gray-800 shadow-sm border-b border-gray-200 dark:border-gray-700 p-4 flex justify-between items-center sticky top-0 z-50 transition-colors">
        <div className="flex items-center space-x-6">
          <Link to="/" className="flex items-center space-x-2 group">
            <div className="bg-blue-600 p-1.5 rounded-lg group-hover:bg-blue-700 transition-colors">
              <Database className="text-white" size={20} />
            </div>
            <h1 className="text-xl font-black text-gray-800 dark:text-white tracking-tight">
              XData <span className="text-blue-600">Web</span>
            </h1>
          </Link>
          
          <div className="hidden md:flex space-x-1">
            <Link to="/" className={getLinkClasses("/")}>
              <LayoutDashboard size={16} />
              <span>Dashboard</span>
            </Link>
            <Link to="/playground" className={getLinkClasses("/playground")}>
              <Beaker size={16} />
              <span>SQL Diagnose Labor</span>
            </Link>
            {(user?.role === 'ADMIN' || user?.role === 'INSTRUCTOR') && (
              <Link to="/dataset-playground" className={getLinkClasses("/dataset-playground")}>
                <FlaskConical size={16} />
                <span>Dataset Playground</span>
              </Link>
            )}
            {user?.role !== 'ADMIN' && (
              <Link to="/leaderboard" className={getLinkClasses("/leaderboard")}>
                <Trophy size={16} />
                <span>Bestenliste</span>
              </Link>
            )}
          </div>
        </div>

        <div className="flex items-center space-x-3">
          <button 
            onClick={toggleTheme}
            className="p-2 rounded-full text-gray-500 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors hidden sm:block"
            title={isDark ? "Hellmodus" : "Dunkelmodus"}
          >
            {isDark ? <Sun size={20} /> : <Moon size={20} />}
          </button>

          <div className="hidden sm:flex items-center bg-gray-50 dark:bg-gray-700/50 px-4 py-2 rounded-full border border-gray-100 dark:border-gray-700 transition-colors">
            <UserIcon className="text-gray-400 mr-2" size={20} />
            <div className="flex flex-col">
              <span className="text-sm font-bold text-gray-700 dark:text-gray-200 leading-none">{user?.username || 'Benutzer'}</span>
              <span className="text-[10px] uppercase font-bold text-gray-400 dark:text-gray-500 mt-1 tracking-wider">{user?.role}</span>
            </div>
            <div className="h-4 w-[1px] bg-gray-200 dark:bg-gray-600 mx-3"></div>
            <button 
              onClick={handleLogout}
              className="text-gray-400 hover:text-red-600 transition-colors"
              title="Abmelden"
            >
              <LogOut size={18} />
            </button>
          </div>

          <button 
            onClick={() => setIsMenuOpen(!isMenuOpen)}
            className="md:hidden p-2 rounded-lg text-gray-500 hover:bg-gray-100 dark:hover:bg-gray-700"
          >
            {isMenuOpen ? <X size={24} /> : <Menu size={24} />}
          </button>
        </div>
      </nav>

      {/* Mobiles Menü */}
      {isMenuOpen && (
        <div className="md:hidden bg-white dark:bg-gray-800 border-b border-gray-200 dark:border-gray-700 p-4 space-y-2 animate-fadeIn">
          <Link 
            to="/" 
            className={getMobileLinkClasses("/")}
            onClick={() => setIsMenuOpen(false)}
          >
            <LayoutDashboard size={20} className={isActive("/") ? "" : "text-blue-500"} />
            <span>Dashboard</span>
          </Link>
          <Link 
            to="/playground" 
            className={getMobileLinkClasses("/playground")}
            onClick={() => setIsMenuOpen(false)}
          >
            <Beaker size={20} className={isActive("/playground") ? "" : "text-blue-500"} />
            <span>SQL Diagnose Labor</span>
          </Link>
          {(user?.role === 'ADMIN' || user?.role === 'INSTRUCTOR') && (
            <Link 
              to="/dataset-playground" 
              className={getMobileLinkClasses("/dataset-playground")}
              onClick={() => setIsMenuOpen(false)}
            >
              <FlaskConical size={20} className={isActive("/dataset-playground") ? "" : "text-purple-500"} />
              <span>Dataset Playground</span>
            </Link>
          )}
          {user?.role !== 'ADMIN' && (
            <Link 
              to="/leaderboard" 
              className={getMobileLinkClasses("/leaderboard")}
              onClick={() => setIsMenuOpen(false)}
            >
              <Trophy size={20} className={isActive("/leaderboard") ? "" : "text-yellow-500"} />
              <span>Bestenliste</span>
            </Link>
          )}
          <div className="h-[1px] bg-gray-100 dark:bg-gray-700 my-2"></div>
          <button 
            onClick={handleLogout}
            className="w-full flex items-center space-x-3 p-3 rounded-xl text-red-500 hover:bg-red-50 dark:hover:bg-red-900/20 transition-colors font-bold"
          >
            <LogOut size={20} />
            <span>Abmelden</span>
          </button>
        </div>
      )}
      
      <main className="flex-grow container mx-auto px-4 py-8">
        <div className="animate-fadeIn">
          {children}
        </div>
      </main>

      <footer className="bg-white dark:bg-gray-800 border-t border-gray-100 dark:border-gray-700 py-6 text-center text-gray-400 dark:text-gray-500 text-sm transition-colors">
        &copy; {new Date().getFullYear()} XData IIT Bombay - Automated SQL Grading System
      </footer>
    </div>
  );
};

export default Layout;
