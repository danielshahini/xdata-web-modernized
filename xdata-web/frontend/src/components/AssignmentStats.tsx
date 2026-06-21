import React, { useState } from 'react';
import { useAsyncData } from '../hooks/useAsyncData';
import api from '../api';
import { 
  BarChart as RechartsBarChart, 
  Bar, 
  XAxis, 
  YAxis, 
  CartesianGrid, 
  Tooltip, 
  ResponsiveContainer,
  Cell
} from 'recharts';
import { 
  TrendingUp, 
  FileSpreadsheet, 
  AlertTriangle, 
  RefreshCw,
  Search,
  MessageSquare,
  CheckCircle,
  XCircle,
  ShieldAlert
} from 'lucide-react';
import { Submission } from '../types';
import { toast } from 'react-hot-toast';

interface SummaryData {
  assignmentId: number;
  assignmentName: string;
  results: {
    studentId: string;
    studentName: string;
    totalMarks: number;
    questionMarks: {
      questionId: number;
      questionName: string;
      marks: number;
    }[];
  }[];
}

interface AnalyticsData {
  assignmentId: number;
  questionAnalytics: {
    questionId: number;
    questionName: string;
    totalSubmissions: number;
    uniqueStudents: number;
    averageMarks: number;
    perfectScores: number;
    commonErrors: {
      errorType: string;
      count: number;
    }[];
  }[];
}

interface PlagiarismResult {
  student1: string;
  student2: string;
  similarity: number;
  query1: string;
  query2: string;
}

interface StatsProps {
  assignmentId?: number;
}

