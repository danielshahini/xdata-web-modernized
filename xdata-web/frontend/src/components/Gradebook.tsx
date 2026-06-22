import React, { useState, useEffect } from 'react';
import { useAsyncData } from '../hooks/useAsyncData';
import api from '../api';
import { toast } from 'react-hot-toast';
import { Course } from '../types';
import { BookOpen, Download, RefreshCw } from 'lucide-react';

interface Gb {
  students: { loginId: string; username: string }[];
  assignments: { id: number; name: string; totalMarks: number }[];
  grades: Record<string, Record<number, number>>;
}

const Gradebook: React.FC = () => {
  const { data: coursesData } = useAsyncData<Course[]>(() => api.get('/admin/courses').then(r => r.data || []), []);
  const courses = coursesData ?? [];
  const [courseId, setCourseId] = useState<number | null>(null);
  const [gb, setGb] = useState<Gb | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => { if (courses.length && courseId === null) setCourseId(courses[0].id); }, [courses, courseId]);

  useEffect(() => {
    if (courseId === null) return;
    setLoading(true);
    api.get(`/admin/courses/${courseId}/gradebook`)
      .then(r => setGb(r.data))
      .catch(() => toast.error('Failed to load gradebook'))
      .finally(() => setLoading(false));
  }, [courseId]);

  const exportCsv = async () => {
    if (courseId === null) return;
    try {
      const r = await api.get(`/admin/courses/${courseId}/gradebook/export`, { responseType: 'blob' });
      const url = URL.createObjectURL(r.data);
      const a = document.createElement('a');
      a.href = url; a.download = `gradebook.csv`;
      document.body.appendChild(a); a.click(); a.remove(); URL.revokeObjectURL(url);
    } catch { toast.error('Export failed'); }
  };

  const pct = (achieved: number, total: number) => total > 0 ? Math.round((achieved / total) * 100) : 0;
  const tint = (p: number) => p >= 100 ? 'text-easy' : p > 0 ? 'text-medium' : 'text-slate-400';

  return (
    <div className="space-y-6">
      <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
        <div>
          <p className="kicker">Grading</p>
          <h2 className="mt-1 section-title text-2xl flex items-center gap-2"><BookOpen size={22} className="text-brand-500" /> Gradebook</h2>
        </div>
        <div className="flex items-center gap-2">
          <select className="x-select w-auto" value={courseId ?? ''} onChange={e => setCourseId(Number(e.target.value))}>
            {courses.map(c => <option key={c.id} value={c.id}>{c.courseName} ({c.instructorCourseId})</option>)}
          </select>
          <button onClick={exportCsv} className="btn-secondary" disabled={!gb}><Download size={16} /> CSV</button>
        </div>
      </div>

      <div className="x-card overflow-x-auto">
        {loading ? (
          <div className="p-12 flex justify-center text-slate-400"><RefreshCw className="animate-spin" size={28} /></div>
        ) : gb && gb.students.length > 0 ? (
          <table className="w-full text-left border-collapse">
            <thead>
              <tr>
                <th className="x-th sticky left-0 bg-white dark:bg-ink-card">Student</th>
                {gb.assignments.map(a => (
                  <th key={a.id} className="x-th text-center" title={`max ${a.totalMarks} pts`}>{a.name}</th>
                ))}
                <th className="x-th text-center">Total</th>
              </tr>
            </thead>
            <tbody>
              {gb.students.map(s => {
                const row = gb.grades[s.loginId] || {};
                const achievedSum = gb.assignments.reduce((acc, a) => acc + (row[a.id] || 0), 0);
                const totalSum = gb.assignments.reduce((acc, a) => acc + a.totalMarks, 0);
                return (
                  <tr key={s.loginId} className="x-row">
                    <td className="x-td font-semibold text-slate-900 dark:text-white sticky left-0 bg-white dark:bg-ink-card">{s.username}</td>
                    {gb.assignments.map(a => {
                      const v = row[a.id] || 0;
                      const p = pct(v, a.totalMarks);
                      return (
                        <td key={a.id} className="x-td text-center">
                          <span className={`font-mono font-semibold ${tint(p)}`}>{v}</span>
                          <span className="text-slate-400 text-xs">/{a.totalMarks}</span>
                        </td>
                      );
                    })}
                    <td className="x-td text-center">
                      <span className={`font-display font-bold ${tint(pct(achievedSum, totalSum))}`}>{pct(achievedSum, totalSum)}%</span>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        ) : (
          <div className="empty-state"><BookOpen className="text-slate-300 dark:text-slate-600 mb-3" size={36} /><p className="text-sm text-slate-500">No students or assignments in this course.</p></div>
        )}
      </div>
    </div>
  );
};

export default Gradebook;
