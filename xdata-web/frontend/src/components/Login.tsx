import React, { useState } from 'react';
import api from '../api';
import { useAuth } from '../context/AuthContext';
import { toast } from 'react-hot-toast';
import { LogIn, Key, Mail, Database } from 'lucide-react';

const Login: React.FC = () => {
  const { login } = useAuth();
  const [loginId, setLoginId] = useState('');
  const [password, setPassword] = useState('');
  const [email, setEmail] = useState('');
  const [loading, setLoading] = useState(false);
  const [showForgot, setShowForgot] = useState(false);

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      const response = await api.post('/auth/login', { loginId, password });
      login(response.data.token, response.data);
      toast.success('Erfolgreich angemeldet!');
    } catch (err: any) {
      console.error('Login error details:', err);
      if (!err.response) {
        toast.error('Netzwerkfehler: Backend nicht erreichbar.');
      } else if (err.response.status === 401) {
        toast.error('Ungültige Anmeldedaten');
      } else {
        toast.error(err.response.data?.message || 'Login fehlgeschlagen');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleForgot = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      await api.post('/auth/forgot-password', { email });
      toast.success('Reset-Link wurde gesendet (falls E-Mail existiert)');
      setShowForgot(false);
    } catch (err) {
      toast.error('Fehler beim Senden der E-Mail');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 dark:bg-gray-900 transition-colors py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md w-full space-y-8 p-10 bg-white dark:bg-gray-800 rounded-3xl shadow-2xl border border-gray-100 dark:border-gray-700 transition-colors">
        <div>
          <div className="mx-auto h-16 w-16 flex items-center justify-center rounded-2xl bg-blue-100 dark:bg-blue-900/40 text-blue-600 dark:text-blue-400 shadow-inner">
            <Database size={32} />
          </div>
          <h2 className="mt-6 text-center text-3xl font-black text-gray-900 dark:text-white tracking-tight">
            XData <span className="text-blue-600">Web</span>
          </h2>
          <p className="mt-2 text-center text-[10px] text-gray-400 dark:text-gray-500 uppercase tracking-[0.2em] font-black">
            Automated SQL Grading
          </p>
        </div>
        {showForgot ? (
          <form className="mt-8 space-y-6" onSubmit={handleForgot}>
            <div className="space-y-1">
              <label className="text-[10px] font-black text-gray-400 uppercase tracking-widest ml-1">E-Mail Adresse</label>
              <div className="relative">
                <span className="absolute inset-y-0 left-0 pl-4 flex items-center text-gray-400">
                  <Mail size={18} />
                </span>
                <input
                  type="email"
                  required
                  className="appearance-none rounded-2xl relative block w-full pl-11 px-3 py-3.5 border border-gray-200 dark:border-gray-700 placeholder-gray-400 text-gray-900 dark:text-white bg-gray-50 dark:bg-gray-900 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:bg-white dark:focus:bg-gray-800 transition-all font-bold"
                  placeholder="name@beispiel.de"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                />
              </div>
            </div>
            <button
              type="submit"
              disabled={loading}
              className="w-full flex justify-center py-4 px-4 border border-transparent text-sm font-black rounded-2xl text-white bg-blue-600 hover:bg-blue-700 shadow-lg shadow-blue-500/20 transition-all active:scale-95 disabled:opacity-50"
            >
              {loading ? 'Sende...' : 'Reset-Link senden'}
            </button>
            <p className="text-center">
              <button type="button" onClick={() => setShowForgot(false)} className="text-sm font-bold text-blue-600 hover:underline">
                Zurück zum Login
              </button>
            </p>
          </form>
        ) : (
          <form className="mt-8 space-y-5" onSubmit={handleLogin}>
            <div className="space-y-4">
              <div className="space-y-1">
                <label className="text-[10px] font-black text-gray-400 uppercase tracking-widest ml-1">Benutzerkennung</label>
                <div className="relative">
                  <span className="absolute inset-y-0 left-0 pl-4 flex items-center text-gray-400 z-20">
                    <LogIn size={18} />
                  </span>
                  <input
                    id="loginId"
                    name="loginId"
                    type="text"
                    autoComplete="username"
                    required
                    className="appearance-none rounded-2xl relative block w-full pl-11 px-3 py-3.5 border border-gray-200 dark:border-gray-700 placeholder-gray-400 text-gray-900 dark:text-white bg-gray-50 dark:bg-gray-900 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:bg-white dark:focus:bg-gray-800 transition-all font-bold"
                    placeholder="Login ID"
                    value={loginId}
                    onChange={(e) => setLoginId(e.target.value)}
                    data-ignore-monaco="true"
                  />
                </div>
              </div>
              <div className="space-y-1">
                <label className="text-[10px] font-black text-gray-400 uppercase tracking-widest ml-1">Passwort</label>
                <div className="relative">
                  <span className="absolute inset-y-0 left-0 pl-4 flex items-center text-gray-400 z-20">
                    <Key size={18} />
                  </span>
                  <input
                    id="password"
                    name="password"
                    type="password"
                    autoComplete="current-password"
                    required
                    className="appearance-none rounded-2xl relative block w-full pl-11 px-3 py-3.5 border border-gray-200 dark:border-gray-700 placeholder-gray-400 text-gray-900 dark:text-white bg-gray-50 dark:bg-gray-900 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:bg-white dark:focus:bg-gray-800 transition-all font-bold"
                    placeholder="••••••••"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    data-ignore-monaco="true"
                  />
                </div>
              </div>
            </div>

            <div className="flex items-center justify-end">
              <button type="button" onClick={() => setShowForgot(true)} className="text-[10px] font-black uppercase text-blue-600 hover:text-blue-700 transition-colors tracking-widest">
                Passwort vergessen?
              </button>
            </div>

            <div className="pt-2">
              <button
                type="submit"
                disabled={loading}
                className="group relative w-full flex justify-center py-4 px-4 border border-transparent text-sm font-black rounded-2xl text-white bg-blue-600 hover:bg-blue-700 shadow-lg shadow-blue-500/20 focus:outline-none transition-all active:scale-95 disabled:opacity-50"
              >
                {loading ? 'Anmelden...' : 'Anmelden'}
              </button>
            </div>
          </form>
        )}
      </div>
    </div>
  );
};

export default Login;
