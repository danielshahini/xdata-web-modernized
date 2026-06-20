import React, { useState, useEffect, useCallback, useRef } from 'react';
import api from '../api';
import Editor from '@monaco-editor/react';
import { toast } from 'react-hot-toast';
import WebSocketService from '../services/WebSocketService';
import { useAuth } from '../context/AuthContext';
import { createSqlCompletionProvider } from '../utils/sqlCompletion';
import { useAsyncData } from '../hooks/useAsyncData';
import {
  BookOpen,
  CheckCircle,
  Clock,
  History,
  Send,
  ChevronRight,
  Award,
  RefreshCw,
  Bell,
  Code,
  Trophy,
  Target,
  MessageSquare,
  Database,
  XCircle,
  Sparkles,
  Circle,
} from 'lucide-react';
import { Assignment, Question, Submission, Announcement } from '../types';
import Skeleton from './common/Skeleton';
import MarkInfoDisplay from './MarkInfoDisplay';
import SchemaVisualizer from './SchemaVisualizer';

interface DashboardData {
  studentName: string;
  courseName: string;
  xp: number;
  level: number;
  nextLevelXp: number;
  currentLevelXp: number;
  assignments: {
    id: number;
    assignmentId: number;
    name: string;
    deadline: string;
    totalQuestions: number;
    solvedQuestions: number;
    totalMarks: number;
    achievedMarks: number;
    percentage: number;
  }[];
}

// Derive a LeetCode-style difficulty from a question's point value.
const difficultyOf = (marks: number): { key: 'easy' | 'medium' | 'hard'; label: string } => {
  if (marks <= 2) return { key: 'easy', label: 'Easy' };
  if (marks <= 5) return { key: 'medium', label: 'Medium' };
  return { key: 'hard', label: 'Hard' };
};

