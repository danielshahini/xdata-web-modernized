import React, { useState, useEffect, useCallback } from 'react';
import { CalendarClock, X, Trash2, Plus } from 'lucide-react';
import api from '../api';
import { toast } from 'react-hot-toast';

interface Ext { id: number; assignmentId: number; studentLoginId: string; extendedDeadline: string; }
interface Props { assignmentId: number; assignmentName: string; onClose: () => void; }

const DeadlineExtensionModal: React.FC<Props> = ({ assignmentId, assignmentName, onClose }) => {
  const [extensions, setExtensions] = useState<Ext[]>([]);
  const [students, setStudents] = useState<{ loginId: string; username: string }[]>([]);
  const [studentLoginId, setStudentLoginId] = useState('');
  const [deadline, setDeadline] = useState('');
  const [loading, setLoading] = useState(false);

  const load = useCallback(async () => {
    try {
      const [ext, users] = await Promise.all([
        api.get(`/assignments/${assignmentId}/extensions`),
        api.get('/admin/users'),
      ]);
      setExtensions(ext.data || []);
      setStudents((users.data || []).filter((u: any) => (u.role || '').toUpperCase() === 'STUDENT'));
    } catch { toast.error('Deadlines could not be loaded'); }
  }, [assignmentId]);

  useEffect(() => { load(); }, [load]);

  const add = async () => {
    if (!studentLoginId || !deadline) { toast.error('Please select a student and a date'); return; }
    setLoading(true);
    try {
      await api.post(`/assignments/${assignmentId}/extensions`, { studentLoginId, extendedDeadline: deadline });
      toast.success('Deadline set');
      setStudentLoginId(''); setDeadline('');
      load();
    } catch (e: any) {
      toast.error(e.response?.data || 'Error setting the deadline');
    } finally { setLoading(false); }
  };

  const remove = async (sid: string) => {
    try {
      await api.delete(`/assignments/${assignmentId}/extensions/${sid}`);
      toast.success('Deadline removed');
      load();
    } catch { toast.error('Error removing'); }
  };

  const nameFor = (sid: string) => students.find(s => s.loginId === sid)?.username || sid;

  return (
    <div className="modal-overlay">
      <div className="modal-card max-w-lg p-6 sm:p-8">
        <div className="flex items-start justify-between mb-1">
          <h3 className="section-title flex items-center gap-2"><CalendarClock size={20} className="text-brand-500" /> Deadline extensions</h3>
          <button onClick={onClose} className="icon-btn" aria-label="Close"><X size={18} /></button>
        </div>
        <p className="text-sm text-slate-500 dark:text-slate-400 mb-6">Individual deadline for "{assignmentName}". No late penalty applies until then.</p>

        <div className="flex flex-col sm:flex-row gap-2 mb-5">
          <select className="x-select flex-1" value={studentLoginId} onChange={e => setStudentLoginId(e.target.value)}>
            <option value="">Select a student…</option>
            {students.map(s => <option key={s.loginId} value={s.loginId}>{s.username} ({s.loginId})</option>)}
          </select>
          <input type="datetime-local" className="x-input sm:w-52" value={deadline} onChange={e => setDeadline(e.target.value)} />
          <button onClick={add} disabled={loading} className="btn-primary shrink-0"><Plus size={16} /> Set</button>
        </div>

        <div className="space-y-2 max-h-64 overflow-auto">
          {extensions.length === 0 && <p className="text-sm text-slate-400 italic">No extensions yet.</p>}
          {extensions.map(ex => (
            <div key={ex.id} className="flex items-center justify-between gap-3 p-3 rounded-xl border border-slate-200 dark:border-ink-border bg-slate-50/50 dark:bg-ink-soft/40">
              <div className="min-w-0">
                <p className="font-semibold text-slate-900 dark:text-white truncate">{nameFor(ex.studentLoginId)}</p>
                <p className="text-xs text-slate-500 dark:text-slate-400">{new Date(ex.extendedDeadline).toLocaleString('en-US')}</p>
              </div>
              <button onClick={() => remove(ex.studentLoginId)} className="icon-btn hover:text-hard shrink-0" aria-label="Remove"><Trash2 size={16} /></button>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default DeadlineExtensionModal;
