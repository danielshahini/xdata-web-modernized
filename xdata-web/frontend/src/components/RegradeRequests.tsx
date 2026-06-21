import React, { useState, useEffect, useCallback } from 'react';
import api from '../api';
import { toast } from 'react-hot-toast';
import { Gavel, MessageSquare, Check, RefreshCw } from 'lucide-react';

interface Req {
  id: number; submissionId: number; studentLoginId: string; studentName?: string;
  questionName?: string; message?: string; currentMarks?: number; query?: string; createdAt?: string;
}

const RegradeRequests: React.FC = () => {
  const [reqs, setReqs] = useState<Req[]>([]);
  const [loading, setLoading] = useState(false);
  const [draft, setDraft] = useState<Record<number, { marks: string; reason: string }>>({});

  const load = useCallback(() => {
    setLoading(true);
    api.get('/evaluation/regrade-requests')
      .then(r => setReqs(r.data || []))
      .catch(() => toast.error('Anfechtungen konnten nicht geladen werden'))
      .finally(() => setLoading(false));
  }, []);
  useEffect(() => { load(); }, [load]);

  const apply = async (r: Req) => {
    const d = draft[r.id] || { marks: '', reason: '' };
    if (d.marks === '') { toast.error('Bitte neue Note (0–100) angeben'); return; }
    try {
      await api.post(`/evaluation/submissions/${r.submissionId}/override`, { marks: Number(d.marks), reason: d.reason });
      toast.success('Note überschrieben & Anfechtung gelöst');
      load();
    } catch (e: any) {
      toast.error(e.response?.data || 'Fehler beim Überschreiben');
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <p className="kicker">Bewertung</p>
          <h2 className="mt-1 section-title text-2xl flex items-center gap-2"><Gavel size={22} className="text-brand-500" /> Anfechtungen</h2>
        </div>
        <button onClick={load} className="btn-secondary"><RefreshCw size={15} /> Aktualisieren</button>
      </div>

      {loading ? (
        <div className="p-12 flex justify-center text-slate-400"><RefreshCw className="animate-spin" size={28} /></div>
      ) : reqs.length === 0 ? (
        <div className="x-card empty-state"><Gavel className="text-slate-300 dark:text-slate-600 mb-3" size={36} /><p className="text-sm text-slate-500">Keine offenen Anfechtungen.</p></div>
      ) : (
        <div className="space-y-4">
          {reqs.map(r => (
            <div key={r.id} className="x-card p-5">
              <div className="flex flex-wrap justify-between gap-3">
                <div className="min-w-0">
                  <p className="font-semibold text-slate-900 dark:text-white">{r.studentName || r.studentLoginId} <span className="text-slate-400 font-normal">· {r.questionName}</span></p>
                  <p className="text-xs text-slate-400 mt-0.5">Aktuell: {r.currentMarks != null ? Math.round(r.currentMarks * 100) : '–'}%{r.createdAt ? ` · ${new Date(r.createdAt).toLocaleString('de-DE')}` : ''}</p>
                </div>
              </div>
              {r.message && (
                <div className="mt-3 p-3 rounded-xl bg-amber-50 dark:bg-amber-900/20 border border-amber-100 dark:border-amber-900/40 flex items-start gap-2">
                  <MessageSquare className="text-amber-600 shrink-0 mt-0.5" size={16} />
                  <p className="text-sm text-slate-700 dark:text-slate-200">{r.message}</p>
                </div>
              )}
              {r.query && <pre className="mt-3 p-3 rounded-xl bg-slate-900 dark:bg-black text-green-400 text-xs font-mono overflow-x-auto">{r.query}</pre>}
              <div className="mt-4 flex flex-col sm:flex-row gap-2 items-stretch sm:items-center">
                <input type="number" min="0" max="100" placeholder="Neue Note %" className="x-input sm:w-36"
                  value={draft[r.id]?.marks ?? ''}
                  onChange={e => setDraft({ ...draft, [r.id]: { ...(draft[r.id] || { marks: '', reason: '' }), marks: e.target.value } })} />
                <input type="text" placeholder="Begründung (optional)" className="x-input flex-1"
                  value={draft[r.id]?.reason ?? ''}
                  onChange={e => setDraft({ ...draft, [r.id]: { ...(draft[r.id] || { marks: '', reason: '' }), reason: e.target.value } })} />
                <button onClick={() => apply(r)} className="btn-primary shrink-0"><Check size={16} /> Übernehmen</button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default RegradeRequests;
