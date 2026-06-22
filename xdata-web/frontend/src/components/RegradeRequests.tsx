import React, { useState, useEffect, useCallback } from 'react';
import api from '../api';
import { toast } from 'react-hot-toast';
import { Gavel, MessageSquare, Check, RefreshCw, ChevronDown, ChevronUp, GitCompareArrows, FileText, CheckCircle, XCircle } from 'lucide-react';
import MarkInfoDisplay from './MarkInfoDisplay';
import DiffTable from './DiffTable';

interface Req {
  id: number; submissionId: number; studentLoginId: string; studentName?: string;
  questionName?: string; questionDescription?: string; message?: string;
  currentMarks?: number; maxMarks?: number; query?: string; createdAt?: string;
  courseId?: string; courseName?: string; assignmentName?: string; markInfoJson?: string;
}

const initials = (s: string) => s.trim().split(/[\s._-]+/).filter(Boolean).slice(0, 2).map(w => w[0]?.toUpperCase()).join('') || '?';

const RegradeRequests: React.FC = () => {
  const [reqs, setReqs] = useState<Req[]>([]);
  const [loading, setLoading] = useState(false);
  const [courseFilter, setCourseFilter] = useState<string>('all');
  const [expandedId, setExpandedId] = useState<number | null>(null);
  const [comparisons, setComparisons] = useState<Record<number, { loading: boolean; data?: any }>>({});
  const [draft, setDraft] = useState<Record<number, { marks: string; reason: string }>>({});

  const load = useCallback(() => {
    setLoading(true);
    api.get('/evaluation/regrade-requests')
      .then(r => setReqs(r.data || []))
      .catch(() => toast.error('Regrade requests could not be loaded'))
      .finally(() => setLoading(false));
  }, []);
  useEffect(() => { load(); }, [load]);

  // Distinct courses present among the open requests (for the filter).
  const courses = Array.from(
    reqs.reduce((m, r) => {
      if (r.courseId) m.set(r.courseId, r.courseName || r.courseId);
      return m;
    }, new Map<string, string>())
  ); // [ [courseId, courseName], ... ]

  const visible = courseFilter === 'all' ? reqs : reqs.filter(r => r.courseId === courseFilter);

  const toggleExpand = async (r: Req) => {
    if (expandedId === r.id) { setExpandedId(null); return; }
    setExpandedId(r.id);
    if (!comparisons[r.id]) {
      setComparisons(c => ({ ...c, [r.id]: { loading: true } }));
      try {
        const res = await api.get(`/evaluation/submissions/${r.submissionId}/result-comparison`);
        setComparisons(c => ({ ...c, [r.id]: { loading: false, data: res.data } }));
      } catch {
        setComparisons(c => ({ ...c, [r.id]: { loading: false, data: { error: 'Comparison not available.' } } }));
      }
    }
  };

  const apply = async (r: Req) => {
    const d = draft[r.id] || { marks: '', reason: '' };
    if (d.marks === '') { toast.error('Please enter a new grade (0–100).'); return; }
    const m = Number(d.marks);
    if (Number.isNaN(m) || m < 0 || m > 100) { toast.error('The grade must be between 0 and 100.'); return; }
    try {
      await api.post(`/evaluation/submissions/${r.submissionId}/override`, { marks: m, reason: d.reason });
      toast.success('Grade overridden & regrade request resolved');
      load();
    } catch {
      toast.error('Grade could not be overridden.');
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-wrap justify-between items-end gap-3">
        <div>
          <p className="kicker">Grading</p>
          <h2 className="mt-1 section-title text-2xl flex items-center gap-2">
            <Gavel size={22} className="text-brand-500" /> Regrade requests
            {reqs.length > 0 && <span className="badge-amber">{reqs.length} open</span>}
          </h2>
        </div>
        <div className="flex items-center gap-2">
          {courses.length > 1 && (
            <select className="x-select w-auto" value={courseFilter} onChange={e => setCourseFilter(e.target.value)}>
              <option value="all">All courses ({reqs.length})</option>
              {courses.map(([id, name]) => (
                <option key={id} value={id}>{name} ({reqs.filter(r => r.courseId === id).length})</option>
              ))}
            </select>
          )}
          <button onClick={load} className="btn-secondary"><RefreshCw size={15} /> Refresh</button>
        </div>
      </div>

      {loading ? (
        <div className="p-12 flex justify-center text-slate-400"><RefreshCw className="animate-spin" size={28} /></div>
      ) : visible.length === 0 ? (
        <div className="x-card empty-state">
          <Gavel className="text-slate-300 dark:text-slate-600 mb-3" size={36} />
          <p className="text-sm text-slate-500">{reqs.length === 0 ? 'No open regrade requests.' : 'No regrade requests in this course.'}</p>
        </div>
      ) : (
        <div className="space-y-4">
          {visible.map(r => {
            const pct = r.currentMarks != null ? Math.round(r.currentMarks * 100) : null;
            const isOpen = expandedId === r.id;
            const cmp = comparisons[r.id];
            return (
              <div key={r.id} className="x-card overflow-hidden">
                <div className="p-5">
                  {/* Identity + grade */}
                  <div className="flex flex-wrap items-start justify-between gap-3">
                    <div className="flex items-center gap-3 min-w-0">
                      <div className="h-10 w-10 shrink-0 rounded-full bg-gradient-to-br from-brand-500 to-brand-700 text-white text-sm font-bold flex items-center justify-center">
                        {initials(r.studentName || r.studentLoginId)}
                      </div>
                      <div className="min-w-0">
                        <p className="font-semibold text-slate-900 dark:text-white truncate">{r.studentName || r.studentLoginId}</p>
                        <p className="text-xs text-slate-400 truncate">
                          {[r.courseName, r.assignmentName, r.questionName].filter(Boolean).join(' · ')}
                          {r.createdAt ? ` · ${new Date(r.createdAt).toLocaleDateString('en-US')}` : ''}
                        </p>
                      </div>
                    </div>
                    {pct != null && (
                      <span className={`badge ${pct >= 50 ? 'badge-success' : 'bg-hard/10 text-hard ring-hard/20'}`}>
                        Current {pct}%
                      </span>
                    )}
                  </div>

                  {/* Student's regrade request */}
                  {r.message && (
                    <div className="mt-3 note-amber p-3 flex items-start gap-2">
                      <MessageSquare className="shrink-0 mt-0.5 opacity-80" size={16} />
                      <p className="text-sm">{r.message}</p>
                    </div>
                  )}

                  {/* Context toggle */}
                  <button onClick={() => toggleExpand(r)} className="btn-ghost mt-3 !px-2 text-xs">
                    {isOpen ? <ChevronUp size={15} /> : <ChevronDown size={15} />} {isOpen ? 'Hide context' : 'View context'}
                  </button>

                  {isOpen && (
                    <div className="mt-3 space-y-4 animate-fadeIn">
                      {r.questionDescription?.trim() && (
                        <div>
                          <p className="kicker flex items-center gap-1.5 mb-1.5"><FileText size={12} /> Problem statement</p>
                          <p className="text-sm text-slate-700 dark:text-slate-300">{r.questionDescription}</p>
                        </div>
                      )}
                      <div>
                        <p className="kicker mb-1.5">Submitted query</p>
                        <pre className="x-code">{r.query}</pre>
                      </div>
                      {r.markInfoJson && (
                        <div>
                          <p className="kicker mb-1.5">Grade breakdown</p>
                          <MarkInfoDisplay markInfoJson={r.markInfoJson} />
                        </div>
                      )}
                      <div>
                        <p className="kicker flex items-center gap-1.5 mb-1.5"><GitCompareArrows size={12} /> Expected ↔ Actual output</p>
                        {!cmp || cmp.loading ? (
                          <div className="flex items-center gap-2 text-sm text-slate-400 py-3"><RefreshCw className="animate-spin" size={15} /> loading …</div>
                        ) : cmp.data?.error ? (
                          <p className="text-sm text-slate-400 italic">{cmp.data.error}</p>
                        ) : (
                          <div className="space-y-2">
                            {cmp.data.match
                              ? <span className="badge badge-success"><CheckCircle size={12} /> Output matches</span>
                              : <span className="badge bg-hard/10 text-hard ring-hard/20"><XCircle size={12} /> {(cmp.data.missing?.length || 0)} missing · {(cmp.data.extra?.length || 0)} extra</span>}
                            <div className="grid md:grid-cols-2 gap-3">
                              <DiffTable title="Expected output" data={cmp.data.expected} highlight={cmp.data.missing} accent="easy" />
                              <DiffTable title="Actual output" data={cmp.data.actual} highlight={cmp.data.extra} accent="hard" />
                            </div>
                          </div>
                        )}
                      </div>
                    </div>
                  )}
                </div>

                {/* Decision footer */}
                <div className="border-t border-slate-100 dark:border-ink-border/70 bg-slate-50/60 dark:bg-ink-soft/30 px-5 py-3 flex flex-col sm:flex-row gap-2 items-stretch sm:items-center">
                  <input type="number" min="0" max="100" placeholder="New grade %" className="x-input sm:w-36"
                    value={draft[r.id]?.marks ?? ''}
                    onChange={e => setDraft({ ...draft, [r.id]: { ...(draft[r.id] || { marks: '', reason: '' }), marks: e.target.value } })} />
                  <input type="text" placeholder="Reason (optional)" className="x-input flex-1"
                    value={draft[r.id]?.reason ?? ''}
                    onChange={e => setDraft({ ...draft, [r.id]: { ...(draft[r.id] || { marks: '', reason: '' }), reason: e.target.value } })} />
                  <button onClick={() => apply(r)} className="btn-primary shrink-0"><Check size={16} /> Apply</button>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};

export default RegradeRequests;
