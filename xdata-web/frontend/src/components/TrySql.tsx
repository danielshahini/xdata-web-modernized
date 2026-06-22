import React, { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import Editor from '@monaco-editor/react';
import { Play, RefreshCw, Database, ArrowRight, Sparkles, Sun, Moon, Table } from 'lucide-react';
import api from '../api';
import { useAuth } from '../context/AuthContext';
import SchemaVisualizer from './SchemaVisualizer';

interface RunResult {
  columns?: string[];
  rows?: any[][];
  rowCount?: number;
  truncated?: boolean;
  error?: string;
}

const TrySql: React.FC = () => {
  const { isDark, toggleTheme } = useAuth();
  const [sql, setSql] = useState('SELECT * FROM students;');
  const [schema, setSchema] = useState<any | null>(null);
  const [examples, setExamples] = useState<{ label: string; sql: string }[]>([]);
  const [result, setResult] = useState<RunResult | null>(null);
  const [running, setRunning] = useState(false);

  useEffect(() => {
    api.get('/public/sql/info')
      .then(res => {
        setSchema(res.data.schema);
        setExamples(res.data.examples || []);
      })
      .catch(() => { /* sandbox info is non-critical */ });
  }, []);

  const run = useCallback(async () => {
    if (!sql.trim()) return;
    setRunning(true);
    setResult(null);
    try {
      const res = await api.post('/public/sql/run', { query: sql });
      setResult(res.data);
    } catch (e) {
      setResult({ error: 'Connection failed. Please try again.' });
    } finally {
      setRunning(false);
    }
  }, [sql]);

  return (
    <div className="min-h-screen bg-slate-50 dark:bg-ink-bg">
      {/* Header */}
      <header className="sticky top-0 z-20 backdrop-blur bg-white/80 dark:bg-ink-bg/80 border-b border-slate-200 dark:border-ink-border">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 h-16 flex items-center justify-between gap-4">
          <Link to="/login" className="flex items-center gap-2 font-display font-extrabold text-lg text-slate-900 dark:text-white">
            XData<span className="text-brand-600">/sql</span>
          </Link>
          <div className="flex items-center gap-2">
            <button onClick={toggleTheme} aria-label="Toggle theme" className="p-2.5 rounded-xl text-slate-500 hover:bg-slate-100 dark:hover:bg-ink-soft transition-colors">
              {isDark ? <Sun size={18} /> : <Moon size={18} />}
            </button>
            <Link to="/login" className="btn-primary text-sm py-2.5 px-4">
              Sign in <ArrowRight size={15} />
            </Link>
          </div>
        </div>
      </header>

      <main className="max-w-7xl mx-auto px-4 sm:px-6 py-8 space-y-6">
        {/* Hero */}
        <div className="space-y-2">
          <span className="kicker text-brand-500 flex items-center gap-1.5"><Sparkles size={14} /> Free · no sign-up</span>
          <h1 className="font-display text-3xl md:text-4xl font-extrabold tracking-tight text-slate-900 dark:text-white">
            Try SQL — instantly, in your browser
          </h1>
          <p className="text-slate-500 dark:text-slate-400 max-w-2xl">
            Write a <span className="font-mono font-semibold text-brand-600">SELECT</span> query against a sample database and run it right away.
            No installation, no login. Read-only access.
          </p>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Left: schema + examples */}
          <aside className="lg:col-span-1 space-y-6 order-2 lg:order-1">
            <div className="x-card p-5">
              <h2 className="flex items-center gap-2 mb-4">
                <Database size={16} className="text-brand-500" />
                <span className="kicker">Sample database</span>
              </h2>
              {schema ? <SchemaVisualizerCompact metadata={schema} /> : <p className="text-sm text-slate-400">Loading …</p>}
            </div>

            <div className="x-card p-5">
              <h2 className="flex items-center gap-2 mb-4">
                <Table size={16} className="text-brand-500" />
                <span className="kicker">Sample queries</span>
              </h2>
              <div className="space-y-2">
                {examples.map(ex => (
                  <button
                    key={ex.label}
                    onClick={() => { setSql(ex.sql); setResult(null); }}
                    className="w-full text-left p-3 rounded-xl border border-slate-200 dark:border-ink-border hover:border-brand-300 dark:hover:border-brand-500/40 hover:bg-slate-50 dark:hover:bg-ink-soft transition-all group"
                  >
                    <span className="text-sm font-semibold text-slate-700 dark:text-slate-200 group-hover:text-brand-600 dark:group-hover:text-brand-300">{ex.label}</span>
                  </button>
                ))}
              </div>
            </div>
          </aside>

          {/* Right: editor + results */}
          <section className="lg:col-span-2 space-y-5 order-1 lg:order-2">
            <div className="x-card overflow-hidden">
              <div className="px-4 py-2.5 border-b border-slate-200 dark:border-ink-border flex items-center justify-between">
                <span className="kicker">SQL editor</span>
                <span className="font-mono text-[11px] text-slate-400">SELECT only</span>
              </div>
              <div className="h-[300px]">
                <Editor
                  height="100%"
                  defaultLanguage="sql"
                  theme={isDark ? 'vs-dark' : 'light'}
                  value={sql}
                  onChange={(v) => setSql(v || '')}
                  loading={<div className="flex items-center justify-center h-full text-slate-400 font-mono text-xs animate-pulse">Loading SQL editor …</div>}
                  options={{
                    minimap: { enabled: false },
                    fontSize: 15,
                    fontFamily: "'JetBrains Mono', monospace",
                    lineNumbers: 'on',
                    padding: { top: 16, bottom: 16 },
                    automaticLayout: true,
                    wordWrap: 'on',
                    scrollBeyondLastLine: false,
                  }}
                />
              </div>
            </div>

            <button onClick={run} disabled={running || !sql.trim()} className="btn-primary w-full py-4 text-base justify-center">
              {running ? <RefreshCw className="animate-spin" size={18} /> : <Play size={18} />}
              Run query
            </button>

            {result && (
              <div className="x-card overflow-hidden animate-fadeIn">
                {result.error ? (
                  <div className="p-4 bg-hard/5 text-hard text-sm font-mono">{result.error}</div>
                ) : (
                  <div>
                    <div className="px-4 py-2 bg-slate-50 dark:bg-ink-soft border-b border-slate-200 dark:border-ink-border flex items-center justify-between">
                      <span className="kicker">Result</span>
                      <span className="text-xs text-slate-400">{result.rowCount} row(s){result.truncated ? ' · truncated to 100' : ''}</span>
                    </div>
                    <div className="overflow-x-auto max-h-96">
                      <table className="w-full text-left border-collapse text-sm">
                        <thead>
                          <tr>{(result.columns || []).map((c, i) => <th key={i} className="x-th">{c}</th>)}</tr>
                        </thead>
                        <tbody>
                          {(result.rows || []).map((r, ri) => (
                            <tr key={ri} className="x-row">
                              {r.map((cell, ci) => <td key={ci} className="x-td font-mono text-xs">{cell === null ? <span className="text-slate-300 italic">NULL</span> : String(cell)}</td>)}
                            </tr>
                          ))}
                          {(result.rows || []).length === 0 && (
                            <tr><td className="x-td text-slate-400 italic" colSpan={(result.columns || []).length || 1}>No rows.</td></tr>
                          )}
                        </tbody>
                      </table>
                    </div>
                  </div>
                )}
              </div>
            )}

            {/* CTA */}
            <div className="rounded-2xl bg-ink-bg text-white p-6 relative overflow-hidden">
              <div className="absolute -top-16 -right-10 h-44 w-44 rounded-full bg-brand-600/30 blur-3xl" />
              <div className="relative flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                <div>
                  <p className="font-display text-lg font-bold">Ready for more than the sandbox?</p>
                  <p className="text-sm text-slate-300 mt-0.5">With an account you get courses, assignments with automatic grading, XP, hints and a leaderboard.</p>
                </div>
                <Link to="/login" className="btn-primary shrink-0 py-3 px-5 whitespace-nowrap">
                  Sign up now <ArrowRight size={16} />
                </Link>
              </div>
            </div>
          </section>
        </div>
      </main>
    </div>
  );
};

// The full visualizer renders a 3-col grid; in the narrow sidebar we want a single column.
const SchemaVisualizerCompact: React.FC<{ metadata: any }> = ({ metadata }) => (
  <div className="[&_.grid]:!grid-cols-1">
    <SchemaVisualizer metadata={metadata} />
  </div>
);

export default TrySql;
