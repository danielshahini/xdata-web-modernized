import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../api';
import { useAuth } from '../context/AuthContext';
import { toast } from 'react-hot-toast';
import { Key, Mail, Database, User as UserIcon, ArrowRight, Sparkles, CheckCircle2, XOctagon } from 'lucide-react';

type AuthState = 'idle' | 'running' | 'ok' | 'error';

const Login: React.FC = () => {
  const { login } = useAuth();
  const [loginId, setLoginId] = useState('');
  const [password, setPassword] = useState('');
  const [email, setEmail] = useState('');
  const [loading, setLoading] = useState(false);
  const [showForgot, setShowForgot] = useState(false);
  // Drives the console "result" line — the signature ties the form to a live query.
  const [authState, setAuthState] = useState<AuthState>('idle');

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setAuthState('running');
    try {
      const response = await api.post('/auth/login', { loginId, password });
      setAuthState('ok');
      login(response.data.token, response.data);
      toast.success('Erfolgreich angemeldet!');
    } catch (err: any) {
      setAuthState('error');
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

  // The form values, rendered as the predicate of a live SQL query.
  const maskedPw = password ? '•'.repeat(Math.min(password.length, 10)) : '';
  const queryConsole = (
    <pre className="px-5 py-4 font-mono text-[13px] leading-[1.7] overflow-x-auto whitespace-pre">
<span className="text-brand-400">SELECT</span> <span className="text-slate-200">*</span> <span className="text-brand-400">FROM</span> <span className="text-slate-200">lernende</span>{'\n'}
<span className="text-brand-400">WHERE</span>  <span className="text-slate-200">login_id</span> <span className="text-slate-500">=</span> <span className="text-easy">'{loginId || <span className="text-slate-600">…</span>}'</span>{!loginId && <span className="ml-px inline-block w-[2px] h-[1.05em] -mb-[2px] bg-brand-400 animate-pulse align-middle" aria-hidden="true" />}{'\n'}
<span className="text-slate-500">  </span><span className="text-brand-400">AND</span>  <span className="text-slate-200">passwort</span> <span className="text-slate-500">=</span> <span className="text-xp-400">crypt</span><span className="text-slate-500">(</span><span className="text-easy">'{maskedPw || <span className="text-slate-600">…</span>}'</span><span className="text-slate-500">);</span>
    </pre>
  );

  const resultLine = (() => {
    switch (authState) {
      case 'running':
        return (
          <span className="flex items-center gap-2 text-brand-300">
            <span className="h-1.5 w-1.5 rounded-full bg-brand-400 animate-pulse" /> wird ausgeführt …
          </span>
        );
      case 'ok':
        return (
          <span className="flex items-center gap-1.5 text-easy">
            <CheckCircle2 size={13} /> 1 Zeile · authentifiziert
          </span>
        );
      case 'error':
        return (
          <span className="flex items-center gap-1.5 text-hard">
            <XOctagon size={13} /> 0 Zeilen · Zugriff verweigert
          </span>
        );
      default:
        return <span className="text-slate-500">bereit · drücke Anmelden zum Ausführen</span>;
    }
  })();

  return (
    <div className="min-h-screen grid lg:grid-cols-[1.05fr_1fr] bg-slate-50 dark:bg-ink-bg">
      {/* ── Signature panel: your credentials, rendered as a live query ── */}
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
          <h1 className="mt-3 font-display text-[2.6rem] font-extrabold leading-[1.05] tracking-tight text-white">
            SQL beherrschen,
            <br />
            <span className="text-brand-400">Query für Query.</span>
          </h1>

          {/* Live query console — the brand artifact, parameterised by the form */}
          <div className="mt-8 max-w-md rounded-2xl border border-ink-border bg-ink-card/80 backdrop-blur shadow-2xl">
            <div className="flex items-center gap-1.5 px-4 py-3 border-b border-ink-border">
              <span className="h-3 w-3 rounded-full bg-hard/80" />
              <span className="h-3 w-3 rounded-full bg-medium/80" />
              <span className="h-3 w-3 rounded-full bg-easy/80" />
              <span className="ml-2 font-mono text-xs text-slate-500">auth.sql</span>
              <span className="ml-auto font-mono text-[10px] uppercase tracking-widest text-slate-600">live</span>
            </div>
            {queryConsole}
            <div className="px-5 py-2.5 border-t border-ink-border font-mono text-[11px]" aria-live="polite">
              {resultLine}
            </div>
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
              <p className="kicker text-brand-500 mb-2">Konto wiederherstellen</p>
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
              <p className="kicker text-brand-500 mb-2">Anmeldung</p>
              <h2 className="font-display text-2xl font-bold text-slate-900 dark:text-white">Willkommen zurück</h2>
              <p className="mt-1.5 text-sm text-slate-500 dark:text-slate-400">Melde dich an und setze deine Reise fort.</p>

              {/* Mobile-only mini query strip so the signature shows on small screens too */}
              <div className="lg:hidden mt-5 rounded-xl border border-slate-200 dark:border-ink-border bg-slate-50 dark:bg-ink-card/80 px-4 py-3 font-mono text-[12px] overflow-x-auto">
                <span className="text-brand-500 dark:text-brand-400">SELECT</span> <span className="text-slate-500">* </span>
                <span className="text-brand-500 dark:text-brand-400">FROM</span> <span className="text-slate-700 dark:text-slate-200">lernende</span> <span className="text-brand-500 dark:text-brand-400">WHERE</span>{' '}
                <span className="text-easy">'{loginId || '…'}'</span>
              </div>

              <form className="mt-6 space-y-5" onSubmit={handleLogin}>
                <div>
                  <label htmlFor="loginId" className="kicker block mb-1.5 ml-0.5">Benutzerkennung</label>
                  <div className="relative">
                    <UserIcon size={18} className="absolute inset-y-0 left-3.5 my-auto text-slate-400 z-10" />
                    <input
                      id="loginId" name="loginId" type="text" autoComplete="username" required
                      className={inputBase} placeholder="Login-ID" value={loginId}
                      onChange={(e) => { setLoginId(e.target.value); if (authState !== 'idle') setAuthState('idle'); }}
                      data-ignore-monaco="true"
                    />
                  </div>
                </div>

                <div>
                  <div className="flex items-center justify-between mb-1.5 ml-0.5">
                    <label htmlFor="password" className="kicker">Passwort</label>
                    <button type="button" onClick={() => setShowForgot(true)} className="text-xs font-semibold text-brand-600 hover:text-brand-700 transition-colors normal-case tracking-normal">
                      Vergessen?
                    </button>
                  </div>
                  <div className="relative">
                    <Key size={18} className="absolute inset-y-0 left-3.5 my-auto text-slate-400 z-10" />
                    <input
                      id="password" name="password" type="password" autoComplete="current-password" required
                      className={inputBase} placeholder="••••••••" value={password}
                      onChange={(e) => { setPassword(e.target.value); if (authState !== 'idle') setAuthState('idle'); }}
                      data-ignore-monaco="true"
                    />
                  </div>
                </div>

                <button type="submit" disabled={loading} className="btn-primary w-full py-3 group">
                  {loading ? 'Führe aus …' : (<><span>Anmelden</span><ArrowRight size={16} className="group-hover:translate-x-0.5 transition-transform" /></>)}
                </button>
              </form>
            </>
          )}

          <div className="mt-8 pt-6 border-t border-slate-200 dark:border-ink-border text-center">
            <Link to="/try-sql" className="group inline-flex items-center gap-1.5 text-sm font-semibold text-brand-600 hover:text-brand-700 dark:text-brand-400 transition-colors">
              <Sparkles size={15} /> SQL ohne Anmeldung ausprobieren
              <ArrowRight size={14} className="opacity-0 -translate-x-1 group-hover:opacity-100 group-hover:translate-x-0 transition-all" />
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
