import React, { useState, useEffect, useCallback } from 'react';
import api from '../api';
import Editor from '@monaco-editor/react';
import { toast } from 'react-hot-toast';
import WebSocketService from '../services/WebSocketService';
import { useAuth } from '../context/AuthContext';
import { 
  BookOpen, 
  CheckCircle, 
  Clock, 
  HelpCircle, 
  History, 
  Send, 
  ChevronRight,
  Award,
  RefreshCw,
  LayoutDashboard,
  Bell,
  Code,
  Trophy,
  Target,
  BarChart2,
  MessageSquare,
  Award as AwardIcon
} from 'lucide-react';
import { Assignment, Question, Submission, Announcement } from '../types';
import Skeleton from './common/Skeleton';
import MarkInfoDisplay from './MarkInfoDisplay';

interface DashboardData {
  studentName: string;
  courseName: string;
  xp: number;
  level: number;
  nextLevelXp: number;
  currentLevelXp: number;
  assignments: {
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
  const [schemaMetadata, setSchemaMetadata] = useState<any>(null);

  const { user } = useAuth();

  useEffect(() => {
    if (selectedQuestion?.assignment?.defaultSchemaId) {
      api.get(`/schemas/${selectedQuestion.assignment.defaultSchemaId}/metadata`)
        .then(res => setSchemaMetadata(res.data))
        .catch(() => setSchemaMetadata(null));
    } else {
      setSchemaMetadata(null);
    }
  }, [selectedQuestion]);

  useEffect(() => {
    let provider: any = null;
    if (schemaMetadata) {
      import('@monaco-editor/react').then(({ loader }) => {
        loader.init().then(monaco => {
          provider = monaco.languages.registerCompletionItemProvider('sql', {
            triggerCharacters: ['.', ' '],
            provideCompletionItems: (model, position) => {
              const word = model.getWordUntilPosition(position);
              const range = {
                startLineNumber: position.lineNumber,
                endLineNumber: position.lineNumber,
                startColumn: word.startColumn,
                endColumn: word.endColumn,
              };

              const suggestions: any[] = [];
              
              schemaMetadata.tables.forEach((table: any) => {
                suggestions.push({
                  label: table.tableName,
                  kind: monaco.languages.CompletionItemKind.Class,
                  insertText: table.tableName,
                  detail: 'Tabelle',
                  range
                });
                
                table.columns.forEach((col: any) => {
                  suggestions.push({
                    label: col.columnName,
                    kind: monaco.languages.CompletionItemKind.Field,
                    insertText: col.columnName,
                    detail: `${table.tableName} (${col.dataType})`,
                    range
                  });
                });
              });

              return { suggestions };
            }
          });
        });
      });
    }
    return () => {
      if (provider) provider.dispose();
    };
  }, [schemaMetadata]);

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
    setLoading(true);
    try {
      await api.post('/student/submit', {
        questionId: selectedQuestion.id,
        query: sql
      });
      toast.success("Abgabe erfolgreich! Bewertung läuft...");
      setSql('');
      loadSubmissions();
    } catch (e) {
      toast.error("Fehler bei der Abgabe");
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

  if (!dashboard) return <div className="p-8"><Skeleton count={3} /></div>;

  const totalAchieved = dashboard.assignments.reduce((acc, curr) => acc + curr.achievedMarks, 0);
  const totalMax = dashboard.assignments.reduce((acc, curr) => acc + curr.totalMarks, 0);
  const overallPercentage = totalMax > 0 ? (totalAchieved / totalMax) * 100 : 0;

  return (
    <div className="p-4 md:p-8 max-w-7xl mx-auto space-y-6 animate-fadeIn pb-20">
      <div className="bg-gradient-to-br from-blue-600 via-indigo-700 to-purple-800 p-8 rounded-[2rem] shadow-2xl text-white relative overflow-hidden">
        <div className="absolute top-0 right-0 w-64 h-64 bg-white/10 rounded-full -mr-32 -mt-32 blur-3xl"></div>
        <div className="absolute bottom-0 left-0 w-48 h-48 bg-blue-400/20 rounded-full -ml-24 -mb-24 blur-2xl"></div>
        
        <div className="relative z-10 flex flex-col md:flex-row justify-between items-start md:items-center gap-6">
          <div className="flex-1">
            <div className="flex items-center gap-4 mb-2">
               <h1 className="text-4xl font-black tracking-tight">Hallo, {dashboard.studentName}! 👋</h1>
               <div className="bg-yellow-400 text-blue-900 px-4 py-1 rounded-2xl font-black text-sm shadow-lg flex items-center gap-2">
                  <AwardIcon size={16} /> Level {dashboard.level}
               </div>
            </div>
            <p className="opacity-90 font-medium flex items-center text-lg mb-4">
              <LayoutDashboard size={20} className="mr-2" /> {dashboard.courseName || 'Willkommen zurück in Ihrem Lern-Dashboard'}
            </p>
            
            <div className="max-w-md bg-white/10 backdrop-blur-md rounded-2xl p-4 border border-white/10">
               <div className="flex justify-between text-[10px] font-black uppercase tracking-widest mb-2">
                  <span>XP: {dashboard.xp}</span>
                  <span>Nächstes Level: {dashboard.nextLevelXp}</span>
               </div>
               <div className="w-full bg-white/20 h-2 rounded-full overflow-hidden">
                  <div 
                    className="bg-yellow-400 h-full transition-all duration-1000" 
                    style={{ 
                      width: `${((dashboard.xp - dashboard.currentLevelXp) / (dashboard.nextLevelXp - dashboard.currentLevelXp)) * 100}%` 
                    }}
                  ></div>
               </div>
            </div>
          </div>
          <div className="flex gap-4">
            <div className="bg-white/20 p-5 rounded-3xl backdrop-blur-md border border-white/20 flex items-center shadow-xl">
              <Trophy className="text-yellow-400 mr-4" size={36} />
              <div>
                <p className="text-[10px] font-black uppercase tracking-widest opacity-80">Gesamt-Score</p>
                <p className="text-2xl font-black">{totalAchieved.toFixed(1)} / {totalMax.toFixed(1)}</p>
                <div className="w-full bg-white/30 h-1.5 rounded-full mt-2 overflow-hidden">
                  <div className="bg-yellow-400 h-full transition-all duration-1000" style={{ width: `${overallPercentage}%` }}></div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <div className="lg:col-span-1 space-y-8">
          {announcements.length > 0 && (
            <div className="bg-white dark:bg-gray-800 rounded-[2rem] p-6 shadow-xl shadow-blue-500/5 border border-gray-100 dark:border-gray-700">
              <h2 className="text-lg font-black mb-4 flex items-center dark:text-white uppercase tracking-wider text-[12px]">
                <Bell size={18} className="mr-2 text-blue-500" /> Ankündigungen
              </h2>
              <div className="space-y-4 max-h-[250px] overflow-y-auto pr-2 custom-scrollbar">
                {announcements.map(a => (
                  <div key={a.id} className="p-4 rounded-2xl bg-blue-50/50 dark:bg-blue-900/20 border border-blue-100 dark:border-blue-800">
                    <p className="font-bold text-blue-900 dark:text-blue-300 text-sm mb-1">{a.title}</p>
                    <p className="text-xs text-blue-700 dark:text-blue-400 mb-2 opacity-80">{a.content}</p>
                    <div className="flex justify-between items-center text-[10px] font-bold opacity-60">
                       <span>{a.course?.courseName}</span>
                       <span>{new Date(a.createdAt).toLocaleDateString()}</span>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          <div className="bg-white dark:bg-gray-800 rounded-[2rem] p-6 shadow-xl shadow-blue-500/5 border border-gray-100 dark:border-gray-700">
            <h2 className="text-lg font-black mb-6 flex items-center dark:text-white uppercase tracking-wider text-[12px]">
              <BookOpen size={18} className="mr-2 text-blue-500" /> Meine Aufgaben
            </h2>
            <div className="space-y-4">
              {dashboard.assignments.map(a => (
                <button
                  key={a.assignmentId}
                  onClick={() => {
                    setSelectedAssignment(a as any);
                    setSelectedQuestion(null);
                    loadQuestions(a.assignmentId);
                  }}
                  className={`w-full text-left p-5 rounded-3xl border transition-all relative overflow-hidden group ${
                    selectedAssignment?.id === a.assignmentId 
                    ? 'border-blue-500 bg-blue-50 dark:bg-blue-900/30 ring-4 ring-blue-500/10' 
                    : 'border-gray-100 dark:border-gray-700 hover:border-blue-200 dark:hover:border-blue-800'
                  }`}
                >
                  <div className="flex justify-between items-start mb-3">
                    <div>
                      <span className="font-black dark:text-white text-base block mb-1 group-hover:text-blue-600 dark:group-hover:text-blue-400 transition-colors">{a.name}</span>
                      <div className="flex items-center gap-3 text-[10px] font-bold uppercase tracking-widest text-gray-400">
                        <span className="flex items-center"><Clock size={12} className="mr-1" /> {new Date(a.deadline).toLocaleDateString()}</span>
                        <span className="flex items-center text-blue-500"><Target size={12} className="mr-1" /> {a.solvedQuestions}/{a.totalQuestions} gelöst</span>
                      </div>
                    </div>
                    <div className="text-right">
                      <span className="text-xs font-black text-blue-600 dark:text-blue-400 block">{a.percentage.toFixed(0)}%</span>
                      <ChevronRight size={18} className="text-gray-300 ml-auto mt-1" />
                    </div>
                  </div>
                  <div className="w-full bg-gray-200 dark:bg-gray-700 h-2 rounded-full overflow-hidden">
                    <div 
                      className="bg-blue-500 h-full transition-all duration-700 ease-out" 
                      style={{ width: `${a.percentage}%` }}
                    ></div>
                  </div>
                </button>
              ))}
            </div>
          </div>
        </div>

        <div className="lg:col-span-2 space-y-8">
          {selectedAssignment ? (
            <div className="animate-slideUp space-y-8">
              <div className="bg-white dark:bg-gray-800 rounded-[2rem] p-8 shadow-xl shadow-blue-500/5 border border-gray-100 dark:border-gray-700">
                <div className="flex justify-between items-center mb-6">
                   <h3 className="text-sm font-black dark:text-white uppercase tracking-widest text-gray-400 flex items-center">
                     <BarChart2 size={16} className="mr-2" /> Fragen in {selectedAssignment.name}
                   </h3>
                   <div className="flex items-center gap-2">
                     <span className="px-3 py-1 bg-blue-50 dark:bg-blue-900/30 text-blue-600 dark:text-blue-400 rounded-full text-[10px] font-black uppercase">
                       {selectedAssignment.totalQuestions} Aufgaben
                     </span>
                     <span className="px-3 py-1 bg-indigo-50 dark:bg-indigo-900/30 text-indigo-600 dark:text-indigo-400 rounded-full text-[10px] font-black uppercase">
                       {selectedAssignment.totalMarks} Punkte Gesamt
                     </span>
                   </div>
                </div>
                
                <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-4">
                  {questions.map((q, idx) => {
                    const status = getQuestionStatus(q.id);
                    return (
                      <button
                        key={q.id}
                        onClick={() => {
                          setSelectedQuestion(q);
                          setSql('');
                        }}
                        className={`p-6 rounded-[1.5rem] border flex flex-col items-center transition-all relative ${
                          selectedQuestion?.id === q.id
                          ? 'border-blue-500 bg-blue-50 dark:bg-blue-900/30 shadow-lg shadow-blue-500/10'
                          : 'border-gray-100 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-700/50'
                        }`}
                      >
                        <span className="text-[10px] font-black text-gray-400 mb-2 uppercase">Nr. {idx + 1}</span>
                        <span className="font-black text-sm dark:text-white mb-3 text-center line-clamp-1">{q.name}</span>
                        
                        <div className="relative">
                          {status === 'SOLVED' ? (
                            <div className="p-2 bg-green-100 dark:bg-green-900/30 rounded-full text-green-600 dark:text-green-400">
                              <CheckCircle size={20} />
                            </div>
                          ) : status === 'PARTIAL' ? (
                            <div className="p-2 bg-yellow-100 dark:bg-yellow-900/30 rounded-full text-yellow-600 dark:text-yellow-400">
                              <RefreshCw size={20} className="animate-spin-slow" />
                            </div>
                          ) : status === 'FAILED' ? (
                             <div className="p-2 bg-red-100 dark:bg-red-900/30 rounded-full text-red-600 dark:text-red-400">
                               <XCircle size={20} />
                             </div>
                          ) : (
                            <div className="p-2 bg-gray-100 dark:bg-gray-800 rounded-full text-gray-300">
                              <HelpCircle size={20} />
                            </div>
                          )}
                        </div>
                        <span className="mt-3 text-[9px] font-black text-gray-400 uppercase tracking-widest">{q.marks} Pkt.</span>
                      </button>
                    );
                  })}
                </div>
              </div>

              {selectedQuestion && (
                <div className="bg-white dark:bg-gray-800 rounded-[2rem] p-10 shadow-2xl shadow-blue-500/10 border border-gray-100 dark:border-gray-700 animate-slideUp">
                  <div className="flex flex-col md:flex-row justify-between items-start md:items-center mb-8 gap-4">
                    <div>
                      <div className="flex items-center gap-3 mb-1">
                        <h3 className="text-2xl font-black dark:text-white tracking-tight">{selectedQuestion.name}</h3>
                        <span className="bg-blue-500 text-white text-[10px] font-black px-3 py-1 rounded-full uppercase tracking-widest shadow-lg shadow-blue-500/30">
                          {selectedQuestion.marks} Punkte
                        </span>
                      </div>
                      <div className="flex items-center text-[10px] font-bold uppercase tracking-widest text-gray-400">
                        <button onClick={() => loadAttempts(selectedQuestion.id)} className="flex items-center text-blue-500 hover:text-blue-600 transition-colors">
                          <History size={12} className="mr-1" /> Verlauf ansehen
                        </button>
                      </div>
                    </div>
                    <button 
                       onClick={() => setShowCheatSheet(!showCheatSheet)}
                       className="flex items-center text-xs font-black text-indigo-600 dark:text-indigo-400 bg-indigo-50 dark:bg-indigo-900/30 px-5 py-3 rounded-2xl border border-indigo-100 dark:indigo-800 hover:bg-indigo-100 transition-all"
                    >
                       <Code size={16} className="mr-2" /> {showCheatSheet ? 'Vorschau schließen' : 'SQL Cheat Sheet'}
                    </button>
                  </div>

                  {showCheatSheet && (
                    <div className="mb-8 p-6 rounded-3xl bg-gray-50 dark:bg-gray-900/50 border border-gray-100 dark:border-gray-700 grid grid-cols-2 md:grid-cols-4 gap-6 animate-fadeIn">
                       {sqlHints.map(hint => (
                         <div key={hint.cmd} className="space-y-1 group">
                            <p className="text-[10px] font-black text-blue-500 uppercase tracking-widest group-hover:scale-105 transition-transform origin-left">{hint.cmd}</p>
                            <p className="text-[10px] dark:text-gray-400 font-medium">{hint.desc}</p>
                            <p className="text-[9px] font-mono bg-white dark:bg-gray-800 p-2 rounded-xl border dark:border-gray-700 mt-2 shadow-sm text-gray-500">{hint.example}</p>
                         </div>
                       ))}
                    </div>
                  )}

                  <div className="rounded-[2rem] border-4 border-gray-50 dark:border-gray-900 overflow-hidden mb-8 h-[350px] shadow-2xl relative" 
                       data-lpignore="true" 
                       data-form-type="other"
                       data-ignore-autofill="true">
                    <Editor
                      height="100%"
                      defaultLanguage="sql"
                      theme="vs-dark"
                      value={sql}
                      onChange={(v) => setSql(v || '')}
                      loading={<div className="flex items-center justify-center h-full dark:bg-gray-900 dark:text-gray-400 font-black uppercase tracking-widest text-xs animate-pulse">Initialisiere SQL Editor...</div>}
                      options={{
                        minimap: { enabled: false },
                        fontSize: 15,
                        fontFamily: "'JetBrains Mono', monospace",
                        lineNumbers: 'on',
                        padding: { top: 20, bottom: 20 },
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

                  <div className="flex gap-4">
                    <button
                      onClick={submitSolution}
                      disabled={loading || !sql.trim()}
                      className="flex-1 bg-gradient-to-r from-blue-600 to-indigo-700 hover:from-blue-700 hover:to-indigo-800 text-white py-5 rounded-3xl font-black shadow-2xl shadow-blue-500/20 transition-all flex items-center justify-center disabled:opacity-50 group active:scale-[0.98]"
                    >
                      {loading ? <RefreshCw className="animate-spin mr-3" /> : <Send className="mr-3 group-hover:translate-x-1 group-hover:-translate-y-1 transition-transform" />}
                      Antwort überprüfen & einreichen
                    </button>
                  </div>

                  {submissions.filter(s => s.questionId === selectedQuestion.id).sort((a,b) => new Date(b.submissionTime).getTime() - new Date(a.submissionTime).getTime()).slice(0, 1).map(s => (
                    <div key={s.submissionId} className="mt-10 p-8 rounded-[2rem] border-2 border-blue-50 dark:border-blue-900/30 bg-blue-50/20 dark:bg-blue-900/10 animate-fadeIn">
                      <div className="flex justify-between items-center mb-6">
                        <div className="flex items-center gap-3">
                           <div className="p-2 bg-blue-500 text-white rounded-xl shadow-lg shadow-blue-500/30">
                             <Award size={20} />
                           </div>
                           <span className="text-sm font-black uppercase tracking-widest dark:text-white">Dein letztes Ergebnis</span>
                        </div>
                        <span className={`text-2xl font-black ${s.marks >= 1.0 ? 'text-green-500' : s.marks > 0 ? 'text-yellow-500' : 'text-red-500'}`}>
                          {(s.marks * 100).toFixed(0)}% Korrekt
                        </span>
                      </div>
                      
                      {s.instructorFeedback && (
                         <div className="mb-6 p-5 rounded-2xl bg-amber-50 dark:bg-amber-900/20 border border-amber-100 dark:border-amber-800 flex items-start shadow-sm">
                            <MessageSquare className="text-amber-500 mr-4 shrink-0" size={20} />
                            <div>
                               <p className="text-[10px] font-black text-amber-600 dark:text-amber-500 uppercase mb-1 tracking-widest">Feedback vom Dozenten</p>
                               <p className="text-sm font-medium dark:text-amber-200">{s.instructorFeedback}</p>
                            </div>
                         </div>
                      )}

                      <div className="bg-white dark:bg-gray-800/50 rounded-2xl p-6 border dark:border-gray-700">
                         <MarkInfoDisplay markInfoJson={s.markInfoJson} />
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          ) : (
            <div className="flex flex-col items-center justify-center h-[500px] bg-white dark:bg-gray-800 rounded-[3rem] border-4 border-dashed border-gray-100 dark:border-gray-700 text-gray-400 group transition-all hover:border-blue-100 dark:hover:border-blue-900/30">
              <div className="relative mb-6">
                <BookOpen size={80} className="opacity-10 group-hover:opacity-20 transition-opacity" />
                <div className="absolute inset-0 flex items-center justify-center">
                   <ChevronRight size={32} className="text-blue-500 opacity-20 group-hover:translate-x-2 transition-transform" />
                </div>
              </div>
              <p className="text-xl font-black uppercase tracking-widest opacity-40">Wählen Sie ein Assignment</p>
              <p className="text-sm font-bold mt-2 opacity-30">Klicken Sie links auf eine Aufgabe, um die Fragen zu laden</p>
            </div>
          )}
        </div>
      </div>

      {showHistory && selectedQuestion && (
        <div className="fixed inset-0 bg-black/80 backdrop-blur-md flex items-center justify-center z-50 p-4 animate-fadeIn">
          <div className="bg-white dark:bg-gray-800 rounded-[2.5rem] p-10 max-w-4xl w-full max-h-[85vh] overflow-y-auto shadow-2xl border border-white/10 dark:border-gray-700 custom-scrollbar relative">
            <button 
              onClick={() => setShowHistory(false)}
              className="absolute top-8 right-8 bg-gray-100 dark:bg-gray-700 p-3 rounded-2xl text-gray-500 dark:text-gray-300 hover:bg-gray-200 hover:scale-110 transition-all z-10"
            >
              <XCircle size={24} />
            </button>
            
            <div className="mb-10">
              <h3 className="text-3xl font-black dark:text-white flex items-center tracking-tight">
                <History className="mr-4 text-blue-500" size={32} /> Abgabe-Verlauf
              </h3>
              <p className="text-gray-400 font-bold uppercase tracking-widest text-xs mt-2 ml-12">Alle Versuche für: {selectedQuestion.name}</p>
            </div>

            <div className="space-y-8">
              {attempts.map((attempt) => (
                <div key={attempt.submissionId} className="p-8 rounded-3xl bg-gray-50/50 dark:bg-gray-900 border border-gray-100 dark:border-gray-700 hover:border-blue-200 transition-all group">
                  <div className="flex justify-between items-center mb-6">
                    <div className="flex items-center gap-4">
                       <div className="w-10 h-10 rounded-full bg-white dark:bg-gray-800 flex items-center justify-center shadow-sm border dark:border-gray-700 font-black text-xs text-gray-400">
                          {new Date(attempt.submissionTime).getHours()}:{new Date(attempt.submissionTime).getMinutes().toString().padStart(2, '0')}
                       </div>
                       <span className="text-xs font-black text-gray-400 uppercase tracking-widest">{new Date(attempt.submissionTime).toLocaleDateString()}</span>
                    </div>
                    <span className={`text-lg font-black ${attempt.marks >= 1.0 ? 'text-green-500' : attempt.marks > 0 ? 'text-yellow-500' : 'text-red-500'}`}>
                      {(attempt.marks * 100).toFixed(0)}%
                    </span>
                  </div>
                  <div className="relative group/code">
                    <pre className="p-5 bg-white dark:bg-gray-800 rounded-2xl text-sm font-mono dark:text-blue-300 overflow-x-auto mb-4 border dark:border-gray-700 shadow-inner">
                      {attempt.query}
                    </pre>
                  </div>
                  {attempt.instructorFeedback && (
                     <div className="p-4 rounded-xl bg-amber-50 dark:bg-amber-900/20 border border-amber-100 dark:border-amber-800 text-xs flex items-start">
                        <MessageSquare size={16} className="text-amber-500 mr-3 shrink-0" />
                        <div>
                           <span className="font-black text-amber-600 uppercase tracking-widest text-[9px] block mb-1">Feedback:</span>
                           <span className="font-medium dark:text-amber-200 leading-relaxed">{attempt.instructorFeedback}</span>
                        </div>
                     </div>
                  )}
                </div>
              ))}
              {attempts.length === 0 && (
                <div className="text-center py-10 opacity-30">
                  <p className="font-black italic">Keine vorherigen Abgaben gefunden.</p>
                </div>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default StudentDashboard;

const XCircle = ({ size, className }: { size?: number, className?: string }) => (
  <svg 
    width={size || 24} 
    height={size || 24} 
    viewBox="0 0 24 24" 
    fill="none" 
    stroke="currentColor" 
    strokeWidth="2.5" 
    strokeLinecap="round" 
    strokeLinejoin="round" 
    className={className}
  >
    <circle cx="12" cy="12" r="10" />
    <path d="m15 9-6 6" />
    <path d="m9 9 6 6" />
  </svg>
);