const StudentDashboard: React.FC = () => {
  const [dashboard, setDashboard] = useState<DashboardData | null>(null);
  const [announcements, setAnnouncements] = useState<Announcement[]>([]);
  const [selectedAssignment, setSelectedAssignment] = useState<Assignment | null>(null);
  const [questions, setQuestions] = useState<Question[]>([]);
  const [selectedQuestion, setSelectedQuestion] = useState<Question | null>(null);
  const [submissions, setSubmissions] = useState<Submission[]>([]);
  const [attempts, setAttempts] = useState<Submission[]>([]);
  const [sql, setSql] = useState('');
  const [loading, setLoading] = useState(false);
  const [showHistory, setShowHistory] = useState(false);
  const [showCheatSheet, setShowCheatSheet] = useState(false);
  const [showSchema, setShowSchema] = useState(false);
  // Question id whose submission is currently being graded (awaiting the
  // WebSocket result push). Used to show a "grading…" state instead of a
  // misleading 0% from the not-yet-graded submission.
  const [gradingQuestionId, setGradingQuestionId] = useState<number | null>(null);
  const completionProviderRef = useRef<any>(null);

  const { user, isDark } = useAuth();

  const { data: schemaMetadata } = useAsyncData<any>(
    () => {
      const schemaId = selectedQuestion?.assignment?.defaultSchemaId ?? selectedAssignment?.defaultSchemaId;
      return schemaId
        ? api.get(`/schemas/${schemaId}/metadata`).then(res => res.data).catch(() => null)
        : Promise.resolve(null);
    },
    [selectedQuestion, selectedAssignment]
  );

  useEffect(() => {
    let isCancelled = false;
    if (schemaMetadata) {
      import('@monaco-editor/react').then(({ loader }) => {
        loader.init().then(monaco => {
          if (isCancelled) return;
          if (completionProviderRef.current) {
            completionProviderRef.current.dispose();
          }
          completionProviderRef.current = monaco.languages.registerCompletionItemProvider(
            'sql', createSqlCompletionProvider(monaco, schemaMetadata));
        });
      });
    }
    return () => {
      isCancelled = true;
      if (completionProviderRef.current) {
        completionProviderRef.current.dispose();
        completionProviderRef.current = null;
      }
    };
  }, [schemaMetadata]);

  const handleEditorMount = (editor: any) => {
    const textarea = editor.getDomNode()?.querySelector('textarea');
    if (textarea) {
      textarea.setAttribute('autocomplete', 'off');
      textarea.setAttribute('autocorrect', 'off');
      textarea.setAttribute('autocapitalize', 'off');
      textarea.setAttribute('spellcheck', 'false');
      textarea.setAttribute('data-lpignore', 'true');
      textarea.setAttribute('data-form-type', 'other');
    }
  };

  const loadDashboard = useCallback(async () => {
    try {
      const res = await api.get('/student/dashboard');
      setDashboard(res.data);
    } catch (e) {
      console.error("Dashboard konnte nicht geladen werden");
    }
  }, []);

  const loadAnnouncements = useCallback(async () => {
    try {
      const res = await api.get('/announcements');
      setAnnouncements(res.data || []);
    } catch (e) {
      console.error("Ankündigungen konnten nicht geladen werden");
    }
  }, []);

  const loadSubmissions = useCallback(() => {
    api.get('/student/submissions').then(res => setSubmissions(res.data || []));
  }, []);

  useEffect(() => {
    loadDashboard();
    loadAnnouncements();
    loadSubmissions();

    if (user) {
      WebSocketService.connect().then(() => {
        WebSocketService.subscribe(`/topic/grading/${user.loginId}`, (data) => {
          toast.success(`Aufgabe "${data.question.name}" wurde bewertet: ${(data.marks * 100).toFixed(0)}%`);
          // Clear the "grading…" state for the graded question so the real result shows.
          setGradingQuestionId(prev => (prev === data.questionId ? null : prev));
          loadSubmissions();
          loadDashboard();
        });
      });
    }

    return () => WebSocketService.disconnect();
  }, [loadDashboard, loadAnnouncements, loadSubmissions, user]);

  const loadAttempts = async (questionId: number) => {
    try {
      const res = await api.get(`/student/questions/${questionId}/attempts`);
      setAttempts(res.data || []);
      setShowHistory(true);
    } catch (e) {
      console.error("Fehler beim Laden der Versuche");
    }
  };

  const loadQuestions = async (assignmentId: number) => {
    try {
      const res = await api.get(`/student/assignments/${assignmentId}/questions`);
      setQuestions(res.data || []);
    } catch (e) {
      toast.error("Fehler beim Laden der Fragen");
    }
  };

  const submitSolution = async () => {
    if (!selectedQuestion || !sql.trim()) return;
    const qId = selectedQuestion.id;
    setLoading(true);
    // Show a "grading…" state immediately (before the request resolves) so the
    // panel never flashes the not-yet-graded 0% result. Cleared by the WebSocket
    // result push, or by the fallback below if no push arrives.
    setGradingQuestionId(qId);
    try {
      await api.post('/student/submit', {
        questionId: qId,
        query: sql
      });
      toast.success("Abgabe erfolgreich! Bewertung läuft...");
      setSql('');
      // Fallback in case the live push never arrives (e.g. WS dropped or the
      // backend grading failed): refresh and clear the grading state.
      setTimeout(() => {
        setGradingQuestionId(prev => (prev === qId ? null : prev));
        loadSubmissions();
        loadDashboard();
      }, 12000);
    } catch (e) {
      toast.error("Fehler bei der Abgabe");
      setGradingQuestionId(prev => (prev === qId ? null : prev));
    } finally {
      setLoading(false);
    }
  };

  const getQuestionStatus = (questionId: number) => {
    const subs = submissions.filter(s => s.questionId === questionId);
    if (subs.length === 0) return 'NOT_STARTED';
    const best = Math.max(...subs.map(s => s.marks));
    if (best >= 1.0) return 'SOLVED';
    if (best > 0) return 'PARTIAL';
    return 'FAILED';
  };

  const sqlHints = [
    { cmd: 'SELECT', desc: 'Spalten auswählen', example: 'SELECT * FROM users;' },
    { cmd: 'WHERE', desc: 'Filtern', example: 'WHERE age > 18' },
    { cmd: 'JOIN', desc: 'Tabellen verbinden', example: 'JOIN orders ON users.id = orders.user_id' },
    { cmd: 'GROUP BY', desc: 'Gruppieren', example: 'GROUP BY department' },
    { cmd: 'ORDER BY', desc: 'Sortieren', example: 'ORDER BY created_at DESC' },
    { cmd: 'COUNT', desc: 'Zählen', example: 'SELECT COUNT(*) FROM users;' },
    { cmd: 'IN', desc: 'In Liste', example: 'WHERE id IN (1, 2, 3)' },
    { cmd: 'LIKE', desc: 'Mustervergleich', example: "WHERE name LIKE 'A%'" }
  ];

  if (!dashboard) return <div className="max-w-7xl mx-auto"><Skeleton count={3} /></div>;

  const totalAchieved = dashboard.assignments.reduce((acc, curr) => acc + curr.achievedMarks, 0);
  const totalMax = dashboard.assignments.reduce((acc, curr) => acc + curr.totalMarks, 0);
  const overallPercentage = totalMax > 0 ? (totalAchieved / totalMax) * 100 : 0;
  const levelSpan = dashboard.nextLevelXp - dashboard.currentLevelXp;
  const xpIntoLevel = levelSpan > 0 ? ((dashboard.xp - dashboard.currentLevelXp) / levelSpan) * 100 : 0;
  const solvedTotal = dashboard.assignments.reduce((a, c) => a + c.solvedQuestions, 0);
  const questionsTotal = dashboard.assignments.reduce((a, c) => a + c.totalQuestions, 0);

  return (
    <div className="max-w-7xl mx-auto space-y-6 animate-fadeIn pb-20">
      {/* ── Hero ── */}
      <section className="relative overflow-hidden rounded-3xl bg-ink-bg text-white p-7 md:p-9">
        <div className="absolute inset-0 bg-grid opacity-50" />
        <div className="absolute -top-24 -right-16 h-72 w-72 rounded-full bg-brand-600/30 blur-3xl" />
        <div className="absolute -bottom-24 left-1/3 h-60 w-60 rounded-full bg-xp-500/10 blur-3xl" />

        <div className="relative flex flex-col lg:flex-row lg:items-center justify-between gap-7">
          <div className="flex-1 min-w-0">
            <div className="flex items-center gap-3">
              <p className="kicker text-brand-400">{dashboard.courseName || 'Dein Lern-Dashboard'}</p>
            </div>
            <h1 className="mt-2 font-display text-3xl md:text-4xl font-extrabold tracking-tight">
              Hallo, {dashboard.studentName}.
            </h1>

            {/* XP / level bar */}
            <div className="mt-6 max-w-lg">
              <div className="flex items-center justify-between mb-2">
                <span className="inline-flex items-center gap-1.5 rounded-lg bg-xp-500/15 text-xp-400 px-2.5 py-1 text-xs font-bold">
                  <Sparkles size={13} /> Level {dashboard.level}
                </span>
                <span className="font-mono text-xs text-slate-400">
                  {dashboard.xp} / {dashboard.nextLevelXp} XP
                </span>
              </div>
              <div className="h-2.5 w-full rounded-full bg-white/10 overflow-hidden">
                <div
                  className="h-full rounded-full bg-gradient-to-r from-xp-500 to-xp-400 transition-all duration-1000"
                  style={{ width: `${Math.max(0, Math.min(100, xpIntoLevel))}%` }}
                />
              </div>
            </div>
          </div>

          {/* Stat tiles */}
          <div className="grid grid-cols-3 gap-3 lg:gap-4 shrink-0">
            <StatTile icon={<Trophy size={18} />} label="Score" value={`${overallPercentage.toFixed(0)}%`} sub={`${totalAchieved.toFixed(0)}/${totalMax.toFixed(0)} Pkt`} accent="xp" />
            <StatTile icon={<Target size={18} />} label="Gelöst" value={`${solvedTotal}`} sub={`von ${questionsTotal}`} accent="brand" />
            <StatTile icon={<BookOpen size={18} />} label="Module" value={`${dashboard.assignments.length}`} sub="aktiv" accent="brand" />
          </div>
        </div>
      </section>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* ── Left: announcements + assignment list ── */}
        <div className="lg:col-span-1 space-y-6">
          {announcements.length > 0 && (
            <div className="x-card p-5">
              <h2 className="flex items-center gap-2 mb-4">
                <Bell size={16} className="text-brand-500" />
                <span className="kicker">Ankündigungen</span>
              </h2>
              <div className="space-y-3 max-h-[250px] overflow-y-auto pr-1">
                {announcements.map(a => (
                  <div key={a.id} className="p-3.5 rounded-xl bg-brand-50 dark:bg-brand-500/10 border border-brand-100 dark:border-brand-500/20">
                    <p className="font-semibold text-brand-900 dark:text-brand-200 text-sm">{a.title}</p>
                    <p className="text-xs text-brand-700/80 dark:text-brand-300/70 mt-1 leading-relaxed">{a.content}</p>
                    <div className="flex justify-between items-center text-[10px] font-medium text-brand-500/70 mt-2 font-mono">
                      <span>{a.course?.courseName}</span>
                      <span>{new Date(a.createdAt).toLocaleDateString()}</span>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          <div className="x-card p-5">
            <h2 className="flex items-center gap-2 mb-4">
              <BookOpen size={16} className="text-brand-500" />
              <span className="kicker">Meine Aufgaben</span>
            </h2>
            <div className="space-y-2.5">
              {dashboard.assignments.map(a => {
                const active = selectedAssignment?.id === a.id;
                return (
                  <button
                    key={a.id}
                    onClick={() => {
                      setSelectedAssignment(a as any);
                      setSelectedQuestion(null);
                      loadQuestions(a.id);
                    }}
                    className={`w-full text-left p-4 rounded-xl border transition-all group ${
                      active
                        ? 'border-brand-400 dark:border-brand-500 bg-brand-50 dark:bg-brand-500/10 ring-2 ring-brand-500/15'
                        : 'border-slate-200 dark:border-ink-border hover:border-brand-300 dark:hover:border-brand-500/40 hover:bg-slate-50 dark:hover:bg-ink-soft'
                    }`}
                  >
                    <div className="flex justify-between items-start gap-2 mb-2.5">
                      <span className="font-semibold text-slate-800 dark:text-slate-100 text-sm group-hover:text-brand-600 dark:group-hover:text-brand-300 transition-colors">{a.name}</span>
                      <span className="font-mono text-xs font-bold text-brand-600 dark:text-brand-300 shrink-0">{a.percentage.toFixed(0)}%</span>
                    </div>
                    <div className="flex items-center gap-3 text-[11px] font-medium text-slate-400 mb-2.5">
                      <span className="flex items-center gap-1"><Clock size={12} /> {new Date(a.deadline).toLocaleDateString()}</span>
                      <span className="flex items-center gap-1 text-easy"><Target size={12} /> {a.solvedQuestions}/{a.totalQuestions}</span>
                    </div>
                    <div className="w-full bg-slate-100 dark:bg-ink-soft h-1.5 rounded-full overflow-hidden">
                      <div className="bg-brand-500 h-full rounded-full transition-all duration-700" style={{ width: `${a.percentage}%` }} />
                    </div>
                  </button>
                );
              })}
            </div>
          </div>
        </div>

        {/* ── Right: questions + editor ── */}
        <div className="lg:col-span-2 space-y-6">
          {selectedAssignment ? (
            <div className="animate-slideUp space-y-6">
              <div className="x-card p-6">
                <div className="flex flex-wrap justify-between items-center gap-3 mb-5">
                  <span className="kicker">Fragen · {selectedAssignment.name}</span>
                  <div className="flex items-center gap-2 font-mono text-[11px]">
                    <span className="px-2.5 py-1 bg-brand-50 dark:bg-brand-500/10 text-brand-600 dark:text-brand-300 rounded-md font-semibold">
                      {selectedAssignment.totalQuestions || questions.length} Aufgaben
                    </span>
                    <span className="px-2.5 py-1 bg-slate-100 dark:bg-ink-soft text-slate-500 dark:text-slate-400 rounded-md font-semibold">
                      {selectedAssignment.totalMarks || questions.reduce((acc, q) => acc + q.marks, 0)} Pkt
                    </span>
                  </div>
                </div>

                {/* LeetCode-style problem list */}
                <div className="divide-y divide-slate-100 dark:divide-ink-border -mx-1">
                  {questions.map((q, idx) => {
                    const status = getQuestionStatus(q.id);
                    const diff = difficultyOf(q.marks);
                    const active = selectedQuestion?.id === q.id;
                    return (
                      <button
                        key={q.id}
                        onClick={() => { setSelectedQuestion(q); setSql(''); }}
                        className={`w-full flex items-center gap-3 px-3 py-3 rounded-lg text-left transition-colors ${
                          active ? 'bg-brand-50 dark:bg-brand-500/10' : 'hover:bg-slate-50 dark:hover:bg-ink-soft'
                        }`}
                      >
                        <span className="shrink-0">
                          {status === 'SOLVED' ? <CheckCircle size={18} className="text-easy" />
                            : status === 'PARTIAL' ? <RefreshCw size={18} className="text-medium" />
                            : status === 'FAILED' ? <XCircle size={18} className="text-hard" />
                            : <Circle size={18} className="text-slate-300 dark:text-slate-600" />}
                        </span>
                        <span className="font-mono text-xs text-slate-400 w-7 shrink-0">{(idx + 1).toString().padStart(2, '0')}</span>
                        <span className={`flex-1 truncate text-sm font-medium ${active ? 'text-brand-700 dark:text-brand-300' : 'text-slate-700 dark:text-slate-200'}`}>
                          {q.name}
                        </span>
                        <span className={`pill pill-${diff.key} shrink-0`}>{diff.label}</span>
                        <span className="font-mono text-[11px] text-slate-400 w-12 text-right shrink-0">{q.marks} Pkt</span>
                        <ChevronRight size={16} className="text-slate-300 dark:text-slate-600 shrink-0" />
                      </button>
                    );
                  })}
                  {questions.length === 0 && (
                    <p className="px-3 py-6 text-sm text-slate-400 text-center">Keine Fragen in diesem Modul.</p>
                  )}
                </div>
              </div>

              {selectedQuestion && (
                <div className="x-card p-6 md:p-8 animate-slideUp">
                  <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4 mb-6">
                    <div>
                      <div className="flex items-center gap-2.5 mb-1.5">
                        <h3 className="font-display text-xl font-bold text-slate-900 dark:text-white tracking-tight">{selectedQuestion.name}</h3>
                        <span className={`pill pill-${difficultyOf(selectedQuestion.marks).key}`}>{difficultyOf(selectedQuestion.marks).label}</span>
                        <span className="font-mono text-xs font-semibold text-slate-400">{selectedQuestion.marks} Pkt</span>
                      </div>
                      <button onClick={() => loadAttempts(selectedQuestion.id)} className="flex items-center gap-1.5 text-xs font-semibold text-brand-600 hover:text-brand-700 transition-colors">
                        <History size={13} /> Verlauf ansehen
                      </button>
                    </div>
                    <div className="flex gap-2">
                      <button
                        onClick={() => { setShowSchema(!showSchema); setShowCheatSheet(false); }}
                        className={`flex items-center gap-2 text-xs font-semibold px-3.5 py-2.5 rounded-xl border transition-all ${
                          showSchema
                            ? 'bg-brand-600 text-white border-brand-600 shadow-sm'
                            : 'text-brand-600 dark:text-brand-300 bg-brand-50 dark:bg-brand-500/10 border-brand-100 dark:border-brand-500/20 hover:bg-brand-100 dark:hover:bg-brand-500/20'
                        }`}
                      >
                        <Database size={15} /> {showSchema ? 'Schema aus' : 'Schema'}
                      </button>
                      <button
                        onClick={() => { setShowCheatSheet(!showCheatSheet); setShowSchema(false); }}
                        className={`flex items-center gap-2 text-xs font-semibold px-3.5 py-2.5 rounded-xl border transition-all ${
                          showCheatSheet
                            ? 'bg-slate-800 text-white border-slate-800 shadow-sm'
                            : 'text-slate-600 dark:text-slate-300 bg-slate-100 dark:bg-ink-soft border-slate-200 dark:border-ink-border hover:bg-slate-200 dark:hover:bg-ink-border'
                        }`}
                      >
                        <Code size={15} /> {showCheatSheet ? 'Cheat-Sheet aus' : 'Cheat-Sheet'}
                      </button>
                    </div>
                  </div>

                  {showCheatSheet && (
                    <div className="mb-6 p-5 rounded-2xl bg-slate-50 dark:bg-ink-bg border border-slate-200 dark:border-ink-border grid grid-cols-2 md:grid-cols-4 gap-5 animate-fadeIn">
                      {sqlHints.map(hint => (
                        <div key={hint.cmd} className="space-y-1.5">
                          <p className="font-mono text-xs font-bold text-brand-500">{hint.cmd}</p>
                          <p className="text-[11px] text-slate-500 dark:text-slate-400">{hint.desc}</p>
                          <p className="text-[10px] font-mono bg-white dark:bg-ink-card p-2 rounded-lg border border-slate-200 dark:border-ink-border text-slate-500">{hint.example}</p>
                        </div>
                      ))}
                    </div>
                  )}

                  {showSchema && schemaMetadata && (
                    <div className="mb-6 p-5 rounded-2xl bg-brand-50/50 dark:bg-brand-500/5 border border-brand-100 dark:border-brand-500/20 animate-fadeIn">
                      <h4 className="kicker text-brand-500 mb-3">Schema · {schemaMetadata.schemaName}</h4>
                      <SchemaVisualizer metadata={schemaMetadata} />
                    </div>
                  )}

                  <div className="rounded-2xl border border-slate-200 dark:border-ink-border overflow-hidden mb-5 h-[350px] shadow-inner"
                       data-lpignore="true" data-form-type="other" data-ignore-autofill="true">
                    <Editor
                      height="100%"
                      defaultLanguage="sql"
                      theme={isDark ? 'vs-dark' : 'light'}
                      value={sql}
                      onChange={(v) => setSql(v || '')}
                      onMount={handleEditorMount}
                      loading={<div className="flex items-center justify-center h-full bg-slate-50 dark:bg-ink-bg text-slate-400 font-mono text-xs animate-pulse">SQL-Editor wird geladen …</div>}
                      options={{
                        minimap: { enabled: false },
                        fontSize: 15,
                        fontFamily: "'JetBrains Mono', monospace",
                        lineNumbers: 'on',
                        padding: { top: 18, bottom: 18 },
                        automaticLayout: true,
                        suggestOnTriggerCharacters: true,
                        wordWrap: 'on',
                        quickSuggestions: { other: true, comments: false, strings: false },
                        parameterHints: { enabled: true },
                        formatOnType: true,
                        autoClosingBrackets: 'always',
                        folding: true,
                        scrollBeyondLastLine: false,
                        fixedOverflowWidgets: true,
                        renderLineHighlight: 'all',
                        cursorSmoothCaretAnimation: 'on',
                        smoothScrolling: true
                      }}
                    />
                  </div>

                  <button
                    onClick={submitSolution}
                    disabled={loading || !sql.trim()}
                    className="btn-primary w-full py-4 text-base group"
                  >
                    {loading ? <RefreshCw className="animate-spin" size={18} /> : <Send size={18} className="group-hover:translate-x-0.5 group-hover:-translate-y-0.5 transition-transform" />}
                    Antwort einreichen & prüfen
                  </button>

                  {gradingQuestionId === selectedQuestion.id && (
                    <div className="mt-6 p-5 rounded-2xl border border-brand-200 dark:border-brand-500/30 bg-brand-50/60 dark:bg-brand-500/10 animate-fadeIn flex items-center gap-4">
                      <RefreshCw className="animate-spin text-brand-500 shrink-0" size={22} />
                      <div>
                        <p className="text-sm font-semibold text-slate-800 dark:text-white">Wird bewertet …</p>
                        <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">Deine Abgabe wird automatisch geprüft. Das Ergebnis erscheint gleich.</p>
                      </div>
                    </div>
                  )}

                  {gradingQuestionId !== selectedQuestion.id && submissions.filter(s => s.questionId === selectedQuestion.id).sort((a,b) => new Date(b.submissionTime).getTime() - new Date(a.submissionTime).getTime()).slice(0, 1).map(s => (
                    <div key={s.submissionId} className="mt-6 p-6 rounded-2xl border border-slate-200 dark:border-ink-border bg-slate-50/60 dark:bg-ink-bg animate-fadeIn">
                      <div className="flex justify-between items-center mb-5">
                        <div className="flex items-center gap-2.5">
                          <div className="grid place-items-center h-9 w-9 rounded-lg bg-brand-600 text-white">
                            <Award size={18} />
                          </div>
                          <span className="kicker">Letztes Ergebnis</span>
                        </div>
                        <span className={`font-display text-2xl font-extrabold ${s.marks >= 1.0 ? 'text-easy' : s.marks > 0 ? 'text-medium' : 'text-hard'}`}>
                          {(s.marks * 100).toFixed(0)}%
                        </span>
                      </div>

                      {s.instructorFeedback && (
                        <div className="mb-5 p-4 rounded-xl bg-xp-500/10 border border-xp-500/20 flex items-start gap-3">
                          <MessageSquare className="text-xp-600 shrink-0" size={18} />
                          <div>
                            <p className="kicker text-xp-600 mb-1">Feedback vom Dozenten</p>
                            <p className="text-sm text-slate-700 dark:text-slate-200">{s.instructorFeedback}</p>
                          </div>
                        </div>
                      )}

                      <div className="bg-white dark:bg-ink-card rounded-xl p-5 border border-slate-200 dark:border-ink-border">
                        <MarkInfoDisplay markInfoJson={s.markInfoJson} />
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          ) : (
            <div className="flex flex-col items-center justify-center h-[460px] x-card bg-grid text-center px-6">
              <div className="grid place-items-center h-16 w-16 rounded-2xl bg-brand-50 dark:bg-brand-500/10 text-brand-500 mb-5">
                <BookOpen size={30} />
              </div>
              <p className="font-display text-lg font-bold text-slate-700 dark:text-slate-200">Wähle ein Modul</p>
              <p className="text-sm text-slate-400 mt-1.5 max-w-xs">Klicke links auf eine Aufgabe, um ihre Fragen zu laden und mit dem Lösen zu beginnen.</p>
            </div>
          )}
        </div>
      </div>

      {/* ── History modal ── */}
      {showHistory && selectedQuestion && (
        <div className="fixed inset-0 bg-slate-900/70 backdrop-blur-sm flex items-center justify-center z-50 p-4 animate-fadeIn" onClick={() => setShowHistory(false)}>
          <div className="x-card p-8 max-w-3xl w-full max-h-[85vh] overflow-y-auto relative animate-slideUp" onClick={e => e.stopPropagation()}>
            <button
              onClick={() => setShowHistory(false)}
              className="absolute top-6 right-6 grid place-items-center h-9 w-9 rounded-lg bg-slate-100 dark:bg-ink-soft text-slate-500 hover:bg-slate-200 dark:hover:bg-ink-border transition-colors"
            >
              <XCircle size={20} />
            </button>

            <div className="mb-7">
              <h3 className="font-display text-2xl font-bold text-slate-900 dark:text-white flex items-center gap-3">
                <History className="text-brand-500" size={24} /> Abgabe-Verlauf
              </h3>
              <p className="kicker mt-1.5 ml-9">{selectedQuestion.name}</p>
            </div>

            <div className="space-y-4">
              {attempts.map((attempt) => (
                <div key={attempt.submissionId} className="p-5 rounded-2xl bg-slate-50 dark:bg-ink-bg border border-slate-200 dark:border-ink-border">
                  <div className="flex justify-between items-center mb-4">
                    <span className="font-mono text-xs text-slate-400">
                      {new Date(attempt.submissionTime).toLocaleDateString()} · {new Date(attempt.submissionTime).getHours()}:{new Date(attempt.submissionTime).getMinutes().toString().padStart(2, '0')}
                    </span>
                    <span className={`font-display text-lg font-bold ${attempt.marks >= 1.0 ? 'text-easy' : attempt.marks > 0 ? 'text-medium' : 'text-hard'}`}>
                      {(attempt.marks * 100).toFixed(0)}%
                    </span>
                  </div>
                  <pre className="p-4 bg-white dark:bg-ink-card rounded-xl text-sm font-mono text-brand-700 dark:text-brand-300 overflow-x-auto border border-slate-200 dark:border-ink-border">
                    {attempt.query}
                  </pre>
                  {attempt.instructorFeedback && (
                    <div className="mt-3 p-3.5 rounded-xl bg-xp-500/10 border border-xp-500/20 text-xs flex items-start gap-2.5">
                      <MessageSquare size={15} className="text-xp-600 shrink-0" />
                      <div>
                        <span className="kicker text-xp-600 block mb-1">Feedback</span>
                        <span className="text-slate-700 dark:text-slate-200 leading-relaxed">{attempt.instructorFeedback}</span>
                      </div>
                    </div>
                  )}
                </div>
              ))}
              {attempts.length === 0 && (
                <p className="text-center py-10 text-sm text-slate-400">Keine vorherigen Abgaben gefunden.</p>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

const StatTile: React.FC<{ icon: React.ReactNode; label: string; value: string; sub: string; accent: 'brand' | 'xp' }> = ({ icon, label, value, sub, accent }) => (
  <div className="rounded-2xl bg-white/5 border border-white/10 backdrop-blur-sm px-4 py-3.5 min-w-[92px]">
    <div className={`flex items-center gap-1.5 ${accent === 'xp' ? 'text-xp-400' : 'text-brand-400'}`}>
      {icon}
      <span className="text-[10px] font-bold uppercase tracking-wider text-slate-400">{label}</span>
    </div>
    <p className="mt-1.5 font-display text-2xl font-extrabold text-white leading-none">{value}</p>
    <p className="mt-1 font-mono text-[10px] text-slate-500">{sub}</p>
  </div>
);

export default StudentDashboard;