const AssignmentStats: React.FC<StatsProps> = ({ assignmentId }) => {
  const [activeTab, setActiveTab] = useState<'overview' | 'submissions' | 'plagiarism'>('overview');
  const [feedbackText, setFeedbackText] = useState('');
  const [editingFeedbackId, setEditingFeedbackId] = useState<number | null>(null);

  const { data, loading, retry: reloadAll } = useAsyncData(
    async () => {
      const empty = { summary: null as SummaryData | null, analytics: null as AnalyticsData | null, plagiarism: [] as PlagiarismResult[], allSubmissions: [] as Submission[] };
      if (!assignmentId) return empty;

      // The backend exposes per-question stats and per-question submissions; the
      // former /summary and /analytics endpoints don't exist (they returned 500).
      // We aggregate summary + analytics on the client from the submissions.
      const questionsRes = await api.get(`/assignments/${assignmentId}/questions`);
      const questions: any[] = questionsRes.data || [];

      const plagRes = await api
        .get(`/evaluation/plagiarism/${assignmentId}?threshold=0.8`)
        .catch(() => ({ data: [] as PlagiarismResult[] }));

      const allSubs: Submission[] = [];
      for (const q of questions) {
        const subs = await api.get(`/evaluation/submissions/${q.id}`).catch(() => ({ data: [] }));
        allSubs.push(...((subs.data || []) as Submission[]));
      }

      const sid = (s: any) => s.studentId ?? s.user?.loginId ?? 'unbekannt';
      const sname = (s: any) => s.user?.username ?? s.studentId ?? 'Unbekannt';
      const qOf = (s: any) => s.questionId ?? s.question?.id;

      // Per-question analytics, based on each student's best attempt.
      const questionAnalytics = questions.map((q: any) => {
        const qSubs = allSubs.filter(s => qOf(s) === q.id);
        const bestByStudent = new Map<string, number>();
        qSubs.forEach(s => {
          const cur = bestByStudent.get(sid(s)) ?? -1;
          if (((s as any).marks ?? 0) > cur) bestByStudent.set(sid(s), (s as any).marks ?? 0);
        });
        const fractions = Array.from(bestByStudent.values());
        const maxPts = q.marks ?? 0;
        const avgFraction = fractions.length ? fractions.reduce((a, b) => a + b, 0) / fractions.length : 0;
        return {
          questionId: q.id,
          questionName: q.name,
          totalSubmissions: qSubs.length,
          uniqueStudents: bestByStudent.size,
          averageMarks: +(avgFraction * maxPts).toFixed(2),
          perfectScores: fractions.filter(f => f >= 1).length,
          commonErrors: [] as { errorType: string; count: number }[],
        };
      });

      // Per-student summary (best attempt per question).
      const byStudent = new Map<string, any>();
      allSubs.forEach(s => {
        const id = sid(s);
        const q = questions.find((qq: any) => qq.id === qOf(s));
        const awarded = ((s as any).marks ?? 0) * (q?.marks ?? 0);
        if (!byStudent.has(id)) byStudent.set(id, { studentId: id, studentName: sname(s), qm: new Map() });
        const e = byStudent.get(id);
        const prev = e.qm.get(qOf(s));
        if (!prev || awarded > prev.marks) e.qm.set(qOf(s), { questionId: qOf(s), questionName: q?.name ?? '', marks: awarded });
      });
      const results = Array.from(byStudent.values()).map(e => ({
        studentId: e.studentId,
        studentName: e.studentName,
        questionMarks: Array.from(e.qm.values()),
        totalMarks: Array.from(e.qm.values()).reduce((a: number, x: any) => a + x.marks, 0),
      }));

      return {
        summary: { assignmentId, assignmentName: '', results } as SummaryData,
        analytics: { assignmentId, questionAnalytics } as AnalyticsData,
        plagiarism: (plagRes.data || []) as PlagiarismResult[],
        allSubmissions: allSubs,
      };
    },
    [assignmentId]
  );
  const summary = data?.summary ?? null;
  const analytics = data?.analytics ?? null;
  const plagiarism = data?.plagiarism ?? [];
  const allSubmissions = data?.allSubmissions ?? [];

  const handleFeedback = async (submissionId: number) => {
    try {
      await api.post(`/evaluation/submissions/${submissionId}/feedback`, { feedback: feedbackText });
      toast.success("Feedback gespeichert");
      setEditingFeedbackId(null);
      setFeedbackText('');
      reloadAll();
    } catch (e) {
      toast.error("Fehler beim Speichern");
    }
  };

  const handleExport = async () => {
    if (!assignmentId) return;
    try {
      const res = await api.get(`/assignments/${assignmentId}/export`, { responseType: 'blob' });
      const url = window.URL.createObjectURL(new Blob([res.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `assignment_${assignmentId}_results.csv`);
      document.body.appendChild(link);
      link.click();
      link.remove();
    } catch (e) {
      toast.error("Fehler beim Exportieren");
    }
  };

  if (loading) return (
    <div className="p-8 text-center flex flex-col items-center justify-center dark:text-gray-400">
      <RefreshCw className="animate-spin mb-4 text-brand-500" size={48} />
      <p className="font-bold uppercase tracking-widest text-xs">Analyse-Daten werden aufbereitet...</p>
    </div>
  );

  if (!summary || !analytics) return (
    <div className="p-10 text-center bg-gray-50 dark:bg-ink-soft rounded-2xl border border-dashed border-gray-200 dark:border-ink-border">
      <Search className="mx-auto mb-4 opacity-20 dark:text-white" size={48} />
      <p className="text-gray-400 font-bold italic">Keine Aufgabe ausgewählt.</p>
      <p className="text-gray-400 text-sm mt-2">Öffne die Statistik über das Diagramm-Symbol einer Aufgabe in der Aufgaben-Liste.</p>
    </div>
  );

  return (
    <div className="space-y-8 animate-fadeIn">
      <div className="flex flex-wrap gap-1 bg-slate-100 dark:bg-ink-soft p-1 rounded-xl border border-slate-200 dark:border-ink-border w-fit">
        <button onClick={() => setActiveTab('overview')} className={`x-tab ${activeTab === 'overview' ? 'x-tab-active' : ''}`}>
          Übersicht
        </button>
        <button onClick={() => setActiveTab('submissions')} className={`x-tab ${activeTab === 'submissions' ? 'x-tab-active' : ''}`}>
          Abgaben &amp; Feedback
        </button>
        <button onClick={() => setActiveTab('plagiarism')} className={`x-tab ${activeTab === 'plagiarism' ? 'x-tab-active' : ''}`}>
          Plagiats-Check ({plagiarism.length})
        </button>
      </div>

      {activeTab === 'overview' && (
        <div className="space-y-8">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            <div className="x-card p-8">
              <TrendingUp className="text-brand-500 mb-4" size={32} />
              <div className="text-[10px] font-bold text-gray-400 uppercase tracking-widest mb-1">Ø Gesamt-Punkte</div>
              <div className="text-3xl font-bold dark:text-white">
                {(analytics.questionAnalytics.reduce((acc, q) => acc + q.averageMarks, 0)).toFixed(1)}
              </div>
            </div>
            <div className="x-card p-8">
              <FileSpreadsheet className="text-green-500 mb-4" size={32} />
              <div className="text-[10px] font-bold text-gray-400 uppercase tracking-widest mb-1">Exportieren</div>
              <button 
                onClick={handleExport}
                className="mt-2 text-xs font-bold text-brand-600 dark:text-brand-400 hover:underline flex items-center"
              >
                Als CSV herunterladen
              </button>
            </div>
            <div className="x-card p-8">
              <AlertTriangle className={`mb-4 ${plagiarism.length > 0 ? 'text-red-500' : 'text-gray-300'}`} size={32} />
              <div className="text-[10px] font-bold text-gray-400 uppercase tracking-widest mb-1">Plagiats-Verdacht</div>
              <div className="text-3xl font-bold dark:text-white">{plagiarism.length} Fälle</div>
            </div>
          </div>

          <div className="x-card p-8 h-[400px]">
              <ResponsiveContainer width="100%" height="100%">
                <RechartsBarChart data={analytics.questionAnalytics}>
                  <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f0f0f0" />
                  <XAxis dataKey="questionName" axisLine={false} tickLine={false} tick={{fontSize: 12, fontWeight: 'bold'}} />
                  <YAxis axisLine={false} tickLine={false} tick={{fontSize: 12, fontWeight: 'bold'}} />
                  <Tooltip 
                    contentStyle={{ borderRadius: '16px', border: 'none', boxShadow: '0 10px 15px -3px rgba(0, 0, 0, 0.1)' }}
                    cursor={{fill: '#f8fafc'}}
                  />
                  <Bar dataKey="averageMarks" radius={[10, 10, 0, 0]}>
                    {analytics.questionAnalytics.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={index % 2 === 0 ? '#3b82f6' : '#6366f1'} />
                    ))}
                  </Bar>
                </RechartsBarChart>
              </ResponsiveContainer>
          </div>
        </div>
      )}

      {activeTab === 'submissions' && (
        <div className="x-card overflow-hidden">
          <table className="w-full">
            <thead className="bg-gray-50 dark:bg-ink-soft/50">
              <tr>
                <th className="px-6 py-4 text-left text-[10px] font-bold text-gray-400 uppercase tracking-widest">Student</th>
                <th className="px-6 py-4 text-left text-[10px] font-bold text-gray-400 uppercase tracking-widest">Aufgabe</th>
                <th className="px-6 py-4 text-center text-[10px] font-bold text-gray-400 uppercase tracking-widest">Ergebnis</th>
                <th className="px-6 py-4 text-right text-[10px] font-bold text-gray-400 uppercase tracking-widest">Feedback</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100 dark:divide-gray-700">
              {allSubmissions.map(s => (
                <tr key={s.submissionId} className="hover:bg-gray-50 dark:hover:bg-gray-700/30 transition-colors">
                  <td className="px-6 py-4">
                    <p className="font-bold dark:text-white">{s.studentId}</p>
                  </td>
                  <td className="px-6 py-4 text-sm dark:text-gray-300">
                    {s.query.substring(0, 50)}...
                  </td>
                  <td className="px-6 py-4 text-center">
                    <span className={`text-xs font-bold px-3 py-1 rounded-full ${s.marks >= 1.0 ? 'bg-green-100 text-green-700' : 'bg-yellow-100 text-yellow-700'}`}>
                      {(s.marks * 100).toFixed(0)}%
                    </span>
                  </td>
                  <td className="px-6 py-4 text-right">
                    {editingFeedbackId === s.submissionId ? (
                      <div className="flex gap-2">
                        <input
                          className="x-input text-xs py-2"
                          value={feedbackText}
                          onChange={e => setFeedbackText(e.target.value)}
                          placeholder="Feedback..."
                        />
                        <button onClick={() => handleFeedback(s.submissionId)} className="icon-btn hover:text-easy"><CheckCircle size={20}/></button>
                        <button onClick={() => setEditingFeedbackId(null)} className="icon-btn hover:text-hard"><XCircle size={20}/></button>
                      </div>
                    ) : (
                      <button 
                        onClick={() => {setEditingFeedbackId(s.submissionId); setFeedbackText(s.instructorFeedback || '');}}
                        className={`text-sm font-bold flex items-center justify-end w-full ${s.instructorFeedback ? 'text-brand-500' : 'text-gray-300'}`}
                      >
                        <MessageSquare size={16} className="mr-2" />
                        {s.instructorFeedback ? 'Bearbeiten' : 'Feedback geben'}
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {activeTab === 'plagiarism' && (
        <div className="space-y-4">
          {plagiarism.map((p, idx) => (
            <div key={idx} className="bg-red-50/50 dark:bg-red-900/10 border border-red-100 dark:border-red-900/30 p-8 rounded-2xl">
              <div className="flex justify-between items-center mb-6">
                <div className="flex items-center">
                  <ShieldAlert className="text-red-500 mr-3" size={24} />
                  <span className="font-bold dark:text-white">Verdacht: {p.student1} ↔ {p.student2}</span>
                </div>
                <span className="text-xl font-bold text-red-600">{(p.similarity * 100).toFixed(1)}% Ähnlichkeit</span>
              </div>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div className="space-y-2">
                  <p className="text-[10px] font-bold text-gray-400 uppercase tracking-widest">{p.student1}</p>
                  <pre className="p-4 bg-white dark:bg-ink-card rounded-2xl text-xs font-mono dark:text-gray-300 overflow-x-auto border dark:border-ink-border">{p.query1}</pre>
                </div>
                <div className="space-y-2">
                  <p className="text-[10px] font-bold text-gray-400 uppercase tracking-widest">{p.student2}</p>
                  <pre className="p-4 bg-white dark:bg-ink-card rounded-2xl text-xs font-mono dark:text-gray-300 overflow-x-auto border dark:border-ink-border">{p.query2}</pre>
                </div>
              </div>
            </div>
          ))}
          {plagiarism.length === 0 && (
            <div className="text-center py-20 bg-gray-50 dark:bg-ink-soft/50 rounded-2xl border border-dashed border-gray-200 dark:border-ink-border">
               <CheckCircle className="mx-auto mb-4 text-green-500 opacity-20" size={48} />
               <p className="text-gray-400 font-bold italic">Keine auffälligen Ähnlichkeiten gefunden.</p>
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default AssignmentStats;
