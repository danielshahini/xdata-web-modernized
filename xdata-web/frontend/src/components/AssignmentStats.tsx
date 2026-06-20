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
      if (!assignmentId) {
        return { summary: null as SummaryData | null, analytics: null as AnalyticsData | null, plagiarism: [] as PlagiarismResult[], allSubmissions: [] as Submission[] };
      }
      const [sumRes, anaRes, plagRes] = await Promise.all([
        api.get(`/assignments/${assignmentId}/summary`),
        api.get(`/assignments/${assignmentId}/analytics`),
        api.get(`/evaluation/plagiarism/${assignmentId}?threshold=0.8`)
      ]);
      const questionsRes = await api.get(`/assignments/${assignmentId}/questions`);
      const allSubs: Submission[] = [];
      for (const q of questionsRes.data) {
        const subs = await api.get(`/evaluation/submissions/${q.id}`);
        allSubs.push(...subs.data);
      }
      return {
        summary: sumRes.data as SummaryData,
        analytics: anaRes.data as AnalyticsData,
        plagiarism: plagRes.data as PlagiarismResult[],
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
      <RefreshCw className="animate-spin mb-4 text-blue-500" size={48} />
      <p className="font-black uppercase tracking-widest text-xs">Analyse-Daten werden aufbereitet...</p>
    </div>
  );

  if (!summary || !analytics) return (
    <div className="p-10 text-center bg-gray-50 dark:bg-gray-900 rounded-3xl border border-dashed border-gray-200 dark:border-gray-700">
      <Search className="mx-auto mb-4 opacity-20 dark:text-white" size={48} />
      <p className="text-gray-400 font-bold italic">Keine Assignment-Daten ausgewählt oder verfügbar.</p>
    </div>
  );

  return (
    <div className="space-y-8 animate-fadeIn">
      <div className="flex gap-4 border-b dark:border-gray-700 pb-2">
        <button 
          onClick={() => setActiveTab('overview')}
          className={`px-6 py-2 text-sm font-black uppercase tracking-widest transition-all ${activeTab === 'overview' ? 'text-blue-600 border-b-2 border-blue-600' : 'text-gray-400'}`}
        >
          Übersicht
        </button>
        <button 
          onClick={() => setActiveTab('submissions')}
          className={`px-6 py-2 text-sm font-black uppercase tracking-widest transition-all ${activeTab === 'submissions' ? 'text-blue-600 border-b-2 border-blue-600' : 'text-gray-400'}`}
        >
          Abgaben & Feedback
        </button>
        <button 
          onClick={() => setActiveTab('plagiarism')}
          className={`px-6 py-2 text-sm font-black uppercase tracking-widest transition-all ${activeTab === 'plagiarism' ? 'text-blue-600 border-b-2 border-blue-600' : 'text-gray-400'}`}
        >
          Plagiats-Check ({plagiarism.length})
        </button>
      </div>

      {activeTab === 'overview' && (
        <div className="space-y-8">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            <div className="bg-white dark:bg-gray-800 p-8 rounded-3xl shadow-sm border border-gray-100 dark:border-gray-700">
              <TrendingUp className="text-blue-500 mb-4" size={32} />
              <div className="text-[10px] font-black text-gray-400 uppercase tracking-widest mb-1">Ø Gesamt-Punkte</div>
              <div className="text-3xl font-black dark:text-white">
                {(analytics.questionAnalytics.reduce((acc, q) => acc + q.averageMarks, 0)).toFixed(1)}
              </div>
            </div>
            <div className="bg-white dark:bg-gray-800 p-8 rounded-3xl shadow-sm border border-gray-100 dark:border-gray-700">
              <FileSpreadsheet className="text-green-500 mb-4" size={32} />
              <div className="text-[10px] font-black text-gray-400 uppercase tracking-widest mb-1">Exportieren</div>
              <button 
                onClick={handleExport}
                className="mt-2 text-xs font-black text-blue-600 dark:text-blue-400 hover:underline flex items-center"
              >
                Als CSV herunterladen
              </button>
            </div>
            <div className="bg-white dark:bg-gray-800 p-8 rounded-3xl shadow-sm border border-gray-100 dark:border-gray-700">
              <AlertTriangle className={`mb-4 ${plagiarism.length > 0 ? 'text-red-500' : 'text-gray-300'}`} size={32} />
              <div className="text-[10px] font-black text-gray-400 uppercase tracking-widest mb-1">Plagiats-Verdacht</div>
              <div className="text-3xl font-black dark:text-white">{plagiarism.length} Fälle</div>
            </div>
          </div>

          <div className="bg-white dark:bg-gray-800 p-8 rounded-3xl shadow-sm border border-gray-100 dark:border-gray-700 h-[400px]">
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
        <div className="bg-white dark:bg-gray-800 rounded-3xl shadow-sm border dark:border-gray-700 overflow-hidden">
          <table className="w-full">
            <thead className="bg-gray-50 dark:bg-gray-900/50">
              <tr>
                <th className="px-6 py-4 text-left text-[10px] font-black text-gray-400 uppercase tracking-widest">Student</th>
                <th className="px-6 py-4 text-left text-[10px] font-black text-gray-400 uppercase tracking-widest">Aufgabe</th>
                <th className="px-6 py-4 text-center text-[10px] font-black text-gray-400 uppercase tracking-widest">Ergebnis</th>
                <th className="px-6 py-4 text-right text-[10px] font-black text-gray-400 uppercase tracking-widest">Feedback</th>
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
                    <span className={`text-xs font-black px-3 py-1 rounded-full ${s.marks >= 1.0 ? 'bg-green-100 text-green-700' : 'bg-yellow-100 text-yellow-700'}`}>
                      {(s.marks * 100).toFixed(0)}%
                    </span>
                  </td>
                  <td className="px-6 py-4 text-right">
                    {editingFeedbackId === s.submissionId ? (
                      <div className="flex gap-2">
                        <input 
                          className="text-xs p-2 border dark:border-gray-700 rounded-xl dark:bg-gray-900 dark:text-white"
                          value={feedbackText}
                          onChange={e => setFeedbackText(e.target.value)}
                          placeholder="Feedback..."
                        />
                        <button onClick={() => handleFeedback(s.submissionId)} className="text-green-500"><CheckCircle size={20}/></button>
                        <button onClick={() => setEditingFeedbackId(null)} className="text-red-500"><XCircle size={20}/></button>
                      </div>
                    ) : (
                      <button 
                        onClick={() => {setEditingFeedbackId(s.submissionId); setFeedbackText(s.instructorFeedback || '');}}
                        className={`text-sm font-bold flex items-center justify-end w-full ${s.instructorFeedback ? 'text-blue-500' : 'text-gray-300'}`}
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
            <div key={idx} className="bg-red-50/50 dark:bg-red-900/10 border border-red-100 dark:border-red-900/30 p-8 rounded-3xl">
              <div className="flex justify-between items-center mb-6">
                <div className="flex items-center">
                  <ShieldAlert className="text-red-500 mr-3" size={24} />
                  <span className="font-black dark:text-white">Verdacht: {p.student1} ↔ {p.student2}</span>
                </div>
                <span className="text-xl font-black text-red-600">{(p.similarity * 100).toFixed(1)}% Ähnlichkeit</span>
              </div>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div className="space-y-2">
                  <p className="text-[10px] font-black text-gray-400 uppercase tracking-widest">{p.student1}</p>
                  <pre className="p-4 bg-white dark:bg-gray-800 rounded-2xl text-xs font-mono dark:text-gray-300 overflow-x-auto border dark:border-gray-700">{p.query1}</pre>
                </div>
                <div className="space-y-2">
                  <p className="text-[10px] font-black text-gray-400 uppercase tracking-widest">{p.student2}</p>
                  <pre className="p-4 bg-white dark:bg-gray-800 rounded-2xl text-xs font-mono dark:text-gray-300 overflow-x-auto border dark:border-gray-700">{p.query2}</pre>
                </div>
              </div>
            </div>
          ))}
          {plagiarism.length === 0 && (
            <div className="text-center py-20 bg-gray-50 dark:bg-gray-900/50 rounded-3xl border border-dashed border-gray-200 dark:border-gray-700">
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
