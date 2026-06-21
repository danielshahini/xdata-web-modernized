import React, { useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { Database, User as UserIcon, LogOut, Sun, Moon, LayoutDashboard, Menu, X, Beaker, FlaskConical, KeyRound } from 'lucide-react';
import ChangePasswordModal from './ChangePasswordModal';

type NavItem = { to: string; label: string; icon: React.ReactNode; show: boolean };

const Layout: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { user, logout, isDark, toggleTheme, isAdmin, isInstructor } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  const [showChangePw, setShowChangePw] = useState(false);

  const isActive = (path: string) => location.pathname === path;

  const navItems: NavItem[] = [
    { to: '/', label: 'Dashboard', icon: <LayoutDashboard size={16} />, show: true },
    { to: '/playground', label: 'SQL-Labor', icon: <Beaker size={16} />, show: isAdmin || isInstructor },
    { to: '/dataset-playground', label: 'Dataset-Playground', icon: <FlaskConical size={16} />, show: isAdmin || isInstructor },
  ];

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const desktopLink = (path: string) =>
    `relative flex items-center gap-2 px-3.5 py-2 rounded-lg text-sm font-medium transition-colors ${
      isActive(path)
        ? 'text-brand-600 dark:text-brand-300'
        : 'text-slate-500 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-100 hover:bg-slate-100/70 dark:hover:bg-ink-soft'
    }`;

  return (
    <div className="min-h-screen bg-slate-50 dark:bg-ink-bg transition-colors duration-300 flex flex-col">
      <nav className="sticky top-0 z-50 bg-white/80 dark:bg-ink-card/80 backdrop-blur-md border-b border-slate-200 dark:border-ink-border">
        <div className="container mx-auto px-4 h-16 flex justify-between items-center">
          <div className="flex items-center gap-7">
            <Link to="/" className="flex items-center gap-2.5 group">
              <div className="relative grid place-items-center h-9 w-9 rounded-xl bg-brand-600 text-white shadow-glow group-hover:scale-105 transition-transform">
                <Database size={18} strokeWidth={2.4} />
              </div>
              <div className="leading-none">
                <span className="font-display text-lg font-extrabold tracking-tight text-slate-900 dark:text-white">
                  XData
                </span>
                <span className="ml-1 font-mono text-xs font-semibold text-brand-500">/sql</span>
              </div>
            </Link>

            <div className="hidden md:flex items-center gap-1">
              {navItems.filter((i) => i.show).map((item) => (
                <Link key={item.to} to={item.to} className={desktopLink(item.to)}>
                  {item.icon}
                  <span>{item.label}</span>
                  {isActive(item.to) && (
                    <span className="absolute -bottom-[17px] left-3 right-3 h-0.5 rounded-full bg-brand-600 dark:bg-brand-400" />
                  )}
                </Link>
              ))}
            </div>
          </div>

          <div className="flex items-center gap-2">
            <button
              onClick={toggleTheme}
              className="grid place-items-center h-9 w-9 rounded-lg text-slate-500 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-ink-soft transition-colors"
              title={isDark ? 'Hellmodus' : 'Dunkelmodus'}
              aria-label="Theme umschalten"
            >
              {isDark ? <Sun size={18} /> : <Moon size={18} />}
            </button>

            <div className="hidden sm:flex items-center gap-3 pl-1.5 pr-1.5 py-1.5 rounded-xl bg-slate-100/70 dark:bg-ink-soft border border-slate-200/60 dark:border-ink-border">
              <div className="grid place-items-center h-7 w-7 rounded-lg bg-white dark:bg-ink-card text-brand-600 dark:text-brand-300 ring-1 ring-slate-200 dark:ring-ink-border">
                <UserIcon size={15} />
              </div>
              <div className="flex flex-col pr-1">
                <span className="text-sm font-semibold text-slate-800 dark:text-slate-100 leading-none">{user?.username || 'Benutzer'}</span>
                <span className="mt-1 font-mono text-[10px] uppercase font-semibold text-slate-400 dark:text-slate-500 tracking-wider leading-none">{user?.role}</span>
              </div>
              <div className="h-5 w-px bg-slate-200 dark:bg-ink-border" />
              <button
                onClick={() => setShowChangePw(true)}
                className="grid place-items-center h-7 w-7 rounded-lg text-slate-400 hover:text-brand-600 hover:bg-brand-500/10 transition-colors"
                title="Passwort ändern"
                aria-label="Passwort ändern"
              >
                <KeyRound size={16} />
              </button>
              <button
                onClick={handleLogout}
                className="grid place-items-center h-7 w-7 rounded-lg text-slate-400 hover:text-hard hover:bg-hard/10 transition-colors"
                title="Abmelden"
                aria-label="Abmelden"
              >
                <LogOut size={16} />
              </button>
            </div>

            <button
              onClick={() => setIsMenuOpen(!isMenuOpen)}
              className="md:hidden grid place-items-center h-9 w-9 rounded-lg text-slate-500 hover:bg-slate-100 dark:hover:bg-ink-soft"
              aria-label="Menü"
            >
              {isMenuOpen ? <X size={22} /> : <Menu size={22} />}
            </button>
          </div>
        </div>

        {/* Mobile menu */}
        {isMenuOpen && (
          <div className="md:hidden border-t border-slate-200 dark:border-ink-border bg-white dark:bg-ink-card px-4 py-3 space-y-1 animate-slideDown">
            {navItems.filter((i) => i.show).map((item) => (
              <Link
                key={item.to}
                to={item.to}
                onClick={() => setIsMenuOpen(false)}
                className={`flex items-center gap-3 p-3 rounded-xl text-sm font-semibold transition-colors ${
                  isActive(item.to)
                    ? 'bg-brand-50 dark:bg-brand-500/10 text-brand-600 dark:text-brand-300'
                    : 'text-slate-600 dark:text-slate-300 hover:bg-slate-50 dark:hover:bg-ink-soft'
                }`}
              >
                {item.icon}
                <span>{item.label}</span>
              </Link>
            ))}
            <div className="h-px bg-slate-100 dark:bg-ink-border my-2" />
            <div className="flex items-center justify-between px-1 pb-1">
              <div className="flex items-center gap-2 text-sm">
                <UserIcon size={16} className="text-slate-400" />
                <span className="font-semibold text-slate-700 dark:text-slate-200">{user?.username}</span>
                <span className="font-mono text-[10px] uppercase text-slate-400">{user?.role}</span>
              </div>
              <button
                onClick={handleLogout}
                className="flex items-center gap-2 px-3 py-2 rounded-lg text-hard hover:bg-hard/10 transition-colors text-sm font-semibold"
              >
                <LogOut size={16} />
                <span>Abmelden</span>
              </button>
            </div>
          </div>
        )}
      </nav>

      <main className="flex-grow container mx-auto px-4 py-8">
        <div className="animate-fadeIn">{children}</div>
      </main>

      <footer className="border-t border-slate-200 dark:border-ink-border py-6">
        <div className="container mx-auto px-4 flex flex-col sm:flex-row items-center justify-between gap-2 text-sm text-slate-400 dark:text-slate-500">
          <span className="font-mono text-xs">XData IIT Bombay · Automated SQL Grading</span>
          <span>&copy; {new Date().getFullYear()} — gebaut zum Lernen, Query für Query.</span>
        </div>
      </footer>

      {/* Forced change on first login / after admin reset — blocks the app. */}
      {user?.mustChangePassword && <ChangePasswordModal forced />}
      {/* Voluntary change from the navbar. */}
      {showChangePw && !user?.mustChangePassword && (
        <ChangePasswordModal onClose={() => setShowChangePw(false)} />
      )}
    </div>
  );
};

export default Layout;
