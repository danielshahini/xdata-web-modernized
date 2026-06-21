import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../api';
import { useAuth } from '../context/AuthContext';
import { toast } from 'react-hot-toast';
import { Key, Mail, Database, User as UserIcon, ArrowRight, Sparkles } from 'lucide-react';

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
      } else if (err.response.status === 423) {
        const mins = err.response.headers?.['x-lock-minutes'];
        toast.error(`Konto vorübergehend gesperrt (zu viele Fehlversuche).${mins ? ` Bitte in ${mins} Min erneut versuchen.` : ''}`);
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

  const inputBase =
    'w-full rounded-xl border border-slate-200 dark:border-ink-border bg-slate-50 dark:bg-ink-bg pl-11 pr-3 py-3 text-sm font-medium text-slate-900 dark:text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-brand-500 focus:border-transparent focus:bg-white dark:focus:bg-ink-soft transition-all';

  return (
    <div className="min-h-screen grid lg:grid-cols-2 bg-slate-50 dark:bg-ink-bg">
      {/* ── Signature panel: a query that authenticates you ── */}
      <aside className="relative hidden lg:flex flex-col justify-between overflow-hidden bg-ink-bg text-slate-200 p-12">
        <div className="absolute inset-0 bg-grid opacity-60" />
        <div className="absolute -top-24 -right-24 h-80 w-80 rounded-full bg-brand-600/30 blur-3xl" />
        <div className="absolute bottom-0 -left-20 h-72 w-72 rounded-full bg-xp-500/10 blur-3xl" />

        <div className="relative flex items-center gap-2.5">
          <div className="grid place-items-center h-10 w-10 rounded-xl bg-brand-600 text-white shadow-glow">
            <Database size={20} strokeWidth={2.4} />
          </div>
          <div className="leading-none">
            <span className="font-display text-xl font-extrabold tracking-tight text-white">XData</span>
            <span className="ml-1 font-mono text-sm font-semibold text-brand-400">/sql</span>
          </div>
        </div>

        <div className="relative">
          <p className="kicker text-brand-400">Lernplattform für Datenbanken</p>
          <h1 className="mt-3 font-display text-4xl font-extrabold leading-tight text-white">
            SQL beherrschen,
            <br />
            <span className="text-brand-400">Query für Query.</span>
          </h1>

          {/* Faux query console — the brand artifact */}
          <div className="mt-8 max-w-md rounded-2xl border border-ink-border bg-ink-card/80 backdrop-blur shadow-2xl">
            <div className="flex items-center gap-1.5 px-4 py-3 border-b border-ink-border">
              <span className="h-3 w-3 rounded-full bg-hard/80" />
              <span className="h-3 w-3 rounded-full bg-medium/80" />
              <span className="h-3 w-3 rounded-full bg-easy/80" />
              <span className="ml-2 font-mono text-xs text-slate-500">auth.sql</span>
            </div>
            <pre className="px-4 py-4 font-mono text-[13px] leading-relaxed overflow-x-auto">
<span className="text-brand-400">SELECT</span> <span className="text-slate-200">fortschritt</span>
<span className="text-brand-400">FROM</span>   <span className="text-slate-200">deine_reise</span>
<span className="text-brand-400">WHERE</span>  <span className="text-slate-200">user</span> <span className="text-slate-500">=</span> <span className="text-easy">'angemeldet'</span><span className="text-slate-500">;</span>
            </pre>
          </div>

          <div className="mt-7 flex items-center gap-4 text-xs font-semibold">
            <span className="pill pill-easy">Easy</span>
            <span className="pill pill-medium">Medium</span>
            <span className="pill pill-hard">Hard</span>
            <span className="flex items-center gap-1.5 text-xp-400">
              <Sparkles size={14} /> XP sammeln
            </span>
          </div>
        </div>

        <p className="relative font-mono text-xs text-slate-500">XData IIT Bombay · Automated SQL Grading</p>
      </aside>

      {/* ── Form panel ── */}
      <main className="flex items-center justify-center p-6 sm:p-12">
        <div className="w-full max-w-sm animate-rise">
          {/* compact brand for mobile */}
          <div className="lg:hidden mb-8 flex items-center gap-2.5">
            <div className="grid place-items-center h-10 w-10 rounded-xl bg-brand-600 text-white shadow-glow">
              <Database size={20} strokeWidth={2.4} />
            </div>
            <span className="font-display text-xl font-extrabold tracking-tight text-slate-900 dark:text-white">
              XData<span className="ml-1 font-mono text-sm font-semibold text-brand-500">/sql</span>
            </span>
          </div>

          {showForgot ? (
            <>
              <h2 className="font-display text-2xl font-bold text-slate-900 dark:text-white">Passwort zurücksetzen</h2>
              <p className="mt-1.5 text-sm text-slate-500 dark:text-slate-400">
                Gib deine E-Mail ein — wir senden dir einen Reset-Link.
              </p>
              <form className="mt-7 space-y-5" onSubmit={handleForgot}>
                <div>
                  <label className="kicker block mb-1.5 ml-0.5">E-Mail-Adresse</label>
                  <div className="relative">
                    <Mail size={18} className="absolute inset-y-0 left-3.5 my-auto text-slate-400" />
                    <input type="email" required className={inputBase} placeholder="name@beispiel.de" value={email} onChange={(e) => setEmail(e.target.value)} />
                  </div>
                </div>
                <button type="submit" disabled={loading} className="btn-primary w-full py-3">
                  {loading ? 'Sende …' : 'Reset-Link senden'}
                </button>
                <button type="button" onClick={() => setShowForgot(false)} className="block w-full text-center text-sm font-semibold text-brand-600 hover:text-brand-700 transition-colors">
                  Zurück zum Login
                </button>
              </form>
            </>
          ) : (
            <>
              <h2 className="font-display text-2xl font-bold text-slate-900 dark:text-white">Willkommen zurück</h2>
              <p className="mt-1.5 text-sm text-slate-500 dark:text-slate-400">Melde dich an und setze deine Reise fort.</p>

              <form className="mt-7 space-y-5" onSubmit={handleLogin}>
                <div>
                  <label className="kicker block mb-1.5 ml-0.5">Benutzerkennung</label>
                  <div className="relative">
                    <UserIcon size={18} className="absolute inset-y-0 left-3.5 my-auto text-slate-400 z-10" />
                    <input
                      id="loginId" name="loginId" type="text" autoComplete="username" required
                      className={inputBase} placeholder="Login-ID" value={loginId}
                      onChange={(e) => setLoginId(e.target.value)} data-ignore-monaco="true"
                    />
                  </div>
                </div>

                <div>
                  <div className="flex items-center justify-between mb-1.5 ml-0.5">
                    <label className="kicker">Passwort</label>
                    <button type="button" onClick={() => setShowForgot(true)} className="text-xs font-semibold text-brand-600 hover:text-brand-700 transition-colors normal-case tracking-normal">
                      Vergessen?
                    </button>
                  </div>
                  <div className="relative">
                    <Key size={18} className="absolute inset-y-0 left-3.5 my-auto text-slate-400 z-10" />
                    <input
                      id="password" name="password" type="password" autoComplete="current-password" required
                      className={inputBase} placeholder="••••••••" value={password}
                      onChange={(e) => setPassword(e.target.value)} data-ignore-monaco="true"
                    />
                  </div>
                </div>

                <button type="submit" disabled={loading} className="btn-primary w-full py-3 group">
                  {loading ? 'Anmelden …' : (<><span>Anmelden</span><ArrowRight size={16} className="group-hover:translate-x-0.5 transition-transform" /></>)}
                </button>
              </form>
            </>
          )}

          <div className="mt-8 pt-6 border-t border-slate-200 dark:border-ink-border text-center">
            <Link to="/try-sql" className="inline-flex items-center gap-1.5 text-sm font-semibold text-brand-600 hover:text-brand-700 dark:text-brand-400 transition-colors">
              <Sparkles size={15} /> SQL ohne Anmeldung ausprobieren
            </Link>
          </div>

          <p className="mt-6 text-center font-mono text-xs text-slate-400 dark:text-slate-600">
            &copy; {new Date().getFullYear()} XData Web
          </p>
        </div>
      </main>
    </div>
  );
};

export default Login;
